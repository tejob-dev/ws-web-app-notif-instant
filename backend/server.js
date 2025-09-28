const express = require('express');
const http = require('http');
const socketIo = require('socket.io');
const cors = require('cors');
const bodyParser = require('body-parser');
const sqlite3 = require('sqlite3').verbose();
const { v4: uuidv4 } = require('uuid');
const webpush = require('web-push');
const admin = require('firebase-admin');
const firebaseConfig = require('./firebase-config');
const NativeWebSocketServer = require('./websocket-server');
const app = express();
const server = http.createServer(app);
const io = socketIo(server, {
  cors: {
    origin: [
      "http://localhost:3000",
      "http://localhost:5020",
      "http://192.168.1.71:3001", 
      "http://localhost:3001",
      "http://192.168.1.71:3000",
      "http://localhost:8081", // Expo DevTools
      "http://192.168.1.71:8081", // Expo DevTools sur réseau
      "http://69.197.142.189:5020", // Frontend production
      "http://69.197.142.189:5022", // Backend production
      "*" // Permettre toutes les origines pour les apps mobiles
    ],
    methods: ["GET", "POST"],
    credentials: true,
    allowedHeaders: ["*"]
  },
  allowEIO3: true // Support pour les anciennes versions de Socket.IO
});

const PORT = process.env.PORT || 3001;
const WS_PORT = process.env.WS_PORT || 3002; // Port pour le serveur WebSocket natif

// Middleware
app.use(cors());
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

// Configuration des notifications push
const vapidKeys = {
  publicKey: 'BB-gI3Sd66lBKWvp5VTim8tLaSzqeMv_Le6JIghZpUdrQshjGIHZGsNg9Gt23sgVYxB110YGWa_j4AeXhJHDJu4',
  privateKey: 'LLNMldIBxVMLfVpBB_l8iKx1kKtCEEdwJZWxR8bQ4Xc'
};

webpush.setVapidDetails(
  'mailto:test@example.com',
  vapidKeys.publicKey,
  vapidKeys.privateKey
);

// Configuration Firebase Admin (pour les notifications mobiles)
let firebaseInitialized = false;

try {
  // Essayer d'utiliser les variables d'environnement d'abord
  if (firebaseConfig.envConfig.projectId && firebaseConfig.envConfig.privateKey && firebaseConfig.envConfig.clientEmail) {
    admin.initializeApp({
      credential: admin.credential.cert(firebaseConfig.envConfig),
      projectId: firebaseConfig.envConfig.projectId
    });
    firebaseInitialized = true;
    console.log('✅ Firebase Admin initialisé avec les variables d\'environnement');
  } 
  // Sinon essayer d'utiliser le fichier de service account
  else if (firebaseConfig.serviceAccount.project_id) {
    admin.initializeApp({
      credential: admin.credential.cert(firebaseConfig.serviceAccount),
      projectId: firebaseConfig.serviceAccount.project_id
    });
    firebaseInitialized = true;
    console.log('✅ Firebase Admin initialisé avec le fichier de configuration');
  }
  // Sinon essayer d'utiliser le fichier google-services.json
  else {
    try {
      const serviceAccount = require('./firebase-service-account.json');
      admin.initializeApp({
        credential: admin.credential.cert(serviceAccount),
        projectId: serviceAccount.project_id
      });
      firebaseInitialized = true;
      console.log('✅ Firebase Admin initialisé avec firebase-service-account.json');
    } catch (fileError) {
      throw new Error('Aucune configuration Firebase trouvée');
    }
  }
} catch (error) {
  console.log('⚠️ Firebase Admin non configuré - notifications mobiles désactivées');
  console.log('Pour activer les notifications mobiles :');
  console.log('1. Configurez les variables d\'environnement dans .env');
  console.log('2. Ou placez firebase-service-account.json dans le dossier backend');
  console.log('3. Ou modifiez firebase-config.js avec vos paramètres');
}

// Base de données SQLite
const dbPath = process.env.DB_PATH || './notifications.db';
const db = new sqlite3.Database(dbPath, (err) => {
  if (err) {
    console.error('❌ Erreur lors de l\'ouverture de la base de données:', err.message);
  } else {
    console.log('✅ Base de données SQLite connectée avec succès');
  }
});

