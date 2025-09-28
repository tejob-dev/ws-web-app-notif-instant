#!/bin/bash

# Script d'installation simple pour NotificationApp Kotlin
echo "📲 Installation de NotificationApp Kotlin..."

# Vérifier qu'un appareil est connecté
adb devices | grep -q "device$"
if [ $? -ne 0 ]; then
    echo "❌ Aucun appareil Android connecté"
    echo "Connectez un appareil Android ou démarrez un émulateur"
    exit 1
fi

# Installer l'APK
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
if [ -f "$APK_PATH" ]; then
    echo "📲 Installation de l'APK..."
    adb install -r "$APK_PATH"
    
    if [ $? -eq 0 ]; then
        echo "✅ APK installé avec succès"
        
        # Lancer l'application
        echo "🚀 Lancement de l'application..."
        adb shell am start -n com.notificationapp.kotlin/.MainActivity
        
        echo ""
        echo "📱 Application lancée !"
        echo "Pour voir les logs: ./debug.sh"
        echo "Pour tester: ./test.sh"
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
        
        if [ $? -eq 0 ]; then
            echo "✅ APK installé avec succès"
            
            # Lancer l'application
            echo "🚀 Lancement de l'application..."
            adb shell am start -n com.notificationapp.kotlin/.MainActivity
            
            echo ""
            echo "📱 Application lancée !"
            echo "Pour voir les logs: ./debug.sh"
            echo "Pour tester: ./test.sh"
        else
            echo "❌ Erreur lors de l'installation de l'APK"
            exit 1
        fi
    else
        echo "❌ Erreur lors du build"
        exit 1
    fi
fi