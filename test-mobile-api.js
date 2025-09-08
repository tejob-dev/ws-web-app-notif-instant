const axios = require('axios');

const API_BASE = 'http://localhost:3001/api';

async function testMobileAPI() {
  console.log('📱 Test de l\'API pour les notifications mobiles...\n');

  try {
    // Test de santé
    console.log('1. Test de santé...');
    const healthResponse = await axios.get(`${API_BASE}/health`);
    console.log('✅ Santé:', healthResponse.data);

    // Test des statistiques
    console.log('\n2. Test des statistiques...');
    const statsResponse = await axios.get(`${API_BASE}/stats`);
    console.log('✅ Statistiques:', statsResponse.data);

    // Test d'enregistrement d'un token mobile (simulation)
    console.log('\n3. Test d\'enregistrement de token mobile...');
    const tokenResponse = await axios.post(`${API_BASE}/register-mobile-token`, {
      token: 'test-token-12345',
      platform: 'android',
      deviceId: 'test-device-001'
    });
    console.log('✅ Token mobile enregistré:', tokenResponse.data);

    // Test d'envoi de message
    console.log('\n4. Test d\'envoi de message...');
    const messageResponse = await axios.post(`${API_BASE}/send-message`, {
      content: 'Test de notification mobile depuis l\'API',
      type: 'info'
    });
    console.log('✅ Message envoyé:', messageResponse.data);

    // Test de récupération des messages
    console.log('\n5. Test de récupération des messages...');
    const messagesResponse = await axios.get(`${API_BASE}/messages?limit=5`);
    console.log('✅ Messages récupérés:', messagesResponse.data.length, 'messages');

    // Test de clé VAPID
    console.log('\n6. Test de clé VAPID...');
    const vapidResponse = await axios.get(`${API_BASE}/vapid-public-key`);
    console.log('✅ Clé VAPID:', vapidResponse.data.publicKey ? 'Présente' : 'Absente');

    console.log('\n🎉 Tous les tests sont passés avec succès !');
    console.log('\n📱 Pour tester avec l\'application mobile :');
    console.log('1. Démarrez l\'application mobile (npm run android)');
    console.log('2. Vérifiez que le token FCM est enregistré');
    console.log('3. Envoyez un message depuis l\'interface web');
    console.log('4. Vérifiez que la notification apparaît sur le mobile');

  } catch (error) {
    console.error('❌ Erreur lors du test:', error.message);
    if (error.response) {
      console.error('Détails:', error.response.data);
    }
  }
}

// Attendre que le serveur soit prêt
setTimeout(testMobileAPI, 3000);
