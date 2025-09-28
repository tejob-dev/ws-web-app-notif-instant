# Configuration de l'environnement de développement

## Prérequis système

### Android Studio
- **Version minimale** : Android Studio Hedgehog (2023.1.1) ou plus récent
- **JDK** : JDK 11 ou plus récent (inclus avec Android Studio)
- **SDK Android** : API 23 (Android 6.0) minimum, API 34 (Android 14) recommandé

### Outils requis
```bash
# Vérifier les outils installés
java -version          # Doit être 11+
adb --version         # Android Debug Bridge
gradle --version      # Gradle 8.2+
```

## Configuration du projet

### 1. Cloner et ouvrir le projet
```bash
cd NotificationAppKotlin
# Ouvrir dans Android Studio : File → Open → Sélectionner le dossier
```

### 2. Configuration SDK
Dans Android Studio :
1. File → Project Structure → SDK Location
2. Vérifier que Android SDK est configuré
3. Installer les SDK requis : API 23, API 34

### 3. Sync du projet
1. Ouvrir le projet dans Android Studio
2. Cliquer sur "Sync Now" si demandé
3. Attendre la fin du sync Gradle

## Variables d'environnement

### Configuration locale (optionnel)
Créer un fichier `local.properties` :
```properties
# Chemin vers le SDK Android (généralement auto-détecté)
sdk.dir=/Users/[username]/Library/Android/sdk

# Configuration de développement
debug.server.url=192.168.1.71:3002
release.server.url=69.197.142.189:3002
```

## Émulateur Android

### Créer un émulateur
1. Tools → AVD Manager
2. Create Virtual Device
3. Sélectionner un appareil (ex: Pixel 7)
4. Choisir une image système (API 34 recommandé)
5. Finish

### Configuration recommandée
- **RAM** : 4GB minimum
- **Storage** : 8GB minimum
- **Graphics** : Hardware - GLES 2.0

## Appareil physique

### Activation du mode développeur
1. Paramètres → À propos du téléphone
2. Appuyer 7 fois sur "Numéro de build"
3. Retourner aux Paramètres → Options pour les développeurs
4. Activer "Débogage USB"

### Test de connexion
```bash
adb devices
# Doit afficher votre appareil avec "device"
```

## Scripts de développement

### Build et installation rapide
```bash
# Build debug
./build.sh

# Installation sur appareil
./install.sh

# Affichage des logs
./debug.sh
```

## Configuration réseau

### URLs de test
Modifier dans `AppConfig.kt` selon votre environnement :

```kotlin
// Développement local
const val SERVER_URL = "192.168.1.71:3002"

// Émulateur Android
const val SERVER_URL = "10.0.2.2:3002"

// Production
const val SERVER_URL = "69.197.142.189:3002"
```

### Test de connectivité
```bash
# Test ping vers le serveur
ping 192.168.1.71

# Test port WebSocket
telnet 192.168.1.71 3002
```

## Dépannage

### Problèmes courants

#### Gradle sync échoue
```bash
# Nettoyer le cache Gradle
./gradlew clean
rm -rf .gradle/
```

#### Émulateur lent
- Activer Hardware Acceleration (HAXM/KVM)
- Augmenter la RAM allouée
- Utiliser une image système x86_64

#### ADB ne détecte pas l'appareil
```bash
# Redémarrer ADB
adb kill-server
adb start-server
adb devices
```

#### Build échoue
1. Vérifier la version JDK (doit être 11+)
2. Nettoyer le projet : Build → Clean Project
3. Invalider les caches : File → Invalidate Caches and Restart

### Logs utiles

#### Logs Gradle
```bash
./gradlew build --info
```

#### Logs Android
```bash
adb logcat | grep -E "(NotificationApp|WebSocket|Notification)"
```

## Optimisations de développement

### Performance Android Studio
Dans `studio.vmoptions` :
```
-Xmx4g
-XX:MaxMetaspaceSize=512m
-XX:+UseG1GC
```

### Build rapide
```gradle
# Dans gradle.properties
org.gradle.daemon=true
org.gradle.parallel=true
org.gradle.caching=true
```

## Extensions recommandées

### Plugins Android Studio
- **Kotlin** (inclus)
- **Android Kotlin Extensions** (inclus)
- **Git Integration** (inclus)

### Outils externes
- **Postman** - Test des APIs
- **Wireshark** - Analyse réseau
- **Scrcpy** - Miroir d'écran Android

---

**Environnement configuré avec succès ! 🚀**