// Créer les tables
db.serialize(() => {
  // Table des messages
  db.run(`CREATE TABLE IF NOT EXISTS messages (
    id TEXT PRIMARY KEY,
    content TEXT NOT NULL,
    type TEXT DEFAULT 'info',
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
  )`);

  // Table des tokens mobiles
  db.run(`CREATE TABLE IF NOT EXISTS mobile_tokens (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    token TEXT UNIQUE NOT NULL,
    platform TEXT NOT NULL,
    device_id TEXT,
    user_agent TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_used DATETIME DEFAULT CURRENT_TIMESTAMP
  )`);
});

// Stockage en mémoire des connexions WebSocket
const connectedClients = new Set();

// Instance du serveur WebSocket natif
let nativeWebSocketServer = null;

// WebSocket connection handling
io.on('connection', (socket) => {
  console.log('Nouveau client connecté:', socket.id);
  connectedClients.add(socket.id);

  socket.on('disconnect', () => {
    console.log('Client déconnecté:', socket.id);
    connectedClients.delete(socket.id);
  });

  // Envoyer un message de bienvenue
  // socket.emit('message', {
  //   id: uuidv4(),
  //   content: 'Vous êtes maintenant connecté au système de notifications !',
  //   type: 'success',
  //   timestamp: new Date()
  // });

  // Enregistrer un token mobile
  socket.on('register-mobile-token', (data) => {
    const { token, platform, deviceId } = data;
    
    db.run(
      'INSERT OR REPLACE INTO mobile_tokens (token, platform, device_id, user_agent, last_used) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)',
      [token, platform, deviceId, socket.handshake.headers['user-agent']],
      function(err) {
        if (err) {
          console.error('Erreur lors de l\'enregistrement du token mobile:', err);
        } else {
          console.log(`Token mobile enregistré: ${platform} - ${token.substring(0, 20)}...`);
        }
      }
    );
  });
});

// Fonction pour valider un token FCM
function isValidFCMToken(token) {
  // Un token FCM valide doit:
  // - Commencer par un préfixe valide
  // - Avoir une longueur appropriée (généralement 140+ caractères)
  // - Contenir uniquement des caractères alphanumériques, tirets et underscores
  
  if (!token || typeof token !== 'string') {
    return false;
  }
  
  // Vérifier la longueur minimale (tokens FCM sont généralement longs)
  if (token.length < 100) {
    return false;
  }
  
  // Vérifier qu'il ne contient que des caractères valides
  const validPattern = /^[A-Za-z0-9_-]+$/;
  return validPattern.test(token);
}

// Fonction pour envoyer des notifications mobiles
async function sendMobileNotification(token, platform, message) {
  try {
    if (platform === 'android' || platform === 'ios') {
      // Valider le token avant d'essayer l'envoi
      if (!isValidFCMToken(token)) {
        console.log(`⚠️ Token FCM invalide détecté: ${token.substring(0, 20)}...`);
        // Supprimer le token invalide de la base de données
        db.run('DELETE FROM mobile_tokens WHERE token = ?', [token], (err) => {
          if (err) {
            console.error('Erreur lors de la suppression du token invalide:', err);
          } else {
            console.log('✅ Token invalide supprimé de la base de données');
          }
        });
        return;
      }
      if (!firebaseInitialized) {
        console.log('⚠️ Firebase non configuré - notification mobile ignorée');
        return;
      }

      // Utiliser Firebase Cloud Messaging pour Android/iOS
      const notification = {
        title: 'Nouvelle notification',
        body: message.content
      };

      const messageData = {
        messageId: message.id,
        type: message.type,
        timestamp: message.timestamp.toISOString()
      };

      const message_fcm = {
        token: token,
        notification: notification,
        data: messageData
      };

      const response = await admin.messaging().send(message_fcm);
      console.log(`✅ Notification FCM envoyée: ${response}`);
      
    } else if (platform === 'web') {
      // Utiliser Web Push pour les navigateurs web
      const payload = JSON.stringify({
        title: 'Nouvelle notification',
        body: message.content,
        icon: '/favicon.ico',
        badge: '/favicon.ico',
        data: {
          messageId: message.id,
          type: message.type,
          timestamp: message.timestamp.toISOString()
        }
      });

      await webpush.sendNotification(token, payload);
      console.log(`✅ Notification Web Push envoyée`);
    }
  } catch (error) {
    console.error(`❌ Erreur lors de l'envoi de la notification mobile:`, error);
    
    // Supprimer le token invalide pour tous les types d'erreurs FCM
    if (error.code === 'messaging/invalid-registration-token' || 
        error.code === 'messaging/registration-token-not-registered' ||
        error.code === 'messaging/invalid-argument' ||
        error.statusCode === 410 ||
        error.message?.includes('registration token') ||
        error.message?.includes('FCM registration token')) {
      
      console.log(`🗑️ Suppression du token invalide: ${token.substring(0, 20)}...`);
      db.run('DELETE FROM mobile_tokens WHERE token = ?', [token], (err) => {
        if (err) {
          console.error('Erreur lors de la suppression du token invalide:', err);
        } else {
          console.log('✅ Token invalide supprimé de la base de données');
        }
      });
    }
  }
}

