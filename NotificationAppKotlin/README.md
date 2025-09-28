# NotificationApp Kotlin

## Description

Application Android native développée en Kotlin qui reproduit toutes les fonctionnalités de l'app Expo NotificationApp. Cette application permet de recevoir des notifications en temps réel via WebSocket et fonctionne entièrement en arrière-plan.

## Fonctionnalités

### 🔔 Notifications en temps réel
- Service WebSocket natif pour recevoir les messages
- Notifications push avec son, vibration et LED
- Notification persistante dans la barre d'état
- Gestion automatique des canaux de notification Android

### 🚀 Service en arrière-plan
- Service foreground pour maintenir la connexion active
- Démarrage automatique au boot de l'appareil
- Reconnexion automatique en cas de perte de connexion
- Optimisation de la batterie gérée

### 📱 Interface utilisateur moderne
- Interface Material Design 3
- Affichage en temps réel du statut de connexion
- Liste des messages reçus avec horodatage
- Informations de compatibilité et de l'appareil
- Tests de fonctionnalités automatiques

### 🔧 Configuration avancée
- URLs de fallback multiples pour la connexion
- Configuration des timeouts et tentatives de reconnexion
- Gestion des permissions Android
- Stockage local des messages

## Configuration

### URLs du serveur WebSocket

Modifiez les URLs dans `AppConfig.kt` :

```kotlin
object Network {
    val FALLBACK_URLS = listOf(
        "192.168.1.71:3002",  // PRIORITÉ - Appareil physique
        "10.0.2.2:3002",      // Émulateur Android
        "localhost:3002",      // Développement local
        "69.197.142.189:3002", // Production WebSocket natif
        // ... autres URLs
    )
    const val CONNECTION_TIMEOUT = 15000L
}
```

### Permissions requises

L'application demande automatiquement les permissions suivantes :
- `POST_NOTIFICATIONS` - Afficher les notifications
- `VIBRATE` - Vibration pour les notifications
- `WAKE_LOCK` - Maintenir le service actif
- `FOREGROUND_SERVICE` - Service en premier plan
- `INTERNET` - Connexion réseau
- `ACCESS_NETWORK_STATE` - État du réseau

## Installation

### Prérequis

- Android Studio Hedgehog ou plus récent
- SDK Android 23+ (Android 6.0+)
- Kotlin 1.9.20+
- Gradle 8.2+

### Étapes d'installation

1. **Cloner le projet** :
   ```bash
   cd NotificationAppKotlin
   ```

2. **Ouvrir dans Android Studio** :
   - File → Open → Sélectionner le dossier `NotificationAppKotlin`

3. **Configurer les URLs** :
   - Modifier `app/src/main/java/com/notificationapp/kotlin/config/AppConfig.kt`
   - Adapter les URLs à votre serveur WebSocket

4. **Build et installer** :
   - Build → Make Project
   - Run → Run 'app'

### Build APK de production

```bash
./gradlew assembleRelease
```

L'APK sera généré dans `app/build/outputs/apk/release/`

## Architecture

### Structure du projet

```
app/src/main/java/com/notificationapp/kotlin/
├── MainActivity.kt                 # Activité principale
├── adapter/
│   └── MessageAdapter.kt          # Adaptateur RecyclerView
├── config/
│   └── AppConfig.kt               # Configuration de l'app
├── model/
│   └── Models.kt                  # Modèles de données
├── receiver/
│   ├── BootReceiver.kt            # Démarrage automatique
│   └── NotificationReceiver.kt    # Gestion des notifications
├── service/
│   ├── BackgroundService.kt       # Service principal
│   ├── NotificationService.kt     # Service de notifications
│   └── WebSocketService.kt        # Service WebSocket
└── utils/
    └── AndroidCompatibility.kt    # Utilitaires compatibilité
```

### Services

1. **BackgroundService** - Service principal qui orchestre tout
2. **WebSocketService** - Gestion de la connexion WebSocket native
3. **NotificationService** - Affichage et gestion des notifications

### Flux de données

1. L'utilisateur démarre l'app et accorde les permissions
2. `BackgroundService` démarre et lance `WebSocketService`
3. `WebSocketService` se connecte au serveur avec fallback
4. Les messages reçus sont traités par `NotificationService`
5. Les notifications sont affichées et les messages stockés localement

## Compatibilité

### Versions Android supportées
- **Minimum** : Android 6.0 (API 23)
- **Cible** : Android 14 (API 34)
- **Recommandé** : Android 8.0+ (API 26+) pour les canaux de notification

### Tests de compatibilité automatiques

L'app teste automatiquement :
- ✅ Connexion WebSocket
- ✅ Permissions de notification
- ✅ Service en arrière-plan
- ✅ Notification persistante
- ✅ Stockage local

## Dépannage

### Problèmes courants

1. **Notifications non affichées** :
   - Vérifier les permissions dans Paramètres → Apps → NotificationApp
   - Désactiver l'optimisation de la batterie
   - Vérifier les paramètres de notification

2. **Service arrêté automatiquement** :
   - Désactiver l'optimisation de la batterie
   - Ajouter l'app aux apps protégées (selon le fabricant)
   - Vérifier les paramètres de démarrage automatique

3. **Connexion WebSocket échoue** :
   - Vérifier la connectivité réseau
   - Tester les URLs de fallback
   - Vérifier les logs dans Logcat

### Logs de debug

Filtrer les logs par tag :
```bash
adb logcat | grep -E "(WebSocketService|NotificationService|BackgroundService)"
```

## Comparaison avec l'app Expo

| Fonctionnalité | Expo (React Native) | Kotlin Android | Avantages Kotlin |
|---|---|---|---|
| WebSocket | ✅ Socket.IO + WebSocket | ✅ WebSocket natif | Plus stable, moins de dépendances |
| Notifications | ✅ Expo Notifications | ✅ Android natif | Meilleur contrôle, plus de personnalisation |
| Service arrière-plan | ✅ BackgroundTask | ✅ Foreground Service | Plus fiable, moins de limitations |
| Stockage local | ✅ AsyncStorage | ✅ SharedPreferences + Room | Plus performant, typé |
| Permissions | ✅ Expo Permissions | ✅ Android natif | Contrôle granulaire |
| Performance | ⚡ Bon | ⚡⚡ Excellent | Natif, pas d'interprétation JS |

## Développement

### Structure des commits

```
feat: nouvelle fonctionnalité
fix: correction de bug
docs: documentation
style: formatage
refactor: refactoring
test: tests
chore: maintenance
```

### Tests

```bash
./gradlew test           # Tests unitaires
./gradlew connectedTest  # Tests d'instrumentation
```

## Licence

MIT License - Voir le fichier LICENSE pour plus de détails.

## Support

Pour toute question ou problème :
1. Vérifier la documentation
2. Consulter les logs de debug
3. Tester sur un appareil physique
4. Vérifier la configuration réseau

---

**Développé avec ❤️ en Kotlin pour Android**
