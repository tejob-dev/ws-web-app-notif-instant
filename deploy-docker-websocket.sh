#!/bin/bash

# Script de déploiement Docker pour le backend avec WebSocket natif
echo "🐳 Déploiement Docker du backend avec WebSocket natif..."

# Vérifier que Docker est installé
if ! command -v docker &> /dev/null; then
    echo "❌ Docker n'est pas installé"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose n'est pas installé"
    exit 1
fi

# Arrêter les conteneurs existants
echo "🛑 Arrêt des conteneurs existants..."
docker-compose down

# Construire et démarrer les services
echo "🔨 Construction et démarrage des services..."
docker-compose up --build -d

# Vérifier le statut des conteneurs
echo "📊 Statut des conteneurs:"
docker-compose ps

# Afficher les logs du backend
echo "📋 Logs du backend (Ctrl+C pour arrêter):"
docker-compose logs -f backend

echo ""
echo "✅ Déploiement terminé !"
echo "🌐 API REST: http://localhost:3001"
echo "🔌 WebSocket natif: ws://localhost:5023"
echo "📱 Pour tester avec l'app Android:"
echo "   - Modifiez AppConfig.kt pour utiliser ws://192.168.1.71:5023"
echo "   - Ou utilisez ws://localhost:5023 pour les tests locaux"