// API Routes

// Fonction commune pour envoyer un message
async function sendMessageHandler(content, type = 'info', res) {
  try {
    if (!content) {
      return res.status(400).json({ error: 'Le contenu du message est requis' });
    }

    const messageId = uuidv4();
    const message = {
      id: messageId,
      content,
      type,
      timestamp: new Date()
    };

    // Sauvegarder en base de données avec gestion des doublons
    db.run(
      'INSERT OR REPLACE INTO messages (id, content, type) VALUES (?, ?, ?)',
      [messageId, content, type],
      function(err) {
        if (err) {
          console.error('Erreur lors de la sauvegarde:', err);
          // Si l'erreur persiste, générer un nouvel ID et réessayer
          const newMessageId = uuidv4();
          db.run(
            'INSERT INTO messages (id, content, type) VALUES (?, ?, ?)',
            [newMessageId, content, type],
            function(retryErr) {
              if (retryErr) {
                console.error('Erreur lors de la sauvegarde (retry):', retryErr);
              } else {
                message.id = newMessageId; // Mettre à jour l'ID pour les autres opérations
                console.log('Message sauvegardé avec un nouvel ID:', newMessageId);
              }
            }
          );
        }
      }
    );

    // Envoyer via WebSocket à tous les clients connectés (Socket.IO)
    io.emit('message', message);
    console.log(`Message Socket.IO envoyé à ${connectedClients.size} client(s) connecté(s)`);

    // Envoyer aussi via WebSocket natif
    if (nativeWebSocketServer) {
      nativeWebSocketServer.broadcast(message);
    }

    // Envoyer des notifications push aux appareils mobiles
    db.all('SELECT token, platform FROM mobile_tokens', [], (err, rows) => {
      if (err) {
        console.error('Erreur lors de la récupération des tokens mobiles:', err);
        return;
      }

      if (rows.length > 0) {
        console.log(`Envoi de notifications à ${rows.length} appareil(s) mobile(s)`);
        
        rows.forEach(row => {
          sendMobileNotification(row.token, row.platform, message);
        });
      }
    });

    res.json({ success: true, message: 'Message envoyé avec succès' });
  } catch (error) {
    console.error('Erreur lors de l\'envoi du message:', error);
    res.status(500).json({ error: 'Erreur interne du serveur' });
  }
}

// Envoyer un message broadcast via POST
app.post('/api/send-message', async (req, res) => {
  const { content, type = 'info' } = req.body;
  await sendMessageHandler(content, type, res);
});

// Envoyer un message broadcast via GET
app.get('/api/send-message', async (req, res) => {
  const { content, type = 'info' } = req.query;
  await sendMessageHandler(content, type, res);
});

// Récupérer l'historique des messages
app.get('/api/messages', (req, res) => {
  const limit = parseInt(req.query.limit) || 50;
  
  db.all(
    'SELECT * FROM messages ORDER BY timestamp DESC LIMIT ?',
    [limit],
    (err, rows) => {
      if (err) {
        console.error('Erreur lors de la récupération des messages:', err);
        return res.status(500).json({ error: 'Erreur lors de la récupération des messages' });
      }
      
      res.json(rows);
    }
  );
});

// Enregistrer un token mobile via API REST
app.post('/api/register-mobile-token', (req, res) => {
  try {
    const { token, platform, deviceId } = req.body;
    
    if (!token || !platform) {
      return res.status(400).json({ error: 'Token et platform sont requis' });
    }

    db.run(
      'INSERT OR REPLACE INTO mobile_tokens (token, platform, device_id, user_agent, last_used) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)',
      [token, platform, deviceId, req.headers['user-agent']],
      function(err) {
        if (err) {
          console.error('Erreur lors de l\'enregistrement du token mobile:', err);
          return res.status(500).json({ error: 'Erreur lors de l\'enregistrement du token' });
        }
        
        console.log(`Token mobile enregistré via API: ${platform} - ${token.substring(0, 20)}...`);
        res.json({ success: true, message: 'Token mobile enregistré avec succès' });
      }
    );
  } catch (error) {
    console.error('Erreur lors de l\'enregistrement du token mobile:', error);
    res.status(500).json({ error: 'Erreur interne du serveur' });
  }
});

