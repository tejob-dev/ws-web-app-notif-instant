# Configuration Mobile pour Production

## 📱 Configuration de l'Application Mobile

L'application mobile est maintenant configurée pour se connecter au serveur de production :

### Serveur Principal
- **URL** : `69.197.142.189:5022`
- **Protocole** : WebSocket (ws://)
- **Port** : 5022

### URLs de Fallback
L'application essaiera ces URLs dans l'ordre :
1. `69.197.142.189:5022` (Production)
2. `192.168.1.71:3001` (Développement local)
3. `localhost:3001` (Développement local)
4. `10.0.2.2:3001` (Émulateur Android)

## 🔧 Configuration Technique

### Fichier de Configuration
Le fichier `NotificationApp/config.js` contient :
```javascript
export const config = {
  serverUrl: '69.197.142.189:5022',
  network: {
    fallbackUrls: [
      '69.197.142.189:5022', // Production
      '192.168.1.71:3001',   // Développement local
      'localhost:3001',
      '10.0.2.2:3001', // Émulateur Android
    ],
    connectionTimeout: 10000,
  },
  // ... autres configurations
};
```

### APIs Disponibles
- **WebSocket** : `ws://69.197.142.189:5022`
- **API REST** : `http://69.197.142.189:5022/api`
- **Health Check** : `http://69.197.142.189:5022/api/health`

## 🚀 Déploiement Mobile

### 1. Build de l'Application
```bash
cd NotificationApp
npm install
npx expo build:android
npx expo build:ios
```

### 2. Configuration des Notifications Push
- Configurez Firebase dans `firebase-config.js`
- Ajoutez votre `google-services.json` (Android)
- Configurez les certificats iOS dans `ios/`

### 3. Test de Connexion
L'application testera automatiquement la connexion au serveur de production.

## 🔍 Vérification

### Test de Connexion WebSocket
```bash
# Depuis un terminal
wscat -c ws://69.197.142.189:5022
```

### Test API REST
```bash
# Health check
curl http://69.197.142.189:5022/api/health

# Envoyer un message via GET
curl "http://69.197.142.189:5022/api/send-message?content=Test%20mobile&type=info"
```

## 📊 Monitoring Mobile

### Logs de Connexion
L'application affichera dans les logs :
- Tentative de connexion au serveur principal
- Fallback vers les URLs alternatives si nécessaire
- Statut de connexion WebSocket

### Notifications Push
- Les notifications push fonctionneront via Firebase Cloud Messaging
- Configuration automatique des tokens mobiles
- Gestion des tokens invalides

## 🛠️ Dépannage Mobile

### Problèmes de Connexion
1. **Vérifier le firewall** : Port 5022 doit être ouvert
2. **Tester la connectivité** : `ping 69.197.142.189`
3. **Vérifier les logs** : Console de l'application mobile

### Problèmes de Notifications
1. **Vérifier Firebase** : Configuration des certificats
2. **Tester les tokens** : API `/api/register-mobile-token`
3. **Vérifier les permissions** : Notifications autorisées

## 📝 Notes Importantes

- L'application mobile se connecte automatiquement au serveur de production
- Les notifications push nécessitent une configuration Firebase valide
- Le système de fallback assure la connectivité même en cas de problème réseau
- Les logs de connexion sont disponibles dans la console de l'application
