#!/bin/bash

# Script de test pour la connexion automatique Socket.IO
# Ce script teste le lancement automatique de Socket.IO au démarrage de l'application

echo "🚀 TEST CONNEXION AUTOMATIQUE SOCKET.IO"
echo "======================================"

# Vérifier si l'appareil est connecté
echo "📱 Vérification de la connexion Android..."
adb devices

echo ""
echo "📊 Vérification de la version Android..."
adb shell getprop ro.build.version.sdk

echo ""
echo "🔄 Redémarrage de l'application pour tester la connexion automatique..."
# Fermer l'application si elle est ouverte
adb shell am force-stop com.notificationapp.kotlin

# Attendre un peu
sleep 2

# Lancer l'application
adb shell am start -n com.notificationapp.kotlin/.MainActivity

echo ""
echo "📋 Surveillance des logs de démarrage (15 secondes)..."
echo "Recherche des logs de connexion Socket.IO..."

# Surveiller les logs pendant 15 secondes
timeout 15s adb logcat -s "MainActivity" "SocketIOService" | grep -E "(Connexion|Socket|Connect|Initialisation)" &

echo ""
echo "🧪 Test d'envoi de message après connexion..."
# Attendre que l'application se lance
sleep 5

# Envoyer un message de test via l'API
curl -X POST "http://192.168.1.71:3002/api/send-message" \
  -H "Content-Type: application/json" \
  -d '{"content": "Test connexion automatique Socket.IO", "type": "test"}' \
  --connect-timeout 10

if [ $? -eq 0 ]; then
    echo "✅ Message de test envoyé"
else
    echo "❌ Échec de l'envoi du message de test"
fi

echo ""
echo "📊 Surveillance des logs de réception (10 secondes)..."
timeout 10s adb logcat -s "MainActivity" "SocketIOService" "NotificationService" | grep -E "(Message|Notification|Reçu)" &

echo ""
echo "🔍 Vérification du statut de connexion..."
adb shell dumpsys activity com.notificationapp.kotlin | grep -A 5 "MainActivity"

echo ""
echo "📱 Instructions pour vérifier manuellement:"
echo "1. Ouvrir l'application NotificationApp"
echo "2. Vérifier que le statut affiche 'Connecté'"
echo "3. Vérifier qu'un message de test apparaît dans la liste"
echo "4. Vérifier qu'une notification popup s'affiche"

echo ""
echo "🔧 Si la connexion automatique ne fonctionne pas:"
echo "1. Vérifier les logs pour des erreurs de connexion"
echo "2. Vérifier la connectivité réseau"
echo "3. Vérifier que le serveur Socket.IO est accessible"
echo "4. Utiliser le bouton de reconnexion manuelle"

echo ""
echo "📋 Logs finaux (5 secondes)..."
timeout 5s adb logcat -s "MainActivity" "SocketIOService" | grep -E "(Connect|Error|Success|Status)"
