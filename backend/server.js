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
const app = express();
const server = http.createServer(app);
const io = socketIo(server, {
  cors: {
    origin: [
      "http://localhost:3000", 
      "http://192.168.1.71:3001", 
      "http://localhost:3001",
      "http://192.168.1.71:3000",
      "http://localhost:8081", // Expo DevTools
      "http://192.168.1.71:8081", // Expo DevTools sur réseau
      "*" // Permettre toutes les origines pour les apps mobiles
    ],
    methods: ["GET", "POST"],
    credentials: true,
    allowedHeaders: ["*"]
  },
  allowEIO3: true // Support pour les anciennes versions de Socket.IO
});

const PORT = 3001;

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
const db = new sqlite3.Database('./notifications.db');

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

// WebSocket connection handling
io.on('connection', (socket) => {
  console.log('Nouveau client connecté:', socket.id);
  connectedClients.add(socket.id);

  socket.on('disconnect', () => {
    console.log('Client déconnecté:', socket.id);
    connectedClients.delete(socket.id);
  });

  // Envoyer un message de bienvenue
  socket.emit('message', {
    id: uuidv4(),
    content: 'Vous êtes maintenant connecté au système de notifications !',
    type: 'success',
    timestamp: new Date()
  });

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

// Fonction pour envoyer des notifications mobiles
async function sendMobileNotification(token, platform, message) {
  try {
    if (platform === 'android' || platform === 'ios') {
      if (!firebaseInitialized) {
        console.log('⚠️ Firebase non configuré - notification mobile ignorée');
        return;
      }

      // Utiliser Firebase Cloud Messaging pour Android/iOS
      const notification = {
        title: 'Nouvelle notification',
        body: message.content,
        data: {
          messageId: message.id,
          type: message.type,
          timestamp: message.timestamp.toISOString()
        }
      };

      const message_fcm = {
        token: token,
        notification: notification,
        data: notification.data
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
    
    // Supprimer le token invalide
    if (error.code === 'messaging/invalid-registration-token' || 
        error.code === 'messaging/registration-token-not-registered' ||
        error.statusCode === 410) {
      db.run('DELETE FROM mobile_tokens WHERE token = ?', [token], (err) => {
        if (err) {
          console.error('Erreur lors de la suppression du token invalide:', err);
        } else {
          console.log('Token invalide supprimé de la base de données');
        }
      });
    }
  }
}

// API Routes

// Envoyer un message broadcast
app.post('/api/send-message', async (req, res) => {
  try {
    const { content, type = 'info' } = req.body;
    
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

    // Sauvegarder en base de données
    db.run(
      'INSERT INTO messages (id, content, type) VALUES (?, ?, ?)',
      [messageId, content, type],
      function(err) {
        if (err) {
          console.error('Erreur lors de la sauvegarde:', err);
        }
      }
    );

    // Envoyer via WebSocket à tous les clients connectés
    io.emit('message', message);
    console.log(`Message envoyé à ${connectedClients.size} client(s) connecté(s)`);

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

// Démarrer le serveur
server.listen(PORT, '0.0.0.0', () => {
  console.log(`🚀 Serveur démarré sur le port ${PORT}`);
  console.log(`📱 WebSocket disponible sur ws://localhost:${PORT}`);
  console.log(`📱 WebSocket disponible sur ws://192.168.1.71:${PORT}`);
  console.log(`🌐 API REST disponible sur http://localhost:${PORT}/api`);
  console.log(`🌐 API REST disponible sur http://192.168.1.71:${PORT}/api`);
});

// Gestion propre de l'arrêt
process.on('SIGINT', () => {
  console.log('\n🛑 Arrêt du serveur...');
  db.close((err) => {
    if (err) {
      console.error('Erreur lors de la fermeture de la base de données:', err);
    } else {
      console.log('Base de données fermée.');
    }
    process.exit(0);
  });
});
