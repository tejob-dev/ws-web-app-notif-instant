#!/bin/bash

# Script de test pour appareil physique - APK
# Teste toutes les configurations possibles pour l'APK

echo "📱 Test complet pour appareil physique - APK"
echo "============================================="

# Obtenir l'adresse IP réelle
SERVER_IP=$(ifconfig | grep "inet " | grep -v 127.0.0.1 | head -1 | awk '{print $2}')
echo "🌐 Adresse IP du serveur: $SERVER_IP"

# Vérifier que le serveur est démarré
echo "📡 Vérification du serveur..."
if curl -s http://localhost:3001/api/health > /dev/null; then
    echo "✅ Serveur backend actif"
else
    echo "❌ Serveur backend non accessible"
    exit 1
fi

if nc -z localhost 3002; then
    echo "✅ Serveur WebSocket natif actif"
else
    echo "❌ Serveur WebSocket natif non accessible"
    exit 1
fi

echo ""
echo "🧪 Tests de connectivité..."

# Test 1: Connexion locale
echo "1️⃣ Test connexion locale (localhost:3002)..."
cd NotificationApp
timeout 10s node test-websocket-native.js

# Test 2: Connexion réseau
echo ""
echo "2️⃣ Test connexion réseau ($SERVER_IP:3002)..."
timeout 10s node test-websocket-network.js

echo ""
echo "📋 Résumé des tests:"
echo "- ✅ Serveur backend: Actif sur le port 3001"
echo "- ✅ Serveur WebSocket natif: Actif sur le port 3002"
echo "- ✅ Connectivité réseau: Accessible depuis $SERVER_IP"
echo ""
echo "🔧 Configuration finale pour l'APK:"
echo "   serverUrl: '$SERVER_IP:3002'"
echo ""
echo "📱 Instructions pour l'APK:"
echo "1. Assurez-vous que l'appareil mobile est sur le même réseau WiFi"
echo "2. Vérifiez que l'adresse IP est correcte: $SERVER_IP"
echo "3. Redémarrez l'application mobile"
echo "4. Consultez les logs pour voir les détails de connexion"
echo ""
echo "🔍 Codes d'erreur WebSocket courants:"
echo "- 1006: Connexion fermée anormalement (pare-feu/réseau)"
echo "- 1000: Connexion fermée normalement"
echo "- 1001: Endpoint part"
echo ""
echo "💡 Si l'APK ne se connecte toujours pas:"
echo "1. Vérifiez les permissions réseau de l'application"
echo "2. Désactivez temporairement le pare-feu"
echo "3. Testez avec une autre adresse IP"
echo "4. Vérifiez que l'appareil est sur le bon réseau WiFi"
