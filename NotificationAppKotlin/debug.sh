#!/bin/bash

# Script de test et diagnostic pour NotificationApp Kotlin
echo "🧪 Test et diagnostic de NotificationApp Kotlin..."

# Vérifier qu'un appareil est connecté
adb devices | grep -q "device$"
if [ $? -ne 0 ]; then
    echo "❌ Aucun appareil Android connecté"
    echo "Connectez un appareil Android ou démarrez un émulateur"
    exit 1
fi

echo "📱 Appareil connecté détecté"

# Installer l'APK
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

# Effacer les logs précédents
echo "🧹 Nettoyage des logs précédents..."
adb logcat -c

# Lancer l'application
echo "🚀 Lancement de l'application..."
adb shell am start -n com.notificationapp.kotlin/.MainActivity

# Attendre un peu pour que l'app se lance
sleep 2

echo ""
echo "📋 Logs en temps réel (Ctrl+C pour arrêter):"
echo "=============================================="

# Afficher les logs filtrés
adb logcat | grep -E "(MainActivity|BackgroundService|NotificationService|WebSocketService|NotificationApp)"