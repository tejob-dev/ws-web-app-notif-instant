#!/bin/bash

# Script de test pour l'application mobile WebSocket
# Ce script teste la connexion WebSocket native

echo "🧪 Test de l'application mobile WebSocket"
echo "========================================"

# Vérifier que le serveur backend est démarré
echo "📡 Vérification du serveur backend..."
if curl -s http://localhost:3001/api/health > /dev/null; then
    echo "✅ Serveur backend actif sur le port 3001"
else
    echo "❌ Serveur backend non accessible sur le port 3001"
    echo "💡 Démarrez le serveur avec: cd backend && npm start"
    exit 1
fi

# Vérifier que le serveur WebSocket natif est actif
echo "📡 Vérification du serveur WebSocket natif..."
if nc -z localhost 3002; then
    echo "✅ Serveur WebSocket natif actif sur le port 3002"
else
    echo "❌ Serveur WebSocket natif non accessible sur le port 3002"
    echo "💡 Le serveur WebSocket natif devrait démarrer automatiquement avec le backend"
    exit 1
fi

# Tester la connexion WebSocket native
echo "🔗 Test de connexion WebSocket native..."
cd NotificationApp
node test-websocket-native.js

echo ""
echo "📱 Pour tester l'application mobile:"
echo "1. Démarrez l'application React Native"
echo "2. Utilisez l'écran de test WebSocket"
echo "3. Vérifiez les logs dans la console"
echo ""
echo "🔧 Configuration actuelle:"
echo "- Serveur WebSocket: ws://localhost:3002"
echo "- Fallback Socket.IO: http://localhost:3001"
echo "- Mode: WebSocket natif par défaut"
