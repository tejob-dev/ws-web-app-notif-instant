#!/bin/bash

# Script de test pour NotificationApp Kotlin
echo "🧪 Test de NotificationApp Kotlin..."

# Vérifier qu'un appareil est connecté
adb devices | grep -q "device$"
if [ $? -ne 0 ]; then
    echo "❌ Aucun appareil Android connecté"
    exit 1
fi

echo "📱 Appareil connecté détecté"

# Installer l'APK si nécessaire
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
if [ -f "$APK_PATH" ]; then
    echo "📲 Installation de l'APK..."
    adb install -r "$APK_PATH"
    
    if [ $? -eq 0 ]; then
        echo "✅ APK installé avec succès"
    else
        echo "❌ Erreur lors de l'installation de l'APK"
        exit 1
    fi
else
    echo "❌ APK non trouvé. Build en cours..."
    ./gradlew assembleDebug
    
    if [ $? -eq 0 ]; then
        echo "📲 Installation de l'APK..."
        adb install -r "$APK_PATH"
    else
        echo "❌ Erreur lors du build"
        exit 1
    fi
fi

# Lancer l'application
echo "🚀 Lancement de l'application..."
adb shell am start -n com.notificationapp.kotlin/.MainActivity

# Attendre un peu pour que l'app se lance
sleep 3

echo "📋 Instructions de test:"
echo "1. Accordez les permissions de notification si demandées"
echo "2. Appuyez sur 'Démarrer le service'"
echo "3. Vérifiez que le statut passe à 'Connecté'"
echo "4. Testez l'envoi de messages via votre serveur WebSocket"
echo "5. Vérifiez que les notifications apparaissent"
echo "6. Vérifiez que les messages s'affichent dans la liste"

echo ""
echo "🔍 Pour voir les logs en temps réel:"
echo "adb logcat | grep -E '(NotificationApp|WebSocket|Notification)'"

echo ""
echo "📱 Pour tester manuellement:"
echo "1. Ouvrez l'application"
echo "2. Démarrez le service"
echo "3. Envoyez des messages via votre serveur WebSocket vers l'IP de l'appareil:3002"
echo "4. Vérifiez les notifications et la liste des messages"

echo ""
echo "✅ Test terminé !"
