# 🚀 Guide de Déploiement Docker

Ce guide explique comment déployer l'application de notifications instantanées avec Docker.

## 📋 Prérequis

- Docker installé sur le serveur
- Docker Compose installé
- Accès au serveur de production (69.197.142.189)

## 🏗️ Architecture

- **Frontend Web** : Port 5020 (host) → 3000 (container)
- **Backend API** : Port 5022 (host) → 3001 (container)
- **Application Mobile** : Configurée pour 69.197.142.189:5022

## 🚀 Déploiement Rapide

### 1. Cloner le projet sur le serveur
```bash
git clone <votre-repo>
cd ws-web-app-notif-instant
```

### 2. Déployer avec Docker Compose
```bash
# Déploiement automatique
./deploy-docker.sh

# Ou manuellement
docker-compose up -d --build
```

### 3. Vérifier le déploiement
```bash
# Vérifier les services
docker-compose ps

# Voir les logs
docker-compose logs -f

# Tester les endpoints
curl http://69.197.142.189:5022/api/health
curl http://69.197.142.189:5020
```

## 🔧 Configuration

### Variables d'environnement
Copiez `env.production` vers `.env` et configurez :
- `FIREBASE_PROJECT_ID` : Votre projet Firebase
- `FIREBASE_PRIVATE_KEY` : Clé privée du service account
- `FIREBASE_CLIENT_EMAIL` : Email du service account

### URLs de production
- **Frontend** : http://69.197.142.189:5020
- **Backend API** : http://69.197.142.189:5022/api
- **WebSocket** : ws://69.197.142.189:5022

## 📱 Configuration Mobile

L'application mobile est configurée pour se connecter à :
- **Serveur** : 69.197.142.189:5022
- **Fallback** : URLs de développement locales

## 🛠️ Commandes Utiles

```bash
# Démarrer les services
docker-compose up -d

# Arrêter les services
docker-compose down

# Redémarrer un service
docker-compose restart backend
docker-compose restart frontend

# Voir les logs en temps réel
docker-compose logs -f

# Voir les logs d'un service spécifique
docker-compose logs -f backend
docker-compose logs -f frontend

# Reconstruire les images
docker-compose build --no-cache

# Nettoyer les volumes
docker-compose down -v
```

## 🔍 Monitoring

### Health Checks
- Backend : `GET /api/health`
- Frontend : `GET /`

### Logs
```bash
# Logs en temps réel
docker-compose logs -f

# Logs avec timestamps
docker-compose logs -f -t
```

## 🚨 Dépannage

### Problèmes courants

1. **Port déjà utilisé**
   ```bash
   # Vérifier les ports utilisés
   netstat -tulpn | grep :5020
   netstat -tulpn | grep :5022
   ```

2. **Services ne démarrent pas**
   ```bash
   # Voir les logs d'erreur
   docker-compose logs backend
   docker-compose logs frontend
   ```

3. **Problème de connexion mobile**
   - Vérifier que le port 5022 est ouvert sur le firewall
   - Tester la connexion WebSocket depuis l'extérieur

### Redémarrage complet
```bash
# Arrêter tout
docker-compose down

# Nettoyer
docker system prune -f

# Redémarrer
docker-compose up -d --build
```

## 📊 Statistiques

Pour voir les statistiques de l'application :
```bash
curl http://69.197.142.189:5022/api/stats
```

## 🔐 Sécurité

- Les conteneurs s'exécutent avec des utilisateurs non-root
- Les volumes sont montés de manière sécurisée
- Les variables d'environnement sensibles sont dans des fichiers séparés

## 📝 Notes

- La base de données SQLite est persistante via un volume Docker
- Les logs sont accessibles via `docker-compose logs`
- Le déploiement est idempotent (peut être relancé sans problème)
