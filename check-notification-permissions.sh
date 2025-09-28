#!/bin/bash

# Script pour vérifier et résoudre les problèmes de permissions de notification
# Ce script aide à diagnostiquer et résoudre les problèmes d'initialisation

echo "🔍 DIAGNOSTIC DES PERMISSIONS DE NOTIFICATION"
echo "============================================="

# Vérifier si l'appareil est connecté
echo "📱 Vérification de la connexion Android..."
adb devices

echo ""
echo "📋 Vérification des permissions de notification..."
adb shell dumpsys notification | grep -A 10 "NotificationApp"

echo ""
echo "🔍 Vérification des paramètres de notification..."
adb shell settings get global notification_listeners

echo ""
echo "📊 Test de notification manuelle..."
# Tester si les notifications fonctionnent
adb shell am broadcast -a "com.notificationapp.kotlin.NOTIFICATION_RECEIVED" \
  --es "message_content" "Test de permission de notification"

echo ""
echo "📱 Instructions pour activer les notifications:"
echo "1. Ouvrez les Paramètres Android"
echo "2. Allez dans Applications > NotificationApp"
echo "3. Cliquez sur Notifications"
echo "4. Activez 'Autoriser les notifications'"
echo "5. Activez 'Son' et 'Vibration' si souhaité"
echo "6. Redémarrez l'application"

echo ""
echo "🔧 Pour Android 13+ (API 33+):"
echo "1. Ouvrez l'application NotificationApp"
echo "2. Une popup devrait demander l'autorisation"
echo "3. Cliquez sur 'Autoriser'"
echo "4. Si pas de popup, allez dans Paramètres > Applications > NotificationApp > Notifications"

echo ""
echo "📋 Vérification des logs d'initialisation..."
adb logcat -s "NotificationService" | grep -E "(Initialisation|Permission|Error)" | tail -10

echo ""
echo "🧪 Test d'envoi de notification..."
# Envoyer une notification de test via l'API
curl -X POST "http://192.168.1.71:3002/api/send-message" \
  -H "Content-Type: application/json" \
  -d '{"content": "Test de permission de notification", "type": "test"}' \
  --connect-timeout 10

if [ $? -eq 0 ]; then
    echo "✅ Message de test envoyé"
else
    echo "❌ Échec de l'envoi du message de test"
fi

echo ""
echo "📊 Surveillance des logs (5 secondes)..."
timeout 5s adb logcat -s "NotificationService" "SocketIOService" | grep -E "(Notification|Permission|Error|Success)"
