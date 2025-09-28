package com.notificationapp.kotlin.config

/**
 * Configuration de l'application NotificationApp Kotlin
 * Reproduit la configuration de l'app Expo
 */
object AppConfig {
    
    // URL du serveur WebSocket (modifiez selon votre serveur)
    const val SERVER_URL = "192.168.1.71:3002"
    
    // Configuration réseau
    object Network {
        // Adresses alternatives à essayer (WebSocket natif)
        val FALLBACK_URLS = listOf(
            //"192.168.1.71:5023",  // PRIORITÉ - Docker WebSocket natif
            //"192.168.1.71:3002",  // Appareil physique (port par défaut)
            //"10.0.2.2:5023",      // Émulateur Android Docker
            //"10.0.2.2:3002",      // Émulateur Android (port par défaut)
            //"localhost:5023",      // Développement local Docker
            //"localhost:3002",      // Développement local (port par défaut)
            //"127.0.0.1:5023",      // Localhost Docker alternatif
            //"127.0.0.1:3002",      // Localhost alternatif (port par défaut)
            "69.197.142.189:5023", // Production Docker WebSocket natif
            //"69.197.142.189:3002", // Production WebSocket natif (port par défaut)
            //"192.168.1.71:3001",   // Fallback Socket.IO physique
            //"10.0.2.2:3001",      // Fallback Socket.IO émulateur
            //"69.197.142.189:5022", // Production Socket.IO (fallback)
        )
        
        // URLs Socket.IO spécifiques
        val SOCKETIO_URLS = listOf(
//            "http://192.168.1.71:3001",   // Local Socket.IO (PRIORITÉ)
//            "http://10.0.2.2:3001",       // Émulateur Socket.IO
//            "http://localhost:3001",      // Développement local Socket.IO
            "http://69.197.142.189:5022", // Production Socket.IO
        )
        
        // Timeout de connexion (augmenté pour la production)
        const val CONNECTION_TIMEOUT = 15000L
    }
    
    // Configuration des notifications
    object Notifications {
        const val CHANNEL_ID = "notification-channel"
        const val CHANNEL_NAME = "Notifications en temps réel"
        const val CHANNEL_DESCRIPTION = "Canal pour les notifications de l'application"
        const val PERSISTENT_NOTIFICATION_ID = 1001
        const val MESSAGE_NOTIFICATION_ID_BASE = 2000
    }
    
    // Configuration du polling local
    object Polling {
        const val INTERVAL = 30000L // 30 secondes
        const val MAX_RETRIES = 5
        const val RETRY_DELAY = 5000L // 5 secondes
    }
    
    // Configuration WebSocket
    object WebSocket {
        const val TIMEOUT = 20000L
        const val RECONNECTION_DELAY = 5000L
        const val RECONNECTION_ATTEMPTS = 5
    }
    
    // Configuration des tâches en arrière-plan
    object BackgroundTasks {
        const val MINIMUM_INTERVAL = 15000L // 15 secondes
        const val STOP_ON_TERMINATE = false
        const val START_ON_BOOT = true
    }
    
    // Configuration de la base de données
    object Database {
        const val DATABASE_NAME = "notification_app.db"
        const val DATABASE_VERSION = 1
        const val MAX_MESSAGES_STORED = 100
    }
    
    // Configuration des préférences
    object Preferences {
        const val PREF_NAME = "notification_app_prefs"
        const val KEY_DEVICE_ID = "device_id"
        const val KEY_SERVICE_RUNNING = "service_running"
        const val KEY_LAST_CONNECTION = "last_connection"
        const val KEY_NOTIFICATION_ENABLED = "notification_enabled"
    }
}
