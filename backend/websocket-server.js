const WebSocket = require('ws');
const http = require('http');
const sqlite3 = require('sqlite3').verbose();
const { v4: uuidv4 } = require('uuid');

/**
 * Serveur WebSocket natif pour la production mobile
 * Compatible avec le WebSocket natif de React Native
 */
class NativeWebSocketServer {
  constructor(port = 5023) {
    this.port = port;
    this.server = null;
    this.wss = null;
    this.clients = new Set();
    this.db = null;
    this.messageHandlers = new Map();
    
    this.init();
  }

  init() {
    // Créer le serveur HTTP
    this.server = http.createServer();
    
    // Créer le serveur WebSocket
    this.wss = new WebSocket.Server({ 
      server: this.server,
      path: '/',
      perMessageDeflate: false // Désactiver la compression pour éviter les problèmes
    });

    // Configurer les gestionnaires d'événements
    this.setupEventHandlers();
    
    // Initialiser la base de données
    this.initDatabase();
  }

  setupEventHandlers() {
    this.wss.on('connection', (ws, req) => {
      console.log('🔗 Nouveau client WebSocket natif connecté');
      this.clients.add(ws);
      
      // Envoyer un message de bienvenue
      this.sendToClient(ws, {
        id: uuidv4(),
        content: 'Connexion WebSocket native établie avec succès !',
        type: 'success',
        timestamp: new Date().toISOString()
      });

      // Gérer les messages reçus
      ws.on('message', (data) => {
        try {
          const message = JSON.parse(data.toString());
          this.handleClientMessage(ws, message);
        } catch (error) {
          console.error('❌ Erreur lors du parsing du message WebSocket:', error);
          this.sendToClient(ws, {
            id: uuidv4(),
            content: 'Erreur: Format de message invalide',
            type: 'error',
            timestamp: new Date().toISOString()
          });
        }
      });

      // Gérer la déconnexion
      ws.on('close', (code, reason) => {
        console.log(`🔌 Client WebSocket déconnecté: ${code} ${reason}`);
        this.clients.delete(ws);
      });

      // Gérer les erreurs
      ws.on('error', (error) => {
        console.error('❌ Erreur WebSocket client:', error);
        this.clients.delete(ws);
      });
    });

    this.wss.on('error', (error) => {
      console.error('❌ Erreur serveur WebSocket:', error);
    });
  }

  initDatabase() {
    const dbPath = process.env.DB_PATH || './notifications.db';
    this.db = new sqlite3.Database(dbPath, (err) => {
      if (err) {
        console.error('❌ Erreur base de données WebSocket:', err.message);
      } else {
        console.log('✅ Base de données WebSocket connectée');
      }
    });
  }

  handleClientMessage(client, message) {
    console.log('📨 Message WebSocket reçu:', message);

    switch (message.type) {
      case 'register-mobile-token':
        this.handleMobileTokenRegistration(client, message.data);
        break;
      
      case 'ping':
        this.sendToClient(client, { type: 'pong', timestamp: new Date().toISOString() });
        break;
      
      default:
        console.log('📝 Message traité:', message);
    }
  }

  handleMobileTokenRegistration(client, tokenData) {
    if (!this.db || !tokenData) return;

    const { token, platform, deviceId } = tokenData;
    
    this.db.run(
      'INSERT OR REPLACE INTO mobile_tokens (token, platform, device_id, user_agent, last_used) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)',
      [token, platform, deviceId, 'WebSocket Native Client'],
      function(err) {
        if (err) {
          console.error('❌ Erreur enregistrement token WebSocket:', err);
        } else {
          console.log(`✅ Token WebSocket enregistré: ${platform} - ${token.substring(0, 20)}...`);
        }
      }
    );
  }

  sendToClient(client, message) {
    if (client.readyState === WebSocket.OPEN) {
      try {
        client.send(JSON.stringify(message));
      } catch (error) {
        console.error('❌ Erreur envoi message WebSocket:', error);
      }
    }
  }

  broadcast(message) {
    console.log(`📢 Broadcast WebSocket à ${this.clients.size} client(s)`);
    
    const messageData = typeof message === 'string' ? 
      { content: message, type: 'info', timestamp: new Date().toISOString() } : 
      message;

    this.clients.forEach(client => {
      this.sendToClient(client, messageData);
    });

    // Sauvegarder en base de données
    this.saveMessage(messageData);
  }

  saveMessage(message) {
    if (!this.db) return;

    this.db.run(
      'INSERT OR IGNORE INTO messages (id, content, type) VALUES (?, ?, ?)',
      [message.id || uuidv4(), message.content, message.type || 'info'],
      function(err) {
        if (err) {
          console.error('❌ Erreur sauvegarde message WebSocket:', err);
        }
      }
    );
  }

  start() {
    return new Promise((resolve, reject) => {
      this.server.listen(this.port, '0.0.0.0', (err) => {
        if (err) {
          reject(err);
        } else {
          console.log(`🚀 Serveur WebSocket natif démarré sur le port ${this.port}`);
          console.log(`📱 WebSocket disponible sur ws://localhost:${this.port}`);
          console.log(`📱 WebSocket disponible sur ws://192.168.1.71:${this.port}`);
          console.log(`📱 WebSocket disponible sur ws://69.197.142.189:${this.port}`);
          resolve();
        }
      });
    });
  }

  stop() {
    return new Promise((resolve) => {
      this.wss.close(() => {
        this.server.close(() => {
          console.log('🛑 Serveur WebSocket natif arrêté');
          resolve();
        });
      });
    });
  }

  getStats() {
    return {
      connectedClients: this.clients.size,
      port: this.port
    };
  }
}

module.exports = NativeWebSocketServer;

