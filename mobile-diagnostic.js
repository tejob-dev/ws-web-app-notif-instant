// Script de diagnostic avancé pour les problèmes de connexion mobile
const { io } = require('socket.io-client');

console.log('🔍 Diagnostic avancé de connectivité mobile...\n');

// URLs à tester (comme dans l'app mobile)
const urls = [
  '192.168.1.71:3001',
  'localhost:3001', 
  '10.0.2.2:3001'
];

// Configuration comme dans l'app mobile
const socketConfig = {
  transports: ['websocket', 'polling'],
  timeout: 10000,
  forceNew: true,
  reconnection: true,
  reconnectionDelay: 5000,
  reconnectionAttempts: 3,
  upgrade: true,
  rememberUpgrade: false
};

async function testConnection(url) {
  return new Promise((resolve) => {
    console.log(`📡 Test: http://${url}`);
    
    const socket = io(`http://${url}`, socketConfig);
    
    const timeout = setTimeout(() => {
      socket.disconnect();
      resolve({
        url,
        success: false,
        error: 'Timeout (10s)',
        transport: 'none'
      });
    }, 10000);

    socket.on('connect', () => {
      clearTimeout(timeout);
      const transport = socket.io.engine.transport.name;
      console.log(`✅ Connecté: ${url} (${transport})`);
      socket.disconnect();
      resolve({
        url,
        success: true,
        transport,
        socketId: socket.id
      });
    });

    socket.on('connect_error', (error) => {
      clearTimeout(timeout);
      console.log(`❌ Erreur: ${url} - ${error.message}`);
      socket.disconnect();
      resolve({
        url,
        success: false,
        error: error.message,
        transport: 'none'
      });
    });

    socket.on('disconnect', (reason) => {
      console.log(`🔌 Déconnecté: ${reason}`);
    });
  });
}

async function runDiagnostic() {
  console.log('🚀 Démarrage du diagnostic...\n');
  
  const results = await Promise.all(urls.map(testConnection));
  
  console.log('\n' + '='.repeat(60));
  console.log('📊 RÉSULTATS DU DIAGNOSTIC:');
  console.log('='.repeat(60));
  
  results.forEach(result => {
    const status = result.success ? '✅' : '❌';
    console.log(`${status} ${result.url}`);
    if (result.success) {
      console.log(`   Transport: ${result.transport}`);
      console.log(`   Socket ID: ${result.socketId}`);
    } else {
      console.log(`   Erreur: ${result.error}`);
    }
    console.log('');
  });
  
  const successCount = results.filter(r => r.success).length;
  
  console.log('='.repeat(60));
  console.log('💡 ANALYSE:');
  console.log('='.repeat(60));
  
  if (successCount === 0) {
    console.log('❌ Aucune connexion réussie');
    console.log('🔧 Solutions possibles:');
    console.log('   1. Vérifiez que le serveur backend est démarré');
    console.log('   2. Vérifiez les permissions réseau Android');
    console.log('   3. Vérifiez la configuration CORS du serveur');
    console.log('   4. Testez avec un appareil physique (pas émulateur)');
  } else if (successCount === 1) {
    const workingUrl = results.find(r => r.success);
    console.log(`✅ Connexion réussie sur: ${workingUrl.url}`);
    console.log(`📱 Pour votre app mobile, utilisez: http://${workingUrl.url}`);
  } else {
    console.log('✅ Plusieurs connexions réussies');
    console.log('📱 URLs recommandées pour votre app mobile:');
    results.filter(r => r.success).forEach(result => {
      console.log(`   - http://${result.url} (${result.transport})`);
    });
  }
  
  console.log('\n🔧 Configuration Socket.IO recommandée:');
  console.log(JSON.stringify(socketConfig, null, 2));
}

runDiagnostic().catch(console.error);