// Récupérer les statistiques
app.get('/api/stats', (req, res) => {
  db.get('SELECT COUNT(*) as totalMessages FROM messages', (err, messageCount) => {
    if (err) {
      return res.status(500).json({ error: 'Erreur lors de la récupération des statistiques' });
    }

    db.get('SELECT COUNT(*) as totalMobileTokens FROM mobile_tokens', (err, tokenCount) => {
      if (err) {
        return res.status(500).json({ error: 'Erreur lors de la récupération des statistiques' });
      }

      res.json({
        totalMessages: messageCount.totalMessages,
        totalMobileTokens: tokenCount.totalMobileTokens,
        connectedClients: connectedClients.size
      });
    });
  });
});

// Endpoint pour obtenir la clé publique VAPID
app.get('/api/vapid-public-key', (req, res) => {
  res.json({ publicKey: vapidKeys.publicKey });
});

// Health check
app.get('/api/health', (req, res) => {
  res.json({ 
    status: 'OK', 
    timestamp: new Date().toISOString(),
    connectedClients: connectedClients.size
  });
});

// Nettoyer les tokens invalides
app.post('/api/cleanup-invalid-tokens', (req, res) => {
  db.all('SELECT token, platform FROM mobile_tokens', [], (err, rows) => {
    if (err) {
      return res.status(500).json({ error: 'Erreur lors de la récupération des tokens' });
    }

    let cleanedCount = 0;
    let processedCount = 0;
    
    if (rows.length === 0) {
      return res.json({ 
        message: 'Aucun token à nettoyer', 
        cleaned: 0, 
        total: 0 
      });
    }

    rows.forEach(row => {
      if (row.platform === 'android' || row.platform === 'ios') {
        if (!isValidFCMToken(row.token)) {
          db.run('DELETE FROM mobile_tokens WHERE token = ?', [row.token], (deleteErr) => {
            if (deleteErr) {
              console.error('Erreur lors de la suppression:', deleteErr);
            } else {
              cleanedCount++;
            }
            
            processedCount++;
            if (processedCount === rows.length) {
              res.json({ 
                message: 'Nettoyage terminé', 
                cleaned: cleanedCount, 
                total: rows.length 
              });
            }
          });
        } else {
          processedCount++;
          if (processedCount === rows.length) {
            res.json({ 
              message: 'Nettoyage terminé', 
              cleaned: cleanedCount, 
              total: rows.length 
            });
          }
        }
      } else {
        processedCount++;
        if (processedCount === rows.length) {
          res.json({ 
            message: 'Nettoyage terminé', 
            cleaned: cleanedCount, 
            total: rows.length 
          });
        }
      }
    });
  });
});

// Démarrer le serveur
server.listen(PORT, '0.0.0.0', async () => {
  console.log(`🚀 Serveur Socket.IO démarré sur le port ${PORT}`);
  console.log(`📱 Socket.IO disponible sur ws://localhost:${PORT}`);
  console.log(`📱 Socket.IO disponible sur ws://192.168.1.71:${PORT}`);
  console.log(`📱 Socket.IO disponible sur ws://69.197.142.189:5022`);
  console.log(`🌐 API REST disponible sur http://localhost:${PORT}/api`);
  console.log(`🌐 API REST disponible sur http://192.168.1.71:${PORT}/api`);
  console.log(`🌐 API REST disponible sur http://69.197.142.189:5022/api`);

  // Démarrer le serveur WebSocket natif
  try {
    nativeWebSocketServer = new NativeWebSocketServer(WS_PORT);
    await nativeWebSocketServer.start();
    console.log(`✅ Serveur WebSocket natif démarré sur le port ${WS_PORT}`);
  } catch (error) {
    console.error('❌ Erreur lors du démarrage du serveur WebSocket natif:', error);
  }
});

// Gestion propre de l'arrêt
process.on('SIGINT', async () => {
  console.log('\n🛑 Arrêt du serveur...');
  
  // Arrêter le serveur WebSocket natif
  if (nativeWebSocketServer) {
    await nativeWebSocketServer.stop();
  }
  
  db.close((err) => {
    if (err) {
      console.error('Erreur lors de la fermeture de la base de données:', err);
    } else {
      console.log('Base de données fermée.');
    }
    process.exit(0);
  });
});
