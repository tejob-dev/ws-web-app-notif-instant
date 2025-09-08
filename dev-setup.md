# Guide de Configuration pour le Développement

## 🚀 Démarrage Rapide

### 1. Installation
```bash
# Installer toutes les dépendances
npm run install:all
```

### 2. Démarrage en mode développement
```bash
# Démarrer frontend et backend ensemble
npm run start:all

# Ou démarrer séparément :
# Terminal 1 - Backend
npm run backend

# Terminal 2 - Frontend  
npm run dev
```

### 3. Accès à l'application
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:3001
- **WebSocket**: ws://localhost:3001

## 🧪 Tests

### Test de l'API
```bash
# Démarrer le backend d'abord
npm run backend

# Dans un autre terminal, tester l'API
node test-api.js
```

### Test manuel
1. Ouvrir http://localhost:3000 dans plusieurs onglets
2. Autoriser les notifications dans un onglet
3. Envoyer un message depuis un onglet
4. Vérifier que tous les onglets reçoivent le message

## 🔧 Configuration

### Variables d'environnement
Copiez `env.example` vers `.env.local` et ajustez selon vos besoins :

```bash
cp env.example .env.local
```

### Base de données
La base de données SQLite est créée automatiquement au premier démarrage dans `backend/notifications.db`.

### Web Push
Les clés VAPID sont configurées par défaut pour le développement. Pour la production, générez vos propres clés :

```bash
cd backend
npx web-push generate-vapid-keys
```

## 📁 Structure du Projet

```
ws-web-app-notif-instant/
├── app/                    # Frontend Next.js
│   ├── globals.css        # Styles Tailwind
│   ├── layout.tsx         # Layout principal
│   └── page.tsx           # Page d'accueil
├── backend/               # Backend Node.js
│   ├── package.json       # Dépendances backend
│   └── server.js          # Serveur Express + WebSocket
├── logs/                  # Logs PM2 (créé automatiquement)
├── package.json           # Dépendances frontend
├── ecosystem.config.js    # Configuration PM2
├── start.js              # Script de démarrage
├── test-api.js           # Tests API
└── README.md             # Documentation principale
```

## 🐛 Débogage

### Logs
- **Backend**: Logs dans la console ou `logs/backend-*.log`
- **Frontend**: Logs dans la console du navigateur

### Problèmes courants

#### Port déjà utilisé
```bash
# Vérifier les processus utilisant le port 3001
netstat -ano | findstr :3001

# Tuer le processus (Windows)
taskkill /PID <PID> /F
```

#### Erreur de connexion WebSocket
- Vérifier que le backend est démarré
- Vérifier l'URL WebSocket dans le frontend
- Vérifier les paramètres CORS

#### Notifications push ne fonctionnent pas
- Vérifier que HTTPS est utilisé (requis en production)
- Vérifier les permissions du navigateur
- Vérifier les clés VAPID

## 🔄 Développement

### Hot Reload
- **Frontend**: Rechargement automatique avec Next.js
- **Backend**: Redémarrage automatique avec nodemon

### Modifications
1. Modifier le code
2. Sauvegarder
3. Les changements sont appliqués automatiquement

### Base de données
Pour réinitialiser la base de données :
```bash
rm backend/notifications.db
# Redémarrer le backend
```

## 📦 Build de Production

```bash
# Construire le frontend
npm run build

# Démarrer en production
npm start
npm run backend:prod
```

## 🚀 Déploiement

### Avec PM2
```bash
# Déploiement automatique
chmod +x deploy.sh
./deploy.sh

# Ou manuellement
pm2 start ecosystem.config.js
```

### VPS/Cloud
1. Cloner le repository
2. Installer les dépendances
3. Configurer les variables d'environnement
4. Démarrer avec PM2

## 📊 Monitoring

### PM2
```bash
# Statut des processus
pm2 status

# Logs en temps réel
pm2 logs

# Redémarrer
pm2 restart all
```

### API Health Check
```bash
curl http://localhost:3001/api/health
```

## 🔐 Sécurité

### Développement
- CORS configuré pour localhost
- Clés VAPID par défaut
- Base de données SQLite locale

### Production
- Configurer HTTPS
- Générer de nouvelles clés VAPID
- Configurer un firewall
- Utiliser une base de données sécurisée

## 📝 Notes

- L'application fonctionne en localhost par défaut
- Les notifications push nécessitent HTTPS en production
- La base de données est créée automatiquement
- Les logs sont stockés dans le dossier `logs/`
