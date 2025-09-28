#!/bin/bash

# Script de déploiement selon l'environnement
# Usage: ./deploy-env.sh [dev|prod]

set -e

# Couleurs
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

ENV=${1:-dev}

echo -e "${BLUE}🚀 Déploiement en mode: ${ENV}${NC}"

case $ENV in
  "dev"|"development")
    echo -e "${GREEN}📱 Déploiement en mode DÉVELOPPEMENT${NC}"
    echo "URLs:"
    echo "  Frontend: http://localhost:3000"
    echo "  Backend:  http://localhost:3001"
    echo "  WebSocket: ws://localhost:3001"
    
    # Arrêter les services existants
    docker-compose down --remove-orphans || true
    
    # Démarrer en mode développement
    docker-compose up -d --build
    
    echo -e "${GREEN}✅ Services démarrés en mode développement${NC}"
    ;;
    
  "prod"|"production")
    echo -e "${YELLOW}🌐 Déploiement en mode PRODUCTION${NC}"
    echo "URLs:"
    echo "  Frontend: http://69.197.142.189:5020"
    echo "  Backend:  http://69.197.142.189:5022"
    echo "  WebSocket: ws://69.197.142.189:5022"
    
    # Arrêter les services existants
    docker-compose -f docker-compose.yml -f docker-compose.prod.yml down --remove-orphans || true
    
    # Démarrer en mode production
    docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build
    
    echo -e "${GREEN}✅ Services démarrés en mode production${NC}"
    ;;
    
  *)
    echo -e "${YELLOW}❌ Mode non reconnu: $ENV${NC}"
    echo "Usage: $0 [dev|prod]"
    echo ""
    echo "Modes disponibles:"
    echo "  dev  - Développement local (ports 3000/3001)"
    echo "  prod - Production (ports 5020/5022)"
    exit 1
    ;;
esac

echo ""
echo -e "${BLUE}📊 Statut des services:${NC}"
docker-compose ps

echo ""
echo -e "${BLUE}🔍 Commandes utiles:${NC}"
echo "  Voir les logs: docker-compose logs -f"
echo "  Arrêter: docker-compose down"
echo "  Redémarrer: docker-compose restart"
