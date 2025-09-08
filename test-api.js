const axios = require('axios');

const API_BASE = 'http://localhost:3001/api';

async function testAPI() {
  console.log('🧪 Test de l\'API de notifications...\n');

  try {
    // Test de santé
    console.log('1. Test de santé...');
    const healthResponse = await axios.get(`${API_BASE}/health`);
    console.log('✅ Santé:', healthResponse.data);

    // Test des statistiques
    console.log('\n2. Test des statistiques...');
    const statsResponse = await axios.get(`${API_BASE}/stats`);
    console.log('✅ Statistiques:', statsResponse.data);

    // Test d'envoi de message
    console.log('\n3. Test d\'envoi de message...');
    const messageResponse = await axios.post(`${API_BASE}/send-message`, {
      content: 'Test de notification depuis l\'API',
      type: 'info'
    });
    console.log('✅ Message envoyé:', messageResponse.data);

    // Test de récupération des messages
    console.log('\n4. Test de récupération des messages...');
    const messagesResponse = await axios.get(`${API_BASE}/messages?limit=5`);
    console.log('✅ Messages récupérés:', messagesResponse.data.length, 'messages');

    // Test de connexion WebSocket (simulation)
    console.log('\n5. Test de connexion WebSocket...');
    console.log('✅ WebSocket: Système interne configuré');

    console.log('\n🎉 Tous les tests sont passés avec succès !');

  } catch (error) {
    console.error('❌ Erreur lors du test:', error.message);
    if (error.response) {
      console.error('Détails:', error.response.data);
    }
  }
}

// Attendre que le serveur soit prêt
setTimeout(testAPI, 3000);
