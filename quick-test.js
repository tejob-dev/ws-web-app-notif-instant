// Test rapide Socket.IO
const { io } = require('socket.io-client');

console.log('🔍 Test Socket.IO depuis le serveur...');

const socket = io('http://192.168.1.71:3001', {
  timeout: 5000,
  transports: ['websocket', 'polling']
});

socket.on('connect', () => {
  console.log('✅ Socket.IO connecté avec succès !');
  console.log('   Socket ID:', socket.id);
  console.log('   Transport:', socket.io.engine.transport.name);
  socket.disconnect();
  process.exit(0);
});

socket.on('connect_error', (error) => {
  console.log('❌ Erreur Socket.IO:', error.message);
  process.exit(1);
});

setTimeout(() => {
  console.log('❌ Timeout - Socket.IO ne répond pas');
  process.exit(1);
}, 10000);
