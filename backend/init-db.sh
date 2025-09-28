#!/bin/bash
# Script d'initialisation de la base de données SQLite

echo "🗄️ Initialisation de la base de données SQLite..."

# Créer le fichier de base de données s'il n'existe pas
if [ ! -f "/app/notifications.db" ]; then
    echo "📝 Création du fichier de base de données..."
    touch /app/notifications.db
    chmod 664 /app/notifications.db
fi

# Créer le dossier data s'il n'existe pas
if [ ! -d "/app/data" ]; then
    echo "📁 Création du dossier data..."
    mkdir -p /app/data
    chmod 755 /app/data
fi

# Vérifier les permissions
echo "🔍 Vérification des permissions..."
ls -la /app/notifications.db
ls -la /app/data

echo "✅ Initialisation terminée"
