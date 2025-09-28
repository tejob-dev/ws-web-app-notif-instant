#!/bin/bash

# Script de test basé sur le projet WebSocket-Kotlin-Demo
echo "🔍 Test de connectivité WebSocket basé sur le projet de référence..."

# Vérifier qu'un appareil est connecté
adb devices | grep -q "device$"
if [ $? -ne 0 ]; then
    echo "❌ Aucun appareil Android connecté"
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
    echo "❌ APK non trouvé"
    exit 1
fi

# Effacer les logs précédents
echo "🧹 Nettoyage des logs précédents..."
adb logcat -c

# Lancer l'application
echo "🚀 Lancement de l'application..."
adb shell am start -n com.notificationapp.kotlin/.MainActivity

# Attendre un peu pour que l'app se lance
sleep 3

echo ""
echo "📋 Test de connectivité WebSocket en cours (Ctrl+C pour arrêter):"
echo "=============================================================="
echo "Basé sur le projet WebSocket-Kotlin-Demo de puskal-khadka"
echo ""
echo "✅ URLs testées (dans l'ordre):"
echo "   1. ws://192.168.1.71:3002 (votre serveur local)"
echo "   2. ws://10.0.2.2:3002 (émulateur Android)"
echo "   3. ws://localhost:3002 (localhost)"
echo "   4. ws://127.0.0.1:3002 (localhost alternatif)"
echo "   5. ws://69.197.142.189:3002 (serveur de production)"
echo ""
echo "📋 Logs attendus:"
echo "   - Tentative de connexion WebSocket: ws://[url]"
echo "   - WebSocket ouvert avec succès vers [url]"
echo "   - Connexion WebSocket réussie: [url]"
echo ""

# Afficher les logs filtrés pour la connectivité WebSocket
adb logcat | grep -E "(WebSocketService|Tentative de connexion WebSocket|WebSocket ouvert avec succès|Connexion WebSocket réussie|Échec WebSocket|BackgroundService|MainActivity)"
