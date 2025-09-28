#!/bin/bash

# Script de test Docker WebSocket natif
echo "🐳 Test Docker WebSocket natif sur le port 5023..."

# Vérifier que Docker est en cours d'exécution
if ! docker info &> /dev/null; then
    echo "❌ Docker n'est pas en cours d'exécution"
    exit 1
fi

# Vérifier qu'un appareil Android est connecté
adb devices | grep -q "device$"
if [ $? -ne 0 ]; then
    echo "❌ Aucun appareil Android connecté"
    exit 1
fi

echo "📱 Appareil Android connecté détecté"

# Construire l'APK avec la nouvelle configuration
echo "🔨 Construction de l'APK avec la configuration Docker..."
cd NotificationAppKotlin
./gradlew assembleDebug

if [ $? -ne 0 ]; then
    echo "❌ Erreur lors de la construction de l'APK"
    exit 1
fi

# Installer l'APK
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
echo "📲 Installation de l'APK..."
adb install -r "$APK_PATH"

if [ $? -eq 0 ]; then
    echo "✅ APK installé avec succès"
else
    echo "❌ Erreur lors de l'installation de l'APK"
    exit 1
fi

# Vérifier que le backend Docker est en cours d'exécution
echo "🔍 Vérification du backend Docker..."
if ! docker-compose ps | grep -q "notification-backend.*Up"; then
    echo "⚠️ Backend Docker non détecté, démarrage..."
    cd ..
    docker-compose up -d backend
    sleep 5
    cd NotificationAppKotlin
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
echo "📋 Test Docker WebSocket natif en cours (Ctrl+C pour arrêter):"
echo "=============================================================="
echo "🐳 Configuration Docker:"
echo "   - Port API REST: 3001"
echo "   - Port WebSocket natif: 5023"
echo ""
echo "📱 URLs de test dans l'app Android:"
echo "   - ws://192.168.1.71:5023 (Docker local)"
echo "   - ws://localhost:5023 (Docker localhost)"
echo "   - ws://69.197.142.189:5023 (Docker production)"
echo ""
echo "📋 Logs attendus:"
echo "   - Tentative de connexion WebSocket: ws://192.168.1.71:5023"
echo "   - WebSocket ouvert avec succès vers 192.168.1.71:5023"
echo "   - Connexion WebSocket réussie: 192.168.1.71:5023"
echo ""

# Afficher les logs filtrés pour la connectivité WebSocket Docker
adb logcat | grep -E "(WebSocketService|Tentative de connexion WebSocket|WebSocket ouvert avec succès|Connexion WebSocket réussie|Message WebSocket reçu|Échec WebSocket|5023|Docker)"
