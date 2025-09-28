#!/bin/bash

# Script de test rapide pour la connectivité WebSocket améliorée
echo "🔍 Test rapide de connectivité WebSocket améliorée..."

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
echo "Recherche des logs de connectivité WebSocket..."
echo ""
echo "✅ Logs attendus maintenant:"
echo "   - Test de connectivité WebSocket vers 192.168.1.71:3002"
echo "   - Connectivité WebSocket OK vers [adresse]"
echo "   - Connexion WebSocket réussie"
echo ""

# Afficher les logs filtrés pour la connectivité WebSocket
adb logcat | grep -E "(WebSocketService|connectivité|connectivity|Test de connectivité WebSocket|Connectivité WebSocket OK|Connexion WebSocket|Échec de connexion|BackgroundService|MainActivity)"
