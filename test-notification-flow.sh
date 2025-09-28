#!/bin/bash

# Script de test pour vérifier le flux de notifications Android
# Ce script envoie un message de test et vérifie les logs

echo "🧪 TEST DU FLUX DE NOTIFICATIONS ANDROID"
echo "========================================"

# Configuration
SERVER_URL="http://192.168.1.71:3002"
TEST_MESSAGE="Test de notification depuis le script - $(date)"

echo "📱 Vérification de la connexion Android..."
adb devices

echo ""
echo "📤 Envoi d'un message de test..."
echo "Message: $TEST_MESSAGE"

# Envoyer un message de test via l'API
curl -X POST "$SERVER_URL/api/send-message" \
  -H "Content-Type: application/json" \
  -d "{\"content\": \"$TEST_MESSAGE\", \"type\": \"test\"}" \
  --connect-timeout 10

if [ $? -eq 0 ]; then
    echo "✅ Message envoyé avec succès"
else
    echo "❌ Échec de l'envoi du message"
fi

echo ""
echo "📋 Surveillance des logs Android (10 secondes)..."
echo "Recherche des logs de notification..."

# Surveiller les logs pendant 10 secondes
timeout 10s adb logcat -s "NotificationService" "SocketIOService" | grep -E "(Message|Notification|Error|Success)" &

echo ""
echo "🔍 Vérification des permissions de notification..."
adb shell dumpsys notification | grep -A 5 "NotificationService"

echo ""
echo "📊 Instructions pour vérifier manuellement:"
echo "1. Vérifiez que la notification apparaît sur l'écran"
echo "2. Vérifiez que le contenu du message est visible"
echo "3. Vérifiez les logs pour confirmer le traitement"
echo ""
echo "🔧 Si aucun problème n'est détecté:"
echo "- Vérifiez les paramètres de notification Android"
echo "- Redémarrez l'application"
echo "- Vérifiez la connexion réseau"
