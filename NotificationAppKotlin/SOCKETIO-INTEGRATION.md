# Intégration Socket.IO dans NotificationAppKotlin

## 🚀 Vue d'ensemble

Cette application Android Kotlin supporte maintenant **deux technologies de connexion en temps réel** :

1. **WebSocket natif** (port 5023) - Connexion directe et légère
2. **Socket.IO** (port 5022) - Connexion avec fallback automatique et fonctionnalités avancées

## 📋 Fonctionnalités

### ✅ Connexion Hybride
- **Connexion automatique** : Essaie WebSocket puis Socket.IO en cas d'échec
- **Connexion sélective** : Choix manuel entre WebSocket et Socket.IO
- **Fallback intelligent** : Bascule automatiquement entre les technologies

### ✅ Gestion des Messages
- Réception de messages en temps réel
- Envoi de messages personnalisés
- Support des notifications push
- Enregistrement des tokens mobiles

### ✅ Interface Utilisateur
- Indicateur de statut de connexion en temps réel
- Affichage du type de connexion actuel
- Gestion des erreurs avec messages clairs
- Interface intuitive avec boutons dédiés

## 🔧 Configuration

### URLs Socket.IO Configurées
```kotlin
val SOCKETIO_URLS = listOf(
    "http://69.197.142.189:5022", // Production Socket.IO
    "http://192.168.1.71:3001",   // Local Socket.IO
    "http://10.0.2.2:3001",       // Émulateur Socket.IO
    "http://localhost:3001",      // Développement local Socket.IO
)
```

### Ports Supportés
- **5023** : WebSocket natif (production)
- **5022** : Socket.IO (production)
- **3002** : WebSocket (développement)
- **3001** : Socket.IO (développement)

## 📱 Utilisation

### 1. Connexion Automatique
```kotlin
viewModel.connect() // Essaie WebSocket puis Socket.IO
```

### 2. Connexion Socket.IO Spécifique
```kotlin
viewModel.connectWithSocketIO() // Force Socket.IO
```

### 3. Connexion WebSocket Spécifique
```kotlin
viewModel.connectWithWebSocket() // Force WebSocket
```

### 4. Envoi de Messages
```kotlin
viewModel.sendMessage("Mon message")
```

## 🏗️ Architecture

### Services
- `WebSocketService` : Gestion WebSocket natif avec OkHttp
- `SocketIOService` : Gestion Socket.IO avec bibliothèque officielle

### ViewModels
- `WebSocketViewModel` : ViewModel original pour WebSocket uniquement
- `HybridConnectionViewModel` : ViewModel hybride avec support des deux technologies

### Modèles
- `Message` : Structure des messages
- `ConnectionStatus` : Statut de connexion
- `WebSocketEvent` : Événements WebSocket/Socket.IO

## 🔌 Événements Socket.IO Supportés

### Événements Système
- `connect` : Connexion établie
- `disconnect` : Déconnexion
- `connect_error` : Erreur de connexion
- `error` : Erreur générale

### Événements Personnalisés
- `message` : Messages généraux
- `notification` : Notifications push
- `register-mobile-token` : Enregistrement de token mobile

## 🛠️ Dépendances Ajoutées

```gradle
// Socket.IO
implementation 'io.socket:socket.io-client:2.1.0'
```

## 📊 Avantages Socket.IO vs WebSocket

### Socket.IO
- ✅ **Fallback automatique** : HTTP long polling si WebSocket échoue
- ✅ **Reconnexion automatique** : Gestion intelligente des déconnexions
- ✅ **Événements nommés** : Système d'événements plus riche
- ✅ **Compression** : Messages compressés automatiquement
- ✅ **Heartbeat** : Vérification de connexion périodique

### WebSocket Natif
- ✅ **Performance** : Plus léger et plus rapide
- ✅ **Simplicité** : Moins de complexité
- ✅ **Contrôle total** : Gestion manuelle complète
- ✅ **Standards** : Protocole standardisé

## 🚀 Déploiement

### Serveur Socket.IO (Port 5022)
Assurez-vous que votre serveur Socket.IO écoute sur le port 5022 :

```javascript
const io = require('socket.io')(5022, {
  cors: {
    origin: "*",
    methods: ["GET", "POST"]
  }
});
```

### Configuration Android
- Permissions Internet : ✅ Configurées
- Sécurité réseau : ✅ Autorise les connexions vers 69.197.142.189
- Services : ✅ SocketIOService enregistré dans AndroidManifest.xml

## 🔍 Debug et Logs

### Tags de Log
- `SocketIOService` : Logs du service Socket.IO
- `WebSocketService` : Logs du service WebSocket
- `HybridConnectionViewModel` : Logs du ViewModel hybride

### Exemple de Logs
```
D/SocketIOService: 🔗 Tentative de connexion Socket.IO...
D/SocketIOService: ✅ Socket.IO connecté avec succès vers http://69.197.142.189:5022
D/SocketIOService: 📨 Message Socket.IO reçu: {"type":"notification","data":"..."}
```

## 🎯 Prochaines Étapes

1. **Tester la connexion** avec le serveur Socket.IO sur le port 5022
2. **Configurer les événements** côté serveur selon vos besoins
3. **Optimiser la reconnexion** selon votre infrastructure
4. **Ajouter des fonctionnalités** spécifiques à Socket.IO (rooms, namespaces, etc.)

## 📞 Support

En cas de problème :
1. Vérifiez les logs avec les tags mentionnés
2. Assurez-vous que le serveur Socket.IO écoute sur le port 5022
3. Vérifiez la configuration réseau dans `network_security_config.xml`
4. Testez d'abord avec WebSocket, puis avec Socket.IO
