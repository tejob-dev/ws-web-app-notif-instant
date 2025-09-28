#!/bin/bash

# Script de diagnostic pour la connexion mobile WebSocket
# Vérifie tous les aspects de la connexion

echo "🔍 Diagnostic de la connexion WebSocket mobile"
echo "=============================================="

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
    exit 1
fi

# Vérifier l'accès réseau
echo "🌐 Vérification de l'accès réseau..."
if nc -z 192.168.1.71 3001; then
    echo "✅ Serveur backend accessible depuis le réseau (192.168.1.71:3001)"
else
    echo "❌ Serveur backend non accessible depuis le réseau"
fi

if nc -z 192.168.1.71 3002; then
    echo "✅ Serveur WebSocket natif accessible depuis le réseau (192.168.1.71:3002)"
else
    echo "❌ Serveur WebSocket natif non accessible depuis le réseau"
fi

# Tester la connexion WebSocket natif local
echo "🔗 Test de connexion WebSocket natif local..."
cd NotificationApp
node test-websocket-native.js

echo ""
echo "🔗 Test de connexion WebSocket natif réseau..."
node test-websocket-network.js

echo ""
echo "📱 Configuration actuelle de l'application mobile:"
echo "- Serveur principal: 192.168.1.71:3002 (WebSocket natif)"
echo "- Fallback Socket.IO: 192.168.1.71:3001"
echo "- URLs de test: localhost:3002, 192.168.1.71:3002"
echo ""
echo "🔧 Si l'application mobile ne se connecte toujours pas:"
echo "1. Vérifiez que l'appareil mobile est sur le même réseau WiFi"
echo "2. Vérifiez l'adresse IP du serveur: ipconfig (Windows) ou ifconfig (Mac/Linux)"
echo "3. Redémarrez l'application mobile avec cache nettoyé"
echo "4. Consultez les logs de l'application mobile"
