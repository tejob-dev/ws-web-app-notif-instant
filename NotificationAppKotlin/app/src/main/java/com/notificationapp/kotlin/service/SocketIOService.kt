package com.notificationapp.kotlin.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.notificationapp.kotlin.config.AppConfig
import com.notificationapp.kotlin.model.*
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.*
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Service Socket.IO pour Android
 * Alternative au WebSocketService natif
 */
class SocketIOService : Service() {
    
    private val binder = SocketIOBinder()
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private var socket: Socket? = null
    private val isConnected = AtomicBoolean(false)
    private var reconnectAttempts = 0
    private val maxReconnectAttempts = AppConfig.WebSocket.RECONNECTION_ATTEMPTS
    private val reconnectDelay = AppConfig.WebSocket.RECONNECTION_DELAY
    
    private val messageHandlers = mutableMapOf<String, (WebSocketEvent) -> Unit>()
    private var connectionStatusCallback: ((ConnectionStatus) -> Unit)? = null
    
    // Callbacks pour le ViewModel
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
    
    inner class SocketIOBinder : Binder() {
        fun getService(): SocketIOService = this@SocketIOService
    }
    
    override fun onBind(intent: Intent?): IBinder = binder
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "🚀 Service Socket.IO créé")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "📡 Service Socket.IO démarré")
        return START_STICKY
    }
    
    /**
     * Se connecter au serveur Socket.IO
     */
    suspend fun connect(): Boolean {
        try {
            Log.d(TAG, "🔗 Tentative de connexion Socket.IO...")
            
            val urlsToTry = AppConfig.Network.SOCKETIO_URLS
            
            Log.d(TAG, "🔗 URLs Socket.IO à essayer: $urlsToTry")
            
            for (url in urlsToTry) {
                try {
                    Log.d(TAG, "🔗 Tentative de connexion Socket.IO: $url")
                    
                    val success = connectToUrlDirect(url)
                    if (success) {
                        Log.d(TAG, "✅ Connexion Socket.IO réussie: $url")
                        reconnectAttempts = 0
                        return true
                    }
                } catch (error: Exception) {
                    Log.w(TAG, "❌ Échec de connexion à $url: ${error.message}")
                    continue
                }
            }
            
            Log.e(TAG, "❌ Impossible de se connecter à aucun serveur Socket.IO")
            return false
            
        } catch (error: Exception) {
            Log.e(TAG, "❌ Erreur lors de la connexion Socket.IO", error)
            return false
        }
    }
    
    /**
     * Connexion directe à une URL Socket.IO
     */
    private suspend fun connectToUrlDirect(url: String): Boolean {
        // Crée un objet pour signaler la réussite ou l'échec de l'opération
        val deferred = CompletableDeferred<Boolean>()
        
        try {
            val options = IO.Options().apply {
                timeout = 15000
                reconnection = false // On gère la reconnexion manuellement
                forceNew = true
            }
            
            socket = IO.socket(url, options)
            
            // Configurer les événements Socket.IO
            socket?.on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "✅ Socket.IO connecté avec succès vers $url")
                isConnected.set(true)
                setupEventHandlers()
                notifyConnectionStatus(true)
                connectionStatusListener?.invoke(true)
                
                // Signale la réussite de l'opération
                if (!deferred.isCompleted) {
                    deferred.complete(true)
                }
            }
            
            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                val error = args[0] as? Exception
                Log.e(TAG, "❌ Échec Socket.IO vers $url: ${error?.message}")
                isConnected.set(false)
                notifyConnectionStatus(false)
                connectionStatusListener?.invoke(false)
                errorListener?.invoke("Erreur de connexion Socket.IO: ${error?.message}")
                
                // Signale l'échec de l'opération
                if (!deferred.isCompleted) {
                    deferred.complete(false)
                }
            }
            
            socket?.on(Socket.EVENT_DISCONNECT) { args ->
                Log.d(TAG, "🔌 Socket.IO déconnecté de $url")
                isConnected.set(false)
                notifyConnectionStatus(false)
                connectionStatusListener?.invoke(false)
            }
            
            socket?.on("error") { args ->
                val error = args[0] as? Exception
                Log.e(TAG, "❌ Erreur Socket.IO: ${error?.message}")
                isConnected.set(false)
                notifyConnectionStatus(false)
                connectionStatusListener?.invoke(false)
                errorListener?.invoke("Erreur Socket.IO: ${error?.message}")
            }
            
            // Écouter les messages personnalisés
            socket?.on("message") { args ->
                try {
                    val data = args[0] as? String
                    if (data != null) {
                        Log.d(TAG, "📨 Message Socket.IO reçu: $data")
                        handleMessage(data)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Erreur lors du traitement du message Socket.IO", e)
                }
            }
            
            socket?.on("notification") { args ->
                try {
                    val data = args[0] as? String
                    if (data != null) {
                        Log.d(TAG, "🔔 Notification Socket.IO reçue: $data")
                        handleMessage(data)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Erreur lors du traitement de la notification Socket.IO", e)
                }
            }
            
            // Se connecter
            socket?.connect()
            
            // Utilisez withTimeoutOrNull pour attendre le résultat avec un délai maximum
            return withTimeoutOrNull(AppConfig.Network.CONNECTION_TIMEOUT.toLong()) {
                deferred.await()
            } ?: false // Retourne false si le délai est dépassé (Timeout)
            
        } catch (error: Exception) {
            Log.e(TAG, "❌ Erreur lors de la connexion à $url", error)
            return false
        }
    }
    
    /**
     * Configurer les gestionnaires d'événements
     */
    private fun setupEventHandlers() {
        Log.d(TAG, "🔧 Gestionnaires d'événements Socket.IO configurés")
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
            
            Log.d(TAG, "📨 Message Socket.IO traité: $event")
            
            // Créer un objet Message pour le ViewModel
            val message = Message(
                id = System.currentTimeMillis().toString(),
                content = jsonObject.optString("content", messageText),
                timestamp = System.currentTimeMillis().toString(),
                type = event.type
            )
            
            // Notifier le ViewModel
            messageListener?.invoke(message)
            
            // Appeler les gestionnaires enregistrés
            messageHandlers[event.type]?.invoke(event)
            messageHandlers["message"]?.invoke(event)
            
        } catch (error: Exception) {
            Log.e(TAG, "❌ Erreur lors du parsing du message Socket.IO", error)
        }
    }
    
    /**
     * Enregistrer un token mobile sur le serveur
     */
    fun registerMobileToken(tokenData: MobileToken) {
        if (!isConnected.get() || socket == null) {
            Log.w(TAG, "⚠️ Socket.IO non connecté - impossible d'enregistrer le token")
            return
        }
        
        try {
            val tokenDataJson = JSONObject().apply {
                put("token", tokenData.token)
                put("platform", tokenData.platform)
                put("deviceId", tokenData.deviceId)
                put("timestamp", tokenData.timestamp)
            }
            
            socket?.emit("register-mobile-token", tokenDataJson)
            Log.d(TAG, "📱 Token mobile envoyé au serveur Socket.IO")
        } catch (error: Exception) {
            Log.e(TAG, "❌ Erreur lors de l'enregistrement du token Socket.IO", error)
        }
    }
    
    /**
     * Programmer une reconnexion
     */
    private fun scheduleReconnect() {
        if (reconnectAttempts >= maxReconnectAttempts) {
            Log.w(TAG, "❌ Nombre maximum de tentatives de reconnexion Socket.IO atteint")
            return
        }
        
        reconnectAttempts++
        val delay = reconnectDelay * (1 shl (reconnectAttempts - 1)) // Backoff exponentiel
        
        Log.d(TAG, "🔄 Reconnexion Socket.IO automatique dans ${delay}ms (tentative $reconnectAttempts/$maxReconnectAttempts)")
        
        serviceScope.launch {
            delay(delay)
            if (!isConnected.get()) {
                try {
                    connect()
                } catch (error: Exception) {
                    Log.e(TAG, "❌ Échec de la reconnexion Socket.IO", error)
                }
            }
        }
    }
    
    /**
     * Envoyer un message
     */
    fun send(message: String) {
        if (!isConnected.get() || socket == null) {
            throw Exception("Socket.IO non connecté")
        }
        
        try {
            socket?.emit("message", message)
            Log.d(TAG, "📤 Message Socket.IO envoyé: $message")
        } catch (error: Exception) {
            Log.e(TAG, "❌ Erreur lors de l'envoi du message Socket.IO", error)
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
        socket?.disconnect()
        socket = null
        isConnected.set(false)
        messageHandlers.clear()
        notifyConnectionStatus(false)
        Log.d(TAG, "🔌 Socket.IO déconnecté")
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
        if (isConnected.get() && socket != null) {
            try {
                socket?.emit("message", message)
                Log.d(TAG, "📤 Message Socket.IO envoyé: $message")
            } catch (error: Exception) {
                Log.e(TAG, "❌ Erreur lors de l'envoi du message Socket.IO", error)
                errorListener?.invoke("Erreur d'envoi Socket.IO: ${error.message}")
            }
        } else {
            Log.w(TAG, "⚠️ Socket.IO non connecté, impossible d'envoyer le message")
            errorListener?.invoke("Socket.IO non connecté")
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
        Log.d(TAG, "🛑 Service Socket.IO détruit")
        disconnect()
        serviceScope.cancel()
    }
    
    companion object {
        private const val TAG = "SocketIOService"
    }
}
