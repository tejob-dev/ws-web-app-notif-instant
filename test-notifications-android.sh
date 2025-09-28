#!/bin/bash

# Script de diagnostic pour les notifications Android
# Ce script teste la connectivité et les notifications

echo "🔍 DIAGNOSTIC DES NOTIFICATIONS ANDROID"
echo "========================================"

# Vérifier si l'appareil est connecté
echo "📱 Vérification de la connexion Android..."
adb devices

# Vérifier les logs de l'application
echo ""
echo "📋 Logs de l'application NotificationApp..."
adb logcat -s "NotificationService" "SocketIOService" "BackgroundService" "MainActivity" | head -50

echo ""
echo "🔔 Test de notification manuelle..."
# Envoyer une notification de test via ADB
adb shell am broadcast -a "com.notificationapp.kotlin.NOTIFICATION_RECEIVED" --es "message_content" "Test de notification depuis le script"

echo ""
echo "📊 Vérification des permissions..."
adb shell dumpsys notification | grep -A 10 "NotificationService"

echo ""
echo "🌐 Test de connectivité WebSocket..."
# Tester la connectivité au serveur
curl -I http://192.168.1.71:3001 2>/dev/null && echo "✅ Serveur Socket.IO accessible" || echo "❌ Serveur Socket.IO inaccessible"

echo ""
echo "📝 Instructions pour résoudre les problèmes:"
echo "1. Vérifiez que les permissions de notification sont accordées"
echo "2. Vérifiez que l'application n'est pas en mode économie d'énergie"
echo "3. Vérifiez que le service en arrière-plan est actif"
echo "4. Redémarrez l'application si nécessaire"
