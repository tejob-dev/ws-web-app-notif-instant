#!/bin/bash

echo "🚀 Déploiement de l'application de notifications instantanées..."

# Vérifier si PM2 est installé
if ! command -v pm2 &> /dev/null; then
    echo "❌ PM2 n'est pas installé. Installation..."
    npm install -g pm2
fi

# Installer les dépendances
echo "📦 Installation des dépendances..."
npm run install:all

# Construire l'application
echo "🔨 Construction de l'application..."
npm run build

# Créer le dossier de logs
mkdir -p logs

# Démarrer avec PM2
echo "🚀 Démarrage avec PM2..."
pm2 start ecosystem.config.js

# Sauvegarder la configuration PM2
pm2 save

# Configurer le démarrage automatique
pm2 startup

echo "✅ Déploiement terminé !"
echo "📱 Frontend: http://localhost:3000"
echo "🔧 Backend: http://localhost:3001"
echo "📊 PM2 Status: pm2 status"
echo "📋 Logs: pm2 logs"
