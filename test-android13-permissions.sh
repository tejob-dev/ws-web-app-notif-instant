#!/bin/bash

# Script de test pour Android 13+ (API 33+) - Permission POST_NOTIFICATIONS
# Ce script teste spécifiquement la gestion de la permission POST_NOTIFICATIONS

echo "🔐 TEST PERMISSION POST_NOTIFICATIONS (Android 13+)"
echo "=================================================="

# Vérifier si l'appareil est connecté
echo "📱 Vérification de la connexion Android..."
adb devices

echo ""
echo "📊 Vérification de la version Android..."
adb shell getprop ro.build.version.sdk

echo ""
echo "🔍 Vérification des permissions de notification..."
adb shell dumpsys notification | grep -A 10 "NotificationApp"

echo ""
echo "📋 Vérification spécifique POST_NOTIFICATIONS..."
adb shell pm list permissions | grep POST_NOTIFICATIONS

echo ""
echo "🧪 Test de notification avec permission..."
# Envoyer une notification de test
adb shell am broadcast -a "com.notificationapp.kotlin.NOTIFICATION_RECEIVED" \
  --es "message_content" "Test permission POST_NOTIFICATIONS"

echo ""
echo "📊 Surveillance des logs (10 secondes)..."
echo "Recherche des logs de permission..."

# Surveiller les logs pendant 10 secondes
timeout 10s adb logcat -s "MainActivity" "NotificationService" | grep -E "(Permission|POST_NOTIFICATIONS|API)" &

echo ""
echo "🔧 Instructions pour Android 13+ (API 33+):"
echo "1. Ouvrir l'application NotificationApp"
echo "2. Une popup devrait demander la permission POST_NOTIFICATIONS"
echo "3. Cliquer sur 'Autoriser'"
echo "4. Si pas de popup, aller dans Paramètres > Applications > NotificationApp > Notifications"
echo "5. Vérifier que 'Autoriser les notifications' est activé"

echo ""
echo "📱 Test manuel de permission:"
echo "1. Désinstaller l'application: adb uninstall com.notificationapp.kotlin"
echo "2. Réinstaller l'application"
echo "3. Ouvrir l'application et vérifier la popup de permission"
echo "4. Accorder la permission POST_NOTIFICATIONS"

echo ""
echo "🔍 Vérification des logs d'initialisation..."
adb logcat -s "MainActivity" "NotificationService" | grep -E "(Permission|API|Initialisation)" | tail -15

echo ""
echo "📊 Test d'envoi de message..."
# Envoyer un message de test via l'API
curl -X POST "http://192.168.1.71:3002/api/send-message" \
  -H "Content-Type: application/json" \
  -d '{"content": "Test permission POST_NOTIFICATIONS Android 13+", "type": "test"}' \
  --connect-timeout 10

if [ $? -eq 0 ]; then
    echo "✅ Message de test envoyé"
else
    echo "❌ Échec de l'envoi du message de test"
fi

echo ""
echo "📋 Logs finaux (5 secondes)..."
timeout 5s adb logcat -s "NotificationService" "SocketIOService" | grep -E "(Permission|Notification|Error|Success)"
