package com.notificationapp.kotlin.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.notificationapp.kotlin.config.AppConfig
import com.notificationapp.kotlin.model.*
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Service en arrière-plan pour gérer les notifications
 * Reproduit les fonctionnalités du BackgroundService.js Expo
 */
class BackgroundService : Service() {
    
    private val binder = BackgroundServiceBinder()
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private var webSocketService: WebSocketService? = null
    private var notificationService: NotificationService? = null
    private val isServiceRunning = AtomicBoolean(false)
    private var deviceId: String? = null
    
    companion object {
        private const val TAG = "BackgroundService"
    }
    
    inner class BackgroundServiceBinder : Binder() {
        fun getService(): BackgroundService = this@BackgroundService
    }
    
    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "🔗 Service lié - onBind")
        return binder
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "🚀 Service en arrière-plan créé")
        initializeServices()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "📡 Service en arrière-plan démarré - onStartCommand")
        startForegroundService()
        return START_STICKY
    }
    
    /**
     * Initialiser les services
     */
    private fun initializeServices() {
        try {
            // Initialiser le service de notifications
            notificationService = NotificationService(this)
            
            // Initialiser le service WebSocket
            webSocketService = WebSocketService()
            
            Log.d(TAG, "✅ Services initialisés")
        } catch (error: Exception) {
            Log.e(TAG, "❌ Erreur lors de l'initialisation des services", error)
        }
    }
    
    /**
     * Démarrer le service en premier plan
     */
    private fun startForegroundService() {
        try {
            // Créer une notification persistante pour le service en premier plan
            notificationService?.createPersistentNotification()
            
            // Démarrer le service WebSocket
            serviceScope.launch {
                startWebSocketService()
            }
            
            isServiceRunning.set(true)
            Log.d(TAG, "✅ Service en arrière-plan démarré avec succès")
        } catch (error: Exception) {
            Log.e(TAG, "❌ Erreur lors du démarrage du service", error)
        }
    }
    
    /**
     * Démarrer le service WebSocket
     */
    private suspend fun startWebSocketService() {
        try {
            val webSocket = webSocketService ?: return
            
            // Se connecter au serveur WebSocket
            val connected = webSocket.connect()
            if (connected) {
                Log.d(TAG, "✅ Service WebSocket démarré avec succès")
                
                // Configurer les gestionnaires d'événements
                setupWebSocketHandlers()
                
                // Enregistrer le token mobile
                registerMobileToken()
            } else {
                Log.w(TAG, "⚠️ Impossible de démarrer le service WebSocket")
            }
        } catch (error: Exception) {
            Log.e(TAG, "❌ Erreur lors du démarrage du service WebSocket", error)
        }
    }
    
    /**
     * Configurer les gestionnaires d'événements WebSocket
     */
    private fun setupWebSocketHandlers() {
        val webSocket = webSocketService ?: return
        
        // Gestionnaire pour les messages généraux
        webSocket.on("message") { event ->
            handleWebSocketMessage(event)
        }
        
        // Gestionnaire pour les notifications spécifiques
        webSocket.on("notification") { event ->
            handleNotificationMessage(event)
        }
        
        // Gestionnaire pour les événements de connexion
        webSocket.on("connect") { event ->
            Log.d(TAG, "🔗 Connecté au serveur WebSocket")
        }
        
        // Gestionnaire pour les événements de déconnexion
        webSocket.on("disconnect") { event ->
            Log.d(TAG, "❌ Déconnecté du serveur WebSocket")
        }
        
        // Callback pour les changements de statut de connexion
        webSocket.setConnectionStatusCallback { status ->
            Log.d(TAG, "📊 Statut de connexion: $status")
        }
    }
    
    /**
     * Gérer les messages WebSocket
     */
    private fun handleWebSocketMessage(event: WebSocketEvent) {
        try {
            Log.d(TAG, "📨 Message WebSocket reçu: $event")
            
            // Convertir l'événement en message
            val message = Message(
                id = event.data?.toString() ?: "unknown-${System.currentTimeMillis()}",
                content = event.data?.toString() ?: "Message vide",
                type = event.type,
                timestamp = java.time.Instant.now().toString()
            )
            
            // Traiter le message via le service de notifications
            notificationService?.handleWebSocketMessage(message)
            
        } catch (error: Exception) {
            Log.e(TAG, "❌ Erreur lors du traitement du message WebSocket", error)
        }
    }
    
    /**
     * Gérer les messages de notification spécifiques
     */
    private fun handleNotificationMessage(event: WebSocketEvent) {
        try {
            Log.d(TAG, "🔔 Message de notification reçu: $event")
            
            // Traiter comme un message WebSocket normal
            handleWebSocketMessage(event)
            
        } catch (error: Exception) {
            Log.e(TAG, "❌ Erreur lors du traitement du message de notification", error)
        }
    }
    
    /**
     * Enregistrer le token mobile
     */
    private fun registerMobileToken() {
        try {
            val webSocket = webSocketService ?: return
            val deviceId = getUniqueDeviceId()
            
            val tokenData = MobileToken(
                token = "mobile-token-$deviceId",
                platform = "android",
                deviceId = deviceId
            )
            
            webSocket.registerMobileToken(tokenData)
            Log.d(TAG, "📱 Token mobile enregistré")
        } catch (error: Exception) {
            Log.e(TAG, "❌ Erreur lors de l'enregistrement du token mobile", error)
        }
    }
    
    /**
     * Obtenir l'ID unique de l'appareil
     */
    private fun getUniqueDeviceId(): String {
        if (deviceId == null) {
            val prefs = getSharedPreferences(AppConfig.Preferences.PREF_NAME, MODE_PRIVATE)
            deviceId = prefs.getString(AppConfig.Preferences.KEY_DEVICE_ID, null)
            
            if (deviceId == null) {
                deviceId = "${android.os.Build.MODEL}-${System.currentTimeMillis()}"
                prefs.edit().putString(AppConfig.Preferences.KEY_DEVICE_ID, deviceId).apply()
            }
        }
        return deviceId ?: "unknown-device"
    }
    
    /**
     * Démarrer le service
     */
    suspend fun start(): Boolean {
        return try {
            Log.d(TAG, "🚀 Démarrage du service en arrière-plan...")
            
            if (!isServiceRunning.get()) {
                startForegroundService()
            }
            
            Log.d(TAG, "✅ Service démarré avec succès")
            true
        } catch (error: Exception) {
            Log.e(TAG, "❌ Erreur lors du démarrage du service", error)
            false
        }
    }
    
    /**
     * Arrêter le service
     */
    suspend fun stop(): Boolean {
        return try {
            Log.d(TAG, "🛑 Arrêt du service en arrière-plan...")
            
            // Arrêter le service WebSocket
            webSocketService?.disconnect()
            
            // Effacer les notifications
            notificationService?.clearAllNotifications()
            
            isServiceRunning.set(false)
            Log.d(TAG, "✅ Service arrêté avec succès")
            true
        } catch (error: Exception) {
            Log.e(TAG, "❌ Erreur lors de l'arrêt du service", error)
            false
        }
    }
    
    /**
     * Obtenir le statut du service
     */
    fun getStatus(): Map<String, Any> {
        val webSocketStatus = webSocketService?.getStatus()
        val notificationStatus = notificationService?.getServiceStatus()
        
        return mapOf(
            "isRegistered" to isServiceRunning.get(),
            "isPolling" to false, // Pas de polling dans cette implémentation
            "notificationStatus" to mapOf(
                "isConnected" to (webSocketStatus?.isConnected ?: false),
                "deviceId" to deviceId
            ),
            "webSocketStatus" to (webSocketStatus ?: mapOf<String, Any>()),
            "notificationServiceStatus" to (notificationStatus ?: mapOf<String, Any>())
        )
    }
    
    /**
     * Obtenir les informations de l'appareil
     */
    fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            deviceId = getUniqueDeviceId(),
            platform = "android",
            modelName = android.os.Build.MODEL,
            brand = android.os.Build.BRAND,
            osVersion = android.os.Build.VERSION.RELEASE,
            isServiceRunning = isServiceRunning.get(),
            isPolling = false,
            compatibility = CompatibilityInfo(
                compatible = true,
                message = "Appareil compatible avec les notifications",
                features = listOf(
                    "WebSocket natif",
                    "Notifications push",
                    "Service en arrière-plan",
                    "Stockage local"
                )
            )
        )
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "🛑 Service en arrière-plan détruit")
        serviceScope.launch {
            stop()
        }
        serviceScope.cancel()
    }
}
