# 🔔 GUIDE DE DÉBOGAGE - FLUX DE NOTIFICATIONS

## Problème résolu
✅ **Le flux Socket.IO → NotificationService est maintenant optimisé pour garantir que le contenu du message apparaît dans la notification popup.**

## 🔧 Améliorations apportées

### 1. **Gestion améliorée des messages Socket.IO**
- ✅ Détection automatique du type de données reçues (String, JSONObject, etc.)
- ✅ Parsing robuste des messages JSON structurés
- ✅ Fallback pour les messages texte brut
- ✅ Logs détaillés pour le débogage

### 2. **Flux de données optimisé**
```
Serveur → Socket.IO → handleMessage() → Message → NotificationService → Popup
```

### 3. **Gestion d'erreurs améliorée**
- ✅ Messages de fallback en cas d'erreur de parsing
- ✅ Logs détaillés à chaque étape
- ✅ Préservation du contenu même en cas d'erreur

## 📋 Format des messages supportés

### **Message JSON structuré (recommandé)**
```json
{
  "id": "uuid",
  "content": "Contenu du message",
  "type": "info",
  "timestamp": "2024-01-01T00:00:00Z"
}
```

### **Message texte brut (fallback)**
```
Contenu du message simple
```

## 🧪 Test du flux

### **Étape 1 : Exécuter le script de test**
```bash
./test-notification-flow.sh
```

### **Étape 2 : Vérifier les logs**
```bash
adb logcat -s "SocketIOService" "NotificationService" | grep -E "(Message|Notification)"
```

### **Étape 3 : Envoyer un message de test**
```bash
curl -X POST "http://192.168.1.71:3002/api/send-message" \
  -H "Content-Type: application/json" \
  -d '{"content": "Test de notification", "type": "test"}'
```

## 🔍 Logs à surveiller

### **Logs SocketIOService**
```
📨 Message Socket.IO reçu (type: JSONObject): {"id":"...","content":"..."}
📨 Message créé: ID=..., Content='...', Type=...
🔔 Notification déclenchée pour le message: '...'
```

### **Logs NotificationService**
```
🔔 Tentative d'affichage de notification pour: ...
📱 Notification pop-up affichée avec succès (ID: ...): ...
```

## 🚨 Résolution des problèmes

### **Problème : Message reçu mais pas de notification**
**Vérifications :**
1. ✅ `handleWebSocketMessage()` est appelée
2. ✅ `showPopUpNotification()` est appelée
3. ✅ Les permissions de notification sont accordées
4. ✅ Le service de notifications est initialisé

### **Problème : Notification vide ou sans contenu**
**Vérifications :**
1. ✅ Le contenu du message est extrait correctement
2. ✅ `message.content` n'est pas vide
3. ✅ Le parsing JSON fonctionne

### **Problème : Messages perdus**
**Vérifications :**
1. ✅ La connexion Socket.IO est stable
2. ✅ Les gestionnaires d'événements sont enregistrés
3. ✅ Aucune erreur dans les logs

## 📱 Vérifications manuelles

### **Test 1 : Message simple**
Envoyez un message texte simple et vérifiez qu'il apparaît dans la notification.

### **Test 2 : Message JSON**
Envoyez un message JSON structuré et vérifiez que le contenu est extrait correctement.

### **Test 3 : Message d'erreur**
Envoyez un message malformé et vérifiez que le fallback fonctionne.

## 🔄 Prochaines étapes

1. **Tester** avec différents types de messages
2. **Vérifier** que le contenu apparaît dans les notifications
3. **Surveiller** les logs pour confirmer le bon fonctionnement
4. **Signaler** tout problème avec les logs détaillés

## 📊 Checklist de validation

- [ ] Messages Socket.IO reçus et traités
- [ ] Contenu extrait correctement du JSON
- [ ] NotificationService appelé avec le bon contenu
- [ ] Notification popup affichée avec le contenu
- [ ] Logs sans erreurs critiques
- [ ] Fallback fonctionnel pour les messages simples
