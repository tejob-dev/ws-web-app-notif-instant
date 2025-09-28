/**
 * Script de test pour le serveur WebSocket natif
 */

const WebSocket = require('ws');

async function testWebSocketServer() {
  console.log('🧪 Test du serveur WebSocket natif...\n');
  
  const urls = [
    'ws://localhost:5023',
    'ws://192.168.1.71:5023',
    'ws://69.197.142.189:5023'
  ];
  
  for (const url of urls) {
    console.log(`🔍 Test de connexion à: ${url}`);
    
    try {
      await testConnection(url);
      console.log(`✅ Connexion réussie à ${url}\n`);
    } catch (error) {
      console.log(`❌ Échec de connexion à ${url}: ${error.message}\n`);
    }
  }
}

function testConnection(url) {
  return new Promise((resolve, reject) => {
    const ws = new WebSocket(url);
    
    const timeout = setTimeout(() => {
      ws.close();
      reject(new Error('Timeout de connexion'));
    }, 10000);
    
    ws.on('open', () => {
      clearTimeout(timeout);
      console.log('  📡 Connexion établie');
      
      // Envoyer un message de test
      const testMessage = {
        type: 'ping',
        timestamp: new Date().toISOString()
      };
      
      ws.send(JSON.stringify(testMessage));
      console.log('  📤 Message ping envoyé');
    });
    
    ws.on('message', (data) => {
      try {
        const message = JSON.parse(data.toString());
        console.log('  📨 Message reçu:', message);
        
        if (message.type === 'pong') {
          console.log('  ✅ Pong reçu - Serveur WebSocket fonctionnel');
          ws.close();
          resolve();
        } else {
          console.log('  📝 Message de bienvenue reçu');
        }
      } catch (error) {
        console.log('  ❌ Erreur parsing message:', error.message);
      }
    });
    
    ws.on('error', (error) => {
      clearTimeout(timeout);
      reject(new Error(`Erreur de connexion: ${error.message}`));
    });
    
    ws.on('close', (code, reason) => {
      if (code === 1000) {
        console.log('  🔌 Connexion fermée proprement');
      } else {
        clearTimeout(timeout);
        reject(new Error(`Connexion fermée: ${code} ${reason}`));
      }
    });
  });
}

// Exécuter les tests
testWebSocketServer()
  .then(() => {
    console.log('🎉 Tests terminés avec succès');
    process.exit(0);
  })
  .catch((error) => {
    console.error('❌ Erreur lors des tests:', error);
    process.exit(1);
  });

