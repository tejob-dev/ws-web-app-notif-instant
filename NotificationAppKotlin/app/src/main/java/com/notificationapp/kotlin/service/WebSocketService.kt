package com.notificationapp.kotlin.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.notificationapp.kotlin.config.AppConfig
import com.notificationapp.kotlin.model.*
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Service WebSocket natif pour Android
 * Reproduit les fonctionnalités du WebSocketService.js Expo
 */
class WebSocketService : Service() {
    
    private val binder = WebSocketBinder()
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private var webSocket: WebSocket? = null
    private var okHttpClient: OkHttpClient? = null
    private val isConnected = AtomicBoolean(false)
    private var reconnectAttempts = 0
    private val maxReconnectAttempts = AppConfig.WebSocket.RECONNECTION_ATTEMPTS
    private val reconnectDelay = AppConfig.WebSocket.RECONNECTION_DELAY
    
    private val messageHandlers = mutableMapOf<String, (WebSocketEvent) -> Unit>()
    private var connectionStatusCallback: ((ConnectionStatus) -> Unit)? = null
    
    // Callbacks pour le ViewModel (basé sur le projet Kotlin-Websocket)
    private var connectionStatusListener: ((Boolean) -> Unit)? = null
    private var messageListener: ((Message) -> Unit)? = null
    private var errorListener: ((String) -> Unit)? = null
    
    /**
     * Définir le listener pour le statut de connexion
     */
    fun setOnConnectionStatusListener(listener: (Boolean) -> Unit) {
        connectionStatusListener = listener
    }
    
    /**
     * Définir le listener pour les messages
     */
    fun setOnMessageListener(listener: (Message) -> Unit) {
        messageListener = listener
    }
    
    /**
     * Définir le listener pour les erreurs
     */
    fun setOnErrorListener(listener: (String) -> Unit) {
        errorListener = listener
    }
    
    inner class WebSocketBinder : Binder() {
        fun getService(): WebSocketService = this@WebSocketService
    }
    
