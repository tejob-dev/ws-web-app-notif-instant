#!/bin/bash

# Script de test basé sur le projet Kotlin-Websocket avec PieSocket
echo "🔍 Test WebSocket avec PieSocket (basé sur le projet Kotlin-Websocket)..."

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
echo "📋 Test WebSocket avec PieSocket en cours (Ctrl+C pour arrêter):"
echo "=============================================================="
echo "Basé sur le projet Kotlin-Websocket de Fannan-Fauzan-Fardhurohman"
echo ""
echo "🌐 Serveurs de test disponibles:"
echo "   1. PieSocket (https://www.piesocket.com/) - Serveur de test public"
echo "   2. Votre serveur local (192.168.1.71:3002)"
echo "   3. Serveur de production (69.197.142.189:3002)"
echo ""
echo "📋 Logs attendus avec ViewModel:"
echo "   - Tentative de connexion WebSocket: ws://[url]"
echo "   - WebSocket ouvert avec succès vers [url]"
echo "   - Connexion WebSocket réussie: [url]"
echo "   - Message WebSocket reçu: [message]"
echo ""

# Afficher les logs filtrés pour la connectivité WebSocket avec ViewModel
adb logcat | grep -E "(WebSocketService|WebSocketViewModel|Tentative de connexion WebSocket|WebSocket ouvert avec succès|Connexion WebSocket réussie|Message WebSocket reçu|Échec WebSocket|BackgroundService|MainActivity)"
