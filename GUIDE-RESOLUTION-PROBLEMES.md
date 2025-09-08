# Guide de Résolution des Problèmes

## 🚨 Problèmes React Native CLI

### Erreur: "No emulators found"
**Cause**: Aucun émulateur Android configuré

**Solution**:
1. Ouvrez Android Studio
2. Allez dans Tools > AVD Manager
3. Créez un nouvel émulateur Android
4. Démarrez l'émulateur
5. Relancez `npm run android`

### Erreur: "'gradlew.bat' n'est pas reconnu"
**Cause**: Structure React Native incomplète

**Solution**: Utilisez Expo au lieu de React Native CLI
```bash
# Supprimez le dossier mobile problématique
# Créez une nouvelle app avec Expo
npx create-expo-app NotificationApp --template blank-typescript
cd NotificationApp
npx expo install expo-notifications expo-device expo-constants @react-native-async-storage/async-storage socket.io-client axios
```

## 📱 Problèmes Expo

### Erreur: "Failed to launch emulator"
**Cause**: Émulateur non configuré ou non démarré

**Solutions**:
1. **Utilisez Expo Go** (recommandé):
   ```bash
   npx expo start
   # Scannez le QR code avec Expo Go sur votre téléphone
   ```

2. **Configurez un émulateur Android**:
   - Ouvrez Android Studio
   - Créez un émulateur dans AVD Manager
   - Démarrez l'émulateur
   - Lancez `npx expo start --android`

### Erreur: "Cannot connect to Metro"
**Cause**: Serveur Metro non démarré

**Solution**:
```bash
# Arrêtez tous les processus
# Redémarrez Expo
npx expo start --clear
```

## 🔧 Problèmes de Configuration

### Erreur: "Network request failed"
**Cause**: URL du serveur incorrecte

**Solutions**:
1. **Pour émulateur Android**:
   ```typescript
   const API_BASE = 'http://10.0.2.2:3001/api';
   const WS_URL = 'http://10.0.2.2:3001';
   ```

2. **Pour appareil physique**:
   ```typescript
   // Remplacez par votre IP locale
   const API_BASE = 'http://192.168.1.100:3001/api';
   const WS_URL = 'http://192.168.1.100:3001';
   ```

3. **Trouvez votre IP**:
   ```bash
   # Windows
   ipconfig
   
   # Linux/Mac
   ifconfig
   ```

### Erreur: "Permission denied for notifications"
**Cause**: Permissions de notification refusées

**Solution**:
1. Allez dans Paramètres de l'appareil
2. Applications > Votre App > Notifications
3. Activez les notifications
4. Redémarrez l'app

## 🔌 Problèmes WebSocket

### Erreur: "WebSocket connection failed"
**Cause**: Serveur backend non démarré ou URL incorrecte

**Solutions**:
1. **Vérifiez que le serveur backend est démarré**:
   ```bash
   cd backend
   npm run dev
   ```

2. **Vérifiez l'URL WebSocket** dans l'app mobile

3. **Testez la connexion**:
   ```bash
   curl http://localhost:3001/api/health
   ```

### Erreur: "CORS policy"
**Cause**: Problème de CORS entre l'app mobile et le serveur

**Solution**: Le serveur est déjà configuré pour CORS, vérifiez l'URL

## 📡 Problèmes de Notifications

### Erreur: "Token not generated"
**Cause**: Expo Push Token non généré

**Solutions**:
1. **Utilisez un appareil physique** (pas d'émulateur)
2. **Vérifiez la connexion internet**
3. **Vérifiez les permissions de notification**

### Erreur: "Notifications not received"
**Cause**: Token non enregistré ou serveur non configuré

**Solutions**:
1. **Vérifiez les logs du serveur** pour voir si le token est enregistré
2. **Testez l'envoi de message** depuis l'interface web
3. **Vérifiez que Firebase est configuré** (pour React Native CLI)

## 🛠️ Solutions Rapides

### Redémarrage complet
```bash
# 1. Arrêtez tous les processus
# 2. Redémarrez le serveur backend
cd backend
npm run dev

# 3. Dans un autre terminal, redémarrez Expo
cd NotificationApp
npx expo start --clear
```

### Test de connectivité
```bash
# Testez l'API du serveur
curl http://localhost:3001/api/health

# Testez l'envoi de message
curl -X POST http://localhost:3001/api/send-message \
  -H "Content-Type: application/json" \
  -d '{"content": "Test", "type": "info"}'
```

### Vérification des logs
```bash
# Logs Expo
npx expo start --verbose

# Logs du serveur backend
# Vérifiez la console où vous avez lancé npm run dev
```

## 📋 Checklist de Dépannage

- [ ] Serveur backend démarré (`npm run dev` dans backend/)
- [ ] Application Expo démarrée (`npx expo start`)
- [ ] Émulateur Android démarré OU Expo Go sur téléphone
- [ ] URLs correctes dans l'app mobile
- [ ] Permissions de notification accordées
- [ ] Connexion internet active
- [ ] Firewall/antivirus ne bloque pas les connexions

## 🆘 Si Rien Ne Fonctionne

1. **Utilisez Expo Go** sur votre téléphone (plus simple)
2. **Vérifiez que le serveur backend fonctionne** avec l'interface web
3. **Testez d'abord l'interface web** avant l'app mobile
4. **Consultez les logs** pour identifier l'erreur exacte

## 📞 Support

Si vous rencontrez d'autres problèmes :
1. Vérifiez les logs d'erreur
2. Consultez la documentation Expo
3. Vérifiez que toutes les dépendances sont installées
4. Redémarrez complètement l'environnement de développement
