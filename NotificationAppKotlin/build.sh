#!/bin/bash

# Script de build pour NotificationApp Kotlin
echo "🚀 Démarrage du build NotificationApp Kotlin..."

# Vérifier que nous sommes dans le bon répertoire
if [ ! -f "app/build.gradle" ]; then
    echo "❌ Erreur: Ce script doit être exécuté depuis le répertoire racine du projet Android"
    exit 1
fi

# Nettoyer le projet
echo "🧹 Nettoyage du projet..."
./gradlew clean

# Vérifier les dépendances
echo "🔍 Vérification des dépendances..."
./gradlew app:dependencies

# Build debug
echo "🔨 Build en mode debug..."
./gradlew assembleDebug

if [ $? -eq 0 ]; then
    echo "✅ Build debug réussi!"
    echo "📱 APK debug généré: app/build/outputs/apk/debug/app-debug.apk"
else
    echo "❌ Erreur lors du build debug"
    exit 1
fi

# Build release (optionnel)
read -p "🤔 Voulez-vous également builder la version release? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "🔨 Build en mode release..."
    ./gradlew assembleRelease
    
    if [ $? -eq 0 ]; then
        echo "✅ Build release réussi!"
        echo "📱 APK release généré: app/build/outputs/apk/release/app-release-unsigned.apk"
    else
        echo "❌ Erreur lors du build release"
        exit 1
    fi
fi

echo "🎉 Build terminé avec succès!"