    override fun onBind(intent: Intent?): IBinder = binder
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "🚀 Service WebSocket créé")
        initializeOkHttpClient()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "📡 Service WebSocket démarré")
        return START_STICKY
    }
    
    /**
     * Initialiser le client OkHttp pour WebSocket
     */
    private fun initializeOkHttpClient() {
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d(TAG, "WebSocket: $message")
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        okHttpClient = OkHttpClient.Builder()
            .connectTimeout(AppConfig.Network.CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
            .readTimeout(AppConfig.Network.CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
            .writeTimeout(AppConfig.Network.CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
            .addInterceptor(loggingInterceptor)
            .build()
    }
    
    /**
     * Se connecter au serveur WebSocket (approche simplifiée basée sur le projet de référence)
     */
    suspend fun connect(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔗 Tentative de connexion WebSocket...")
                
                // URLs à essayer (basé sur le projet de référence)
                val urlsToTry = listOf(
                    "ws://192.168.1.71:3002",
                    "ws://10.0.2.2:3002", 
                    "ws://localhost:3002",
                    "ws://127.0.0.1:3002",
                    "ws://69.197.142.189:3002"
                )
                
                Log.d(TAG, "🔗 URLs WebSocket à essayer: $urlsToTry")
                
                for (url in urlsToTry) {
                    try {
                        Log.d(TAG, "🔗 Tentative de connexion WebSocket: $url")
                        
                        val success = connectToUrlDirect(url)
                        if (success) {
                            Log.d(TAG, "✅ Connexion WebSocket réussie: $url")
                            reconnectAttempts = 0
                            return@withContext true
                        }
                    } catch (error: Exception) {
                        Log.w(TAG, "❌ Échec de connexion à $url: ${error.message}")
                        continue
                    }
                }
                
                Log.e(TAG, "❌ Impossible de se connecter à aucun serveur WebSocket")
                false
                
            } catch (error: Exception) {
                Log.e(TAG, "❌ Erreur lors de la connexion WebSocket", error)
                false
            }
        }
    }
    
    /**
     * Connexion directe à une URL WebSocket (basé sur le projet de référence)
     */
    private suspend fun connectToUrlDirect(url: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url(url)
                    .build()
                
                var connectionEstablished = false
                
                webSocket = okHttpClient?.newWebSocket(request, object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        Log.d(TAG, "✅ WebSocket ouvert avec succès vers $url")
                        isConnected.set(true)
                        connectionEstablished = true
                        setupEventHandlers()
                        notifyConnectionStatus(true)
                        
                        // Notifier le ViewModel
                        connectionStatusListener?.invoke(true)
                    }
                    
                    override fun onMessage(webSocket: WebSocket, text: String) {
                        Log.d(TAG, "📨 Message WebSocket reçu: $text")
                        handleMessage(text)
                    }
                    
                    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                        Log.d(TAG, "🔌 WebSocket fermé vers $url: code=$code, reason=$reason")
                        isConnected.set(false)
                        notifyConnectionStatus(false)
                        
                        // Notifier le ViewModel
                        connectionStatusListener?.invoke(false)
                    }
                    
                    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                        Log.d(TAG, "🔌 WebSocket fermé définitivement vers $url: code=$code, reason=$reason")
                        isConnected.set(false)
                        notifyConnectionStatus(false)
                        
                        // Notifier le ViewModel
                        connectionStatusListener?.invoke(false)
                    }
                    
                    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                        Log.e(TAG, "❌ Échec WebSocket vers $url: ${t.message}")
                        isConnected.set(false)
                        notifyConnectionStatus(false)
                        
                        // Notifier le ViewModel
                        connectionStatusListener?.invoke(false)
                        errorListener?.invoke("Erreur de connexion: ${t.message}")
                    }
                })
                
                // Attendre un peu pour voir si la connexion s'établit
                delay(3000)
                
                connectionEstablished
                
            } catch (error: Exception) {
                Log.e(TAG, "❌ Erreur lors de la connexion à $url", error)
                false
            }
        }
    }
    
    /**
     * Connexion à une URL spécifique
     */
    private suspend fun connectToUrl(url: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("$WS_PROTOCOL$url")
                    .build()
                
                webSocket = okHttpClient?.newWebSocket(request, object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        Log.d(TAG, "✅ WebSocket ouvert avec succès vers $url")
                        isConnected.set(true)
                        setupEventHandlers()
                        notifyConnectionStatus(true)
                    }
                    
                    override fun onMessage(webSocket: WebSocket, text: String) {
                        Log.d(TAG, "📨 Message WebSocket reçu: $text")
                        handleMessage(text)
                    }
                    
                    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                        Log.d(TAG, "🔌 WebSocket fermé vers $url: code=$code, reason=$reason")
                        isConnected.set(false)
                        notifyConnectionStatus(false)
                    }
                    
                    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                        Log.d(TAG, "🔌 WebSocket fermé définitivement vers $url: code=$code, reason=$reason")
                        isConnected.set(false)
                        notifyConnectionStatus(false)
                        
                        // Codes d'erreur courants
                        when (code) {
                            1006 -> Log.d(TAG, "💡 Code 1006: Connexion fermée anormalement (pas de frame de fermeture)")
                            1000 -> Log.d(TAG, "💡 Code 1000: Connexion fermée normalement")
                            1001 -> Log.d(TAG, "💡 Code 1001: Connexion fermée (endpoint part)")
                        }
                        
                        // Tentative de reconnexion automatique seulement si ce n'est pas une fermeture normale
                        if (code != 1000) {
                            scheduleReconnect()
                        }
                    }
                    
                    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                        Log.e(TAG, "❌ Erreur WebSocket vers $url", t)
                        isConnected.set(false)
                        notifyConnectionStatus(false)
                        scheduleReconnect()
                    }
                })
                
                // Attendre un peu pour voir si la connexion réussit
                delay(AppConfig.Network.CONNECTION_TIMEOUT)
                return@withContext isConnected.get()
                
            } catch (error: Exception) {
                Log.e(TAG, "❌ Erreur lors de la création de la connexion vers $url", error)
                return@withContext false
            }
        }
    }
    
    /**
     * Configurer les gestionnaires d'événements
     */
    private fun setupEventHandlers() {
        // Les gestionnaires sont déjà configurés dans WebSocketListener
        Log.d(TAG, "🔧 Gestionnaires d'événements WebSocket configurés")
    }
    
    /**
     * Gérer les messages reçus
     */
    private fun handleMessage(messageText: String) {
        try {
            val jsonObject = JSONObject(messageText)
            val event = WebSocketEvent(
                type = jsonObject.optString("type", "message"),
                data = jsonObject.opt("data"),
                timestamp = System.currentTimeMillis()
            )
            
            Log.d(TAG, "📨 Message WebSocket traité: $event")
            
            // Appeler les gestionnaires enregistrés
            messageHandlers[event.type]?.invoke(event)
            messageHandlers["message"]?.invoke(event)
            
        } catch (error: Exception) {
            Log.e(TAG, "❌ Erreur lors du parsing du message", error)
        }
    }
    
    /**
     * Enregistrer un token mobile sur le serveur
     */
    fun registerMobileToken(tokenData: MobileToken) {
        if (!isConnected.get() || webSocket == null) {
            Log.w(TAG, "⚠️ WebSocket non connecté - impossible d'enregistrer le token")
            return
        }
        
        try {
            val message = JSONObject().apply {
                put("type", "register-mobile-token")
                put("data", JSONObject().apply {
                    put("token", tokenData.token)
                    put("platform", tokenData.platform)
                    put("deviceId", tokenData.deviceId)
                    put("timestamp", tokenData.timestamp)
                })
            }
            
            send(message.toString())
            Log.d(TAG, "📱 Token mobile envoyé au serveur WebSocket natif")
        } catch (error: Exception) {
            Log.e(TAG, "❌ Erreur lors de l'enregistrement du token", error)
        }
    }
    
    /**
     * Programmer une reconnexion
     */
    private fun scheduleReconnect() {
        if (reconnectAttempts >= maxReconnectAttempts) {
            Log.w(TAG, "❌ Nombre maximum de tentatives de reconnexion atteint")
            return
        }
        
        reconnectAttempts++
        val delay = reconnectDelay * (1 shl (reconnectAttempts - 1)) // Backoff exponentiel
        
        Log.d(TAG, "🔄 Reconnexion automatique dans ${delay}ms (tentative $reconnectAttempts/$maxReconnectAttempts)")
        
        serviceScope.launch {
            delay(delay)
            if (!isConnected.get()) {
                try {
                    connect()
                } catch (error: Exception) {
                    Log.e(TAG, "❌ Échec de la reconnexion", error)
                }
            }
        }
    }
    
    /**
     * Envoyer un message
     */
    fun send(message: String) {
        if (!isConnected.get() || webSocket == null) {
            throw Exception("WebSocket non connecté")
        }
        
        try {
            webSocket?.send(message)
            Log.d(TAG, "📤 Message envoyé: $message")
        } catch (error: Exception) {
            Log.e(TAG, "❌ Erreur lors de l'envoi du message", error)
            throw error
        }
    }
    
    /**
     * Enregistrer un gestionnaire d'événements
     */
    fun on(eventType: String, handler: (WebSocketEvent) -> Unit) {
        messageHandlers[eventType] = handler
    }
    
    /**
     * Désinscrire un gestionnaire d'événements
     */
    fun off(eventType: String) {
        messageHandlers.remove(eventType)
    }
    
    /**
     * Se déconnecter
     */
    fun disconnect() {
        webSocket?.close(1000, "Fermeture normale")
        webSocket = null
        isConnected.set(false)
        messageHandlers.clear()
        notifyConnectionStatus(false)
        Log.d(TAG, "🔌 WebSocket déconnecté")
    }
    
    /**
     * Obtenir le statut de connexion
     */
    fun getStatus(): ConnectionStatus {
        return ConnectionStatus(
            isConnected = isConnected.get(),
            deviceId = null, // À implémenter selon vos besoins
            lastConnectionTime = System.currentTimeMillis(),
            reconnectAttempts = reconnectAttempts
        )
    }
    
    /**
     * Notifier le changement de statut de connexion
     */
    private fun notifyConnectionStatus(connected: Boolean) {
        connectionStatusCallback?.invoke(getStatus())
    }
    
    /**
     * Envoyer un message (pour le ViewModel)
     */
    fun sendMessage(message: String) {
        if (isConnected.get() && webSocket != null) {
            try {
                webSocket?.send(message)
                Log.d(TAG, "📤 Message envoyé: $message")
            } catch (error: Exception) {
                Log.e(TAG, "❌ Erreur lors de l'envoi du message", error)
                errorListener?.invoke("Erreur d'envoi: ${error.message}")
            }
        } else {
            Log.w(TAG, "⚠️ WebSocket non connecté, impossible d'envoyer le message")
            errorListener?.invoke("WebSocket non connecté")
        }
    }
    
    /**
     * Définir le callback de statut de connexion
     */
    fun setConnectionStatusCallback(callback: (ConnectionStatus) -> Unit) {
        connectionStatusCallback = callback
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "🛑 Service WebSocket détruit")
        disconnect()
        serviceScope.cancel()
    }
    
    companion object {
        private const val TAG = "WebSocketService"
        private const val WS_PROTOCOL = "ws://"
    }
}
