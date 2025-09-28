# Configuration WebSocket Native pour l'Application Mobile

## ✅ Configuration Terminée

Votre application mobile est maintenant configurée pour utiliser le WebSocket natif sur `ws://localhost:3002`.

## 🔧 Modifications Apportées

### 1. Configuration (`config.js`)
- **Serveur principal** : `localhost:3002` (WebSocket natif)
- **URLs de fallback** : Inclut les ports 3002 et 3001
- **Timeout de connexion** : 15 secondes

### 2. Service WebSocket (`WebSocketService.js`)
- ✅ Connexion WebSocket native avec `new WebSocket()`
- ✅ Gestion automatique de la reconnexion
- ✅ Enregistrement des tokens mobiles
- ✅ Support des messages ping/pong

### 3. Service de Notifications (`NotificationService.js`)
- ✅ WebSocket natif par défaut (plus stable pour mobile)
- ✅ Fallback vers Socket.IO si nécessaire
- ✅ Enregistrement automatique des tokens
- ✅ Gestion des notifications push

## 🚀 Comment Utiliser

### Démarrage du Backend
```bash
cd backend
npm start
```
Le serveur démarre automatiquement :
- **API REST** : `http://localhost:3001`
- **Socket.IO** : `ws://localhost:3001`
- **WebSocket natif** : `ws://localhost:3002`

### Test de Connexion
```bash
# Test automatique complet
./test-mobile-websocket.sh

# Test WebSocket natif uniquement
cd NotificationApp
node test-websocket-native.js
```

### Application Mobile
1. **Démarrez l'application React Native**
2. **Le service se connecte automatiquement** au WebSocket natif
3. **En cas d'échec**, fallback automatique vers Socket.IO
4. **Les tokens mobiles** sont enregistrés automatiquement

## 📱 Fonctionnalités Mobile

### Connexion WebSocket
- ✅ Connexion native WebSocket (`ws://localhost:3002`)
- ✅ Reconnexion automatique en cas de perte
- ✅ Gestion des erreurs robuste
- ✅ Fallback vers Socket.IO si nécessaire

### Enregistrement des Tokens
- ✅ Enregistrement automatique des tokens mobiles
- ✅ Support Android et iOS
- ✅ Gestion des tokens Expo et FCM

### Notifications
- ✅ Notifications push natives
- ✅ Notifications persistantes
- ✅ Gestion des interactions utilisateur
- ✅ Stockage local des messages

## 🔍 Tests Disponibles

### 1. Test WebSocket Natif
```bash
cd NotificationApp
node test-websocket-native.js
```

### 2. Test Complet
```bash
./test-mobile-websocket.sh
```

### 3. Écran de Test dans l'App
- Utilisez `WebSocketTestScreen.tsx` pour tester dans l'application
- Tests de connexion, enregistrement et notifications

## 📊 Statut de la Configuration

| Composant | Statut | Port | Protocole |
|-----------|--------|------|-----------|
| Backend API | ✅ Actif | 3001 | HTTP |
| Socket.IO | ✅ Actif | 3001 | WebSocket |
| WebSocket Natif | ✅ Actif | 3002 | WebSocket |
| App Mobile | ✅ Configuré | - | WebSocket Natif |

## 🛠️ Dépannage

### Problème de Connexion
1. Vérifiez que le backend est démarré : `curl http://localhost:3001/api/health`
2. Vérifiez le port 3002 : `nc -z localhost 3002`
3. Consultez les logs du backend pour les erreurs

### Problème Mobile
1. Vérifiez la configuration dans `config.js`
2. Testez avec l'écran de test WebSocket
3. Consultez les logs React Native

### Fallback Automatique
Si le WebSocket natif échoue, l'application bascule automatiquement vers Socket.IO sur le port 3001.

## 📝 Notes Importantes

- **WebSocket natif** est plus stable pour les applications mobiles
- **Reconnexion automatique** en cas de perte de connexion
- **Enregistrement automatique** des tokens mobiles
- **Support complet** des notifications push
- **Fallback robuste** vers Socket.IO si nécessaire

Votre application mobile est maintenant prête à utiliser le WebSocket natif sur `ws://localhost:3002` ! 🎉
