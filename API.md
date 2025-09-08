# Documentation API

## Base URL
```
http://localhost:3001/api
```

## Endpoints

### 1. Santé du serveur
**GET** `/health`

Vérifie l'état du serveur.

**Réponse:**
```json
{
  "status": "OK",
  "timestamp": "2024-01-01T12:00:00.000Z",
  "connectedClients": 3
}
```

### 2. Envoyer un message
**POST** `/send-message`

Envoie un message broadcast à tous les clients connectés.

**Body:**
```json
{
  "content": "Votre message",
  "type": "info"
}
```

**Types de message:**
- `info` (défaut)
- `success`
- `warning`
- `error`

**Réponse:**
```json
{
  "success": true,
  "message": "Message envoyé avec succès"
}
```

### 3. Récupérer les messages
**GET** `/messages`

Récupère l'historique des messages.

**Query Parameters:**
- `limit` (optionnel): Nombre maximum de messages (défaut: 50)

**Exemple:**
```
GET /messages?limit=10
```

**Réponse:**
```json
[
  {
    "id": "uuid",
    "content": "Message content",
    "type": "info",
    "timestamp": "2024-01-01T12:00:00.000Z"
  }
]
```

### 4. Statistiques
**GET** `/stats`

Récupère les statistiques de l'application.

**Réponse:**
```json
{
  "totalMessages": 150,
  "connectedClients": 3
}
```


## WebSocket Events

### Connexion
```javascript
const socket = io('http://localhost:3001');

socket.on('connect', () => {
  console.log('Connecté au serveur');
});
```

### Événements reçus

#### `message`
Nouveau message reçu.

```javascript
socket.on('message', (data) => {
  console.log('Nouveau message:', data);
  // data = { id, content, type, timestamp }
});
```

### Événements envoyés

Aucun événement spécifique n'est envoyé par le client. Le système WebSocket est entièrement géré côté serveur.

## Exemples d'utilisation

### JavaScript (Frontend)
```javascript
// Connexion WebSocket
const socket = io('http://localhost:3001');

// Envoyer un message via API
async function sendMessage(content, type = 'info') {
  try {
    const response = await fetch('http://localhost:3001/api/send-message', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ content, type })
    });
    
    const result = await response.json();
    console.log('Message envoyé:', result);
  } catch (error) {
    console.error('Erreur:', error);
  }
}

// Écouter les messages
socket.on('message', (message) => {
  console.log('Nouveau message:', message);
  // Afficher le message dans l'interface
});
```

### cURL
```bash
# Envoyer un message
curl -X POST http://localhost:3001/api/send-message \
  -H "Content-Type: application/json" \
  -d '{"content": "Hello World", "type": "info"}'

# Récupérer les messages
curl http://localhost:3001/api/messages?limit=10

# Vérifier la santé
curl http://localhost:3001/api/health
```

### Python
```python
import requests
import json

# Envoyer un message
response = requests.post('http://localhost:3001/api/send-message', 
                        json={'content': 'Hello from Python', 'type': 'info'})
print(response.json())

# Récupérer les statistiques
stats = requests.get('http://localhost:3001/api/stats')
print(stats.json())
```

## Codes d'erreur

- `400` - Bad Request (données manquantes ou invalides)
- `500` - Internal Server Error (erreur serveur)

## Rate Limiting

Actuellement, il n'y a pas de limitation de taux implémentée. Pour la production, considérez l'ajout d'un middleware de rate limiting.

## Sécurité

- CORS configuré pour `http://localhost:3000`
- Validation des données d'entrée
- Gestion des erreurs sécurisée
- Tokens de notification stockés de manière sécurisée

## Monitoring

Utilisez les endpoints `/health` et `/stats` pour surveiller l'état de l'application.

Pour des logs détaillés, consultez les fichiers de logs PM2 ou les logs de la console.
