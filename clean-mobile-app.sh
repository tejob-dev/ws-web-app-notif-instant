#!/bin/bash

# Script de nettoyage et redémarrage de l'application mobile
# Corrige les problèmes de cache et redémarre proprement

echo "🧹 Nettoyage de l'application mobile"
echo "===================================="

cd NotificationApp

# Nettoyer le cache Metro
echo "📦 Nettoyage du cache Metro..."
npx react-native start --reset-cache &
METRO_PID=$!

# Attendre un peu pour que Metro démarre
sleep 3

# Arrêter Metro
kill $METRO_PID 2>/dev/null

# Nettoyer le cache npm
echo "📦 Nettoyage du cache npm..."
npm cache clean --force

# Nettoyer le cache Expo
echo "📦 Nettoyage du cache Expo..."
npx expo install --fix

# Nettoyer le stockage local de l'appareil (si possible)
echo "📱 Nettoyage du stockage local..."
echo "💡 Pour nettoyer le stockage local de l'appareil:"
echo "   - Android: Paramètres > Apps > NotificationApp > Stockage > Effacer les données"
echo "   - iOS: Supprimer et réinstaller l'app"

echo ""
echo "✅ Nettoyage terminé !"
echo ""
echo "🚀 Pour redémarrer l'application:"
echo "1. cd NotificationApp"
echo "2. npx expo start --clear"
echo "3. Ou utilisez votre méthode habituelle de démarrage"
echo ""
echo "🔧 Les corrections apportées:"
echo "- ✅ Clés React Native uniques (évite les doublons)"
echo "- ✅ Notifications Expo mises à jour (supprime les avertissements)"
echo "- ✅ Gestion des messages améliorée (évite les doublons)"
echo "- ✅ Configuration WebSocket optimisée"
