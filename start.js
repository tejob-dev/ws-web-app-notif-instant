const { spawn } = require('child_process');
const path = require('path');

console.log('🚀 Démarrage de l\'application de notifications instantanées...\n');

// Démarrer le backend
console.log('📡 Démarrage du backend...');
const backend = spawn('npm', ['run', 'backend'], {
  cwd: __dirname,
  stdio: 'inherit',
  shell: true
});

// Attendre un peu puis démarrer le frontend
setTimeout(() => {
  console.log('🌐 Démarrage du frontend...');
  const frontend = spawn('npm', ['run', 'dev'], {
    cwd: __dirname,
    stdio: 'inherit',
    shell: true
  });

  // Gestion des erreurs
  frontend.on('error', (err) => {
    console.error('Erreur frontend:', err);
  });

  backend.on('error', (err) => {
    console.error('Erreur backend:', err);
  });

  // Gestion de l'arrêt propre
  process.on('SIGINT', () => {
    console.log('\n🛑 Arrêt de l\'application...');
    backend.kill();
    frontend.kill();
    process.exit(0);
  });

}, 2000);

console.log('✅ Application démarrée !');
console.log('📱 Frontend: http://localhost:3000');
console.log('🔧 Backend: http://localhost:3001');
console.log('📱 Application mobile: Voir mobile/README.md');
console.log('💡 Appuyez sur Ctrl+C pour arrêter\n');
