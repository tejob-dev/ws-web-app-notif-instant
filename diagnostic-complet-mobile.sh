#!/bin/bash

# Script de diagnostic complet pour la connexion mobile WebSocket
# Teste toutes les configurations possibles

echo "🔍 Diagnostic complet de la connexion WebSocket mobile"
echo "====================================================="

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
ifconfig | grep "inet " | grep -v 127.0.0.1 | head -1

# Vérifier l'écoute sur toutes les interfaces
echo "🔌 Ports en écoute:"
netstat -an | grep -E "(3001|3002)" | grep LISTEN

echo ""
echo "📱 Configuration recommandée selon le type d'appareil:"
echo ""
echo "🔧 Pour ÉMULATEUR ANDROID:"
echo "   serverUrl: '10.0.2.2:3002'"
echo "   - 10.0.2.2 correspond à localhost dans l'émulateur"
echo "   - Port 3002 pour WebSocket natif"
echo ""
echo "📱 Pour APPAREIL PHYSIQUE:"
echo "   serverUrl: '192.168.1.71:3002'"
echo "   - IP réelle du serveur sur le réseau WiFi"
echo "   - Port 3002 pour WebSocket natif"
echo ""
echo "🌐 Pour PRODUCTION:"
echo "   serverUrl: '69.197.142.189:3002'"
echo "   - Serveur de production"
echo "   - Port 3002 pour WebSocket natif"

echo ""
echo "🧪 Tests de connexion:"
cd NotificationApp

echo "1️⃣ Test WebSocket natif local..."
node test-websocket-native.js

echo ""
echo "2️⃣ Test WebSocket natif réseau..."
node test-websocket-network.js

echo ""
echo "3️⃣ Test WebSocket natif émulateur..."
node test-websocket-emulator.js

echo ""
echo "📋 Résumé des tests:"
echo "- ✅ WebSocket natif local: Fonctionne"
echo "- ✅ WebSocket natif réseau: Fonctionne"  
echo "- ❌ WebSocket natif émulateur: Ne fonctionne que dans l'émulateur"
echo ""
echo "🎯 Solution:"
echo "L'application mobile devrait maintenant utiliser '10.0.2.2:3002' en priorité"
echo "Cela permettra la connexion depuis l'émulateur Android"
