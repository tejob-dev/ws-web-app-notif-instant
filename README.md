# Application de Notifications Instantanées

Une application web complète avec Next.js et Node.js pour envoyer et recevoir des notifications en temps réel.

## 🚀 Fonctionnalités

- **Frontend Next.js** avec interface utilisateur moderne
- **Backend Node.js** avec Express et WebSocket
- **Application mobile React Native** pour Android/iOS
- **Notifications push natives** sur mobile
- **WebSocket interne** pour les notifications en temps réel
- **Base de données SQLite** pour l'historique des messages
- **API REST** pour envoyer des messages
- **Interface responsive** avec Tailwind CSS

## 📋 Prérequis

- Node.js (version 16 ou supérieure)
- npm ou yarn

## 🛠️ Installation

1. **Cloner le projet**
   ```bash
   git clone <votre-repo>
   cd ws-web-app-notif-instant
   ```

2. **Installer les dépendances du frontend**
   ```bash
   npm install
   ```

3. **Installer les dépendances du backend**
   ```bash
   cd backend
   npm install
   cd ..
   ```

4. **Installer les dépendances de l'application mobile** (optionnel)
   ```bash
   cd mobile
   npm install
   cd ..
   ```

## 🚀 Démarrage

### Mode développement

1. **Démarrer le backend** (dans un terminal)
   ```bash
   npm run backend
   ```

2. **Démarrer le frontend** (dans un autre terminal)
   ```bash
   npm run dev
   ```

3. **Ouvrir l'application**
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:3001
   - Application mobile: Voir `mobile/README.md`

### Mode production

1. **Construire le frontend**
   ```bash
   npm run build
   ```

2. **Démarrer en production**
   ```bash
   npm start
   npm run backend:prod
   ```

## 📡 API Endpoints

### POST /api/send-message
Envoie un message broadcast à tous les clients connectés.

**Body:**
```json
{
  "content": "Votre message",
  "type": "info" // optionnel: "info", "success", "warning", "error"
}
```

### GET /api/messages
Récupère l'historique des messages.

**Query parameters:**
- `limit`: Nombre maximum de messages (défaut: 50)

### GET /api/stats
Récupère les statistiques de l'application.

### GET /api/health
Vérifie l'état du serveur.

## 🔧 Configuration

### Variables d'environnement

Créez un fichier `.env.local` dans le dossier racine :

```env
NEXT_PUBLIC_API_URL=http://localhost:3001
NEXT_PUBLIC_WS_URL=http://localhost:3001
```

### Configuration Firebase (pour les notifications mobiles)

1. Créez un projet Firebase sur [Firebase Console](https://console.firebase.google.com/)
2. Ajoutez Android/iOS à votre projet
3. Téléchargez les fichiers de configuration :
   - `google-services.json` → `mobile/android/app/`
   - `GoogleService-Info.plist` → `mobile/ios/` (si iOS)
4. Configurez Firebase Admin dans le backend (voir `backend/firebase-config.js`)

### Configuration Web Push

Les clés VAPID sont configurées par défaut pour le développement. Pour la production, générez vos propres clés :

```bash
cd backend
npx web-push generate-vapid-keys
```

Puis mettez à jour les clés dans `backend/server.js`.

## 🏗️ Architecture

```
ws-web-app-notif-instant/
├── app/                    # Frontend Next.js
│   ├── globals.css        # Styles globaux
│   ├── layout.tsx         # Layout principal
│   └── page.tsx           # Page d'accueil
├── backend/               # Backend Node.js
│   ├── package.json       # Dépendances backend
│   ├── server.js          # Serveur Express + WebSocket
│   └── firebase-config.js # Configuration Firebase
├── mobile/                # Application mobile React Native
│   ├── App.tsx            # Application principale
│   ├── package.json       # Dépendances mobile
│   └── android/           # Configuration Android
├── package.json           # Dépendances frontend
├── next.config.js         # Configuration Next.js
├── tailwind.config.js     # Configuration Tailwind
└── tsconfig.json          # Configuration TypeScript
```

## 🔌 WebSocket Events

### Côté client
- `connect` - Connexion établie
- `disconnect` - Connexion fermée
- `message` - Nouveau message reçu

### Côté serveur
- `message` - Diffuser un message à tous les clients

## 📱 Notifications

### WebSocket (Web)
L'application web utilise un système WebSocket interne pour les notifications :

1. Connexion automatique au serveur WebSocket
2. Réception des messages en temps réel
3. Affichage instantané des notifications
4. Gestion automatique des déconnexions/reconnexions

### Push Notifications (Mobile)
L'application mobile utilise Firebase Cloud Messaging pour les notifications push :

1. Enregistrement automatique du token FCM
2. Notifications push natives Android/iOS
3. Affichage dans la barre de notifications du téléphone
4. Gestion des notifications en arrière-plan

## 🗄️ Base de données

SQLite est utilisé pour stocker :
- **messages** : Historique des messages envoyés
- **mobile_tokens** : Tokens FCM des appareils mobiles

## 🚀 Déploiement

### Avec PM2

1. **Installer PM2 globalement**
   ```bash
   npm install -g pm2
   ```

2. **Créer ecosystem.config.js**
   ```javascript
   module.exports = {
     apps: [
       {
         name: 'notification-backend',
         script: './backend/server.js',
         cwd: './',
         instances: 1,
         autorestart: true,
         watch: false,
         max_memory_restart: '1G',
         env: {
           NODE_ENV: 'production',
           PORT: 3001
         }
       },
       {
         name: 'notification-frontend',
         script: 'npm',
         args: 'start',
         cwd: './',
         instances: 1,
         autorestart: true,
         watch: false,
         max_memory_restart: '1G',
         env: {
           NODE_ENV: 'production',
           PORT: 3000
         }
       }
     ]
   };
   ```

3. **Démarrer avec PM2**
   ```bash
   pm2 start ecosystem.config.js
   ```

## 🧪 Test

Pour tester l'application :

1. Ouvrez plusieurs onglets du navigateur
2. Vérifiez que tous les onglets sont connectés au WebSocket
3. Envoyez un message depuis un onglet
4. Vérifiez que tous les onglets reçoivent le message instantanément
5. Vérifiez que les messages s'affichent dans l'historique

## 📝 Notes

- L'application fonctionne en localhost par défaut
- Pour la production, configurez les URLs dans les variables d'environnement
- Le système WebSocket fonctionne en HTTP/HTTPS
- La base de données SQLite est créée automatiquement au premier démarrage

## 🤝 Contribution

1. Fork le projet
2. Créez une branche pour votre fonctionnalité
3. Committez vos changements
4. Poussez vers la branche
5. Ouvrez une Pull Request

## 📄 Licence

MIT License
