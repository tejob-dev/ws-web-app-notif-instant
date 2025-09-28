#!/bin/bash

# Script de déploiement pour l'application de notifications
# Usage: ./deploy.sh

set -e

echo "🚀 Démarrage du déploiement..."

# Couleurs pour les messages
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Fonction pour afficher les messages
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Vérifier que Docker est installé
if ! command -v docker &> /dev/null; then
    log_error "Docker n'est pas installé. Veuillez installer Docker d'abord."
    exit 1
fi

# Vérifier que Docker Compose est installé
if ! command -v docker-compose &> /dev/null; then
    log_error "Docker Compose n'est pas installé. Veuillez installer Docker Compose d'abord."
    exit 1
fi

# Arrêter les conteneurs existants
log_info "Arrêt des conteneurs existants..."
docker-compose down --remove-orphans || true

# Nettoyer les images anciennes
log_info "Nettoyage des images Docker..."
docker system prune -f || true

# Construire les nouvelles images
log_info "Construction des images Docker..."
docker-compose build --no-cache

# Démarrer les services
log_info "Démarrage des services..."
docker-compose up -d

# Attendre que les services soient prêts
log_info "Attente du démarrage des services..."
sleep 10

# Vérifier le statut des services
log_info "Vérification du statut des services..."
docker-compose ps

# Test de santé des services
log_info "Test de santé des services..."

# Test du backend
if curl -f http://localhost:5022/api/health > /dev/null 2>&1; then
    log_info "✅ Backend est opérationnel"
else
    log_warn "⚠️ Backend pourrait ne pas être prêt"
fi

# Test du frontend
if curl -f http://localhost:5020 > /dev/null 2>&1; then
    log_info "✅ Frontend est opérationnel"
else
    log_warn "⚠️ Frontend pourrait ne pas être prêt"
fi

log_info "🎉 Déploiement terminé!"
log_info "Frontend disponible sur: http://69.197.142.189:5020"
log_info "Backend API disponible sur: http://69.197.142.189:5022/api"
log_info "WebSocket disponible sur: ws://69.197.142.189:5022"

echo ""
log_info "Commandes utiles:"
echo "  - Voir les logs: docker-compose logs -f"
echo "  - Arrêter: docker-compose down"
echo "  - Redémarrer: docker-compose restart"
echo "  - Statut: docker-compose ps"
