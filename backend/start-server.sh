#!/bin/bash

# Script de démarrage du serveur backend avec WebSocket natif
echo "🚀 Démarrage du serveur backend avec WebSocket natif..."

# Vérifier que Node.js est installé
if ! command -v node &> /dev/null; then
    echo "❌ Node.js n'est pas installé"
    exit 1
fi

# Vérifier que npm est installé
if ! command -v npm &> /dev/null; then
    echo "❌ npm n'est pas installé"
    exit 1
fi

# Installer les dépendances si nécessaire
if [ ! -d "node_modules" ]; then
    echo "📦 Installation des dépendances..."
    npm install
fi

# Vérifier que la dépendance WebSocket est installée
if ! npm list ws &> /dev/null; then
    echo "📦 Installation de la dépendance WebSocket..."
    npm install ws
fi

# Démarrer le serveur
echo "🚀 Démarrage du serveur..."
echo "📱 Socket.IO sur le port 3001"
echo "📱 WebSocket natif sur le port 5023"
echo "🌐 API REST sur le port 3001"
echo ""
echo "Pour tester le WebSocket natif:"
echo "  node test-websocket-server.js"
echo ""
echo "Pour arrêter le serveur: Ctrl+C"
echo ""

node server.js

