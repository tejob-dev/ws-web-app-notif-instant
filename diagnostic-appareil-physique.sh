#!/bin/bash

# Script de diagnostic pour appareil physique
# Vérifie la connectivité réseau pour l'APK sur appareil physique

echo "📱 Diagnostic pour appareil physique - APK"
echo "=========================================="

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

# Obtenir l'adresse IP réelle
echo "🌐 Adresse IP du serveur:"
SERVER_IP=$(ifconfig | grep "inet " | grep -v 127.0.0.1 | head -1 | awk '{print $2}')
echo "   $SERVER_IP"

# Vérifier l'écoute sur toutes les interfaces
echo "🔌 Ports en écoute:"
netstat -an | grep -E "(3001|3002)" | grep LISTEN

# Vérifier la connectivité réseau
echo "🌐 Test de connectivité réseau..."
if curl -s http://$SERVER_IP:3001/api/health > /dev/null; then
    echo "✅ Serveur backend accessible depuis le réseau ($SERVER_IP:3001)"
else
    echo "❌ Serveur backend non accessible depuis le réseau"
fi

if nc -z $SERVER_IP 3002; then
    echo "✅ Serveur WebSocket natif accessible depuis le réseau ($SERVER_IP:3002)"
else
    echo "❌ Serveur WebSocket natif non accessible depuis le réseau"
fi

# Vérifier les connexions actives
echo "🔗 Connexions actives sur le port 3002:"
netstat -an | grep 3002 | grep -v LISTEN

echo ""
echo "📱 Configuration pour appareil physique:"
echo "   serverUrl: '$SERVER_IP:3002'"
echo ""
echo "🔧 Vérifications à faire sur l'appareil mobile:"
echo "1. 📶 WiFi: L'appareil est-il sur le même réseau WiFi que le serveur ?"
echo "2. 🔒 Pare-feu: Y a-t-il un pare-feu qui bloque les connexions ?"
echo "3. 📱 Permissions: L'app a-t-elle les permissions réseau ?"
echo "4. 🌐 IP: L'adresse IP du serveur est-elle correcte ?"
echo ""
echo "🧪 Test de connexion WebSocket depuis le réseau..."
cd NotificationApp
node test-websocket-network.js

echo ""
echo "💡 Solutions possibles:"
echo "1. Vérifiez que l'appareil mobile est sur le même réseau WiFi"
echo "2. Désactivez temporairement le pare-feu pour tester"
echo "3. Vérifiez les permissions réseau de l'application"
echo "4. Testez avec une autre adresse IP si nécessaire"
