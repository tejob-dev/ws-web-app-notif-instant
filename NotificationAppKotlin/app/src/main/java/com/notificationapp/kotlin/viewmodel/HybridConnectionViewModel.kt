package com.notificationapp.kotlin.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.notificationapp.kotlin.model.Message
import com.notificationapp.kotlin.service.WebSocketService
import com.notificationapp.kotlin.service.SocketIOService
import kotlinx.coroutines.launch

/**
 * ViewModel hybride pour la gestion des connexions WebSocket et Socket.IO
 * Peut basculer entre les deux technologies selon la disponibilité
 */
class HybridConnectionViewModel(application: Application) : AndroidViewModel(application) {
    
    private val webSocketService = WebSocketService()
    private val socketIOService = SocketIOService()
    
    // Type de connexion actuel (Socket.IO par défaut car plus fiable)
    private var currentConnectionType: ConnectionType = ConnectionType.SOCKETIO
    
    // LiveData pour l'état de connexion
    private val _connectionStatus = MutableLiveData<Boolean>()
    val connectionStatus: LiveData<Boolean> = _connectionStatus
    
    // LiveData pour les messages reçus
    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages
    
    // LiveData pour les erreurs
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    // LiveData pour le statut de connexion textuel
    private val _statusText = MutableLiveData<String>()
    val statusText: LiveData<String> = _statusText
    
    // LiveData pour le type de connexion actuel
    private val _connectionType = MutableLiveData<ConnectionType>()
    val connectionType: LiveData<ConnectionType> = _connectionType
    
    init {
        // Initialiser le statut
        _statusText.value = "Déconnecté"
        _connectionStatus.value = false
        _connectionType.value = currentConnectionType
        
        // Configurer les callbacks des services
        setupWebSocketCallbacks()
        setupSocketIOCallbacks()
    }
    
    /**
     * Configurer les callbacks du service WebSocket
     */
    private fun setupWebSocketCallbacks() {
        webSocketService.setOnConnectionStatusListener { isConnected ->
            if (currentConnectionType == ConnectionType.WEBSOCKET) {
                _connectionStatus.value = isConnected
                _statusText.value = if (isConnected) "Connecté (WebSocket)" else "Déconnecté"
            }
        }
        
        webSocketService.setOnMessageListener { message ->
            if (currentConnectionType == ConnectionType.WEBSOCKET) {
                addMessage(message)
            }
        }
        
        webSocketService.setOnErrorListener { error ->
            if (currentConnectionType == ConnectionType.WEBSOCKET) {
                _error.value = error
                // Essayer Socket.IO en cas d'erreur WebSocket
                trySocketIOFallback()
            }
        }
    }
    
    /**
     * Configurer les callbacks du service Socket.IO
     */
    private fun setupSocketIOCallbacks() {
        socketIOService.setOnConnectionStatusListener { isConnected ->
            if (currentConnectionType == ConnectionType.SOCKETIO) {
                _connectionStatus.value = isConnected
                _statusText.value = if (isConnected) "Connecté (Socket.IO)" else "Déconnecté"
            }
        }
        
        socketIOService.setOnMessageListener { message ->
            if (currentConnectionType == ConnectionType.SOCKETIO) {
                addMessage(message)
            }
        }
        
        socketIOService.setOnErrorListener { error ->
            if (currentConnectionType == ConnectionType.SOCKETIO) {
                _error.value = error
                // Essayer WebSocket en cas d'erreur Socket.IO
                tryWebSocketFallback()
            }
        }
    }
    
    /**
     * Ajouter un message à la liste
     */
    private fun addMessage(message: Message) {
        val currentMessages = _messages.value?.toMutableList() ?: mutableListOf()
        currentMessages.add(0, message) // Ajouter au début
        _messages.value = currentMessages
    }
    
    /**
     * Essayer Socket.IO en cas d'échec WebSocket
     */
    private fun trySocketIOFallback() {
        viewModelScope.launch {
            try {
                _statusText.value = "Tentative Socket.IO..."
                currentConnectionType = ConnectionType.SOCKETIO
                _connectionType.value = currentConnectionType
                
                val success = socketIOService.connect()
                if (!success) {
                    _error.value = "Impossible de se connecter avec Socket.IO non plus"
                    _statusText.value = "Erreur de connexion"
                }
            } catch (e: Exception) {
                _error.value = "Erreur lors du basculement vers Socket.IO: ${e.message}"
                _statusText.value = "Erreur de connexion"
            }
        }
    }
    
    /**
     * Essayer WebSocket en cas d'échec Socket.IO
     */
    private fun tryWebSocketFallback() {
        viewModelScope.launch {
            try {
                _statusText.value = "Tentative WebSocket..."
                currentConnectionType = ConnectionType.WEBSOCKET
                _connectionType.value = currentConnectionType
                
                val success = webSocketService.connect()
                if (!success) {
                    _error.value = "Impossible de se connecter avec WebSocket non plus"
                    _statusText.value = "Erreur de connexion"
                }
            } catch (e: Exception) {
                _error.value = "Erreur lors du basculement vers WebSocket: ${e.message}"
                _statusText.value = "Erreur de connexion"
            }
        }
    }
    
    /**
     * Se connecter au serveur (essaie Socket.IO directement car plus fiable)
     */
    fun connect() {
        viewModelScope.launch {
            try {
                // Commencer directement par Socket.IO car plus fiable
                _statusText.value = "Connexion Socket.IO en cours..."
                currentConnectionType = ConnectionType.SOCKETIO
                _connectionType.value = currentConnectionType
                
                val success = socketIOService.connect()
                
                if (!success) {
                    // Essayer WebSocket si Socket.IO échoue
                    _statusText.value = "Tentative WebSocket..."
                    currentConnectionType = ConnectionType.WEBSOCKET
                    _connectionType.value = currentConnectionType
                    
                    val webSocketSuccess = webSocketService.connect()
                    if (!webSocketSuccess) {
                        _error.value = "Impossible de se connecter avec Socket.IO ou WebSocket"
                        _statusText.value = "Erreur de connexion"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Erreur lors de la connexion: ${e.message}"
                _statusText.value = "Erreur de connexion"
            }
        }
    }
    
    /**
     * Se connecter spécifiquement avec Socket.IO
     */
    fun connectWithSocketIO() {
        viewModelScope.launch {
            try {
                _statusText.value = "Connexion Socket.IO en cours..."
                currentConnectionType = ConnectionType.SOCKETIO
                _connectionType.value = currentConnectionType
                
                val success = socketIOService.connect()
                
                if (!success) {
                    _error.value = "Impossible de se connecter au serveur Socket.IO"
                    _statusText.value = "Erreur de connexion Socket.IO"
                }
            } catch (e: Exception) {
                _error.value = "Erreur lors de la connexion Socket.IO: ${e.message}"
                _statusText.value = "Erreur de connexion Socket.IO"
            }
        }
    }
    
    /**
     * Se connecter spécifiquement avec WebSocket
     */
    fun connectWithWebSocket() {
        viewModelScope.launch {
            try {
                _statusText.value = "Connexion WebSocket en cours..."
                currentConnectionType = ConnectionType.WEBSOCKET
                _connectionType.value = currentConnectionType
                
                val success = webSocketService.connect()
                
                if (!success) {
                    _error.value = "Impossible de se connecter au serveur WebSocket"
                    _statusText.value = "Erreur de connexion WebSocket"
                }
            } catch (e: Exception) {
                _error.value = "Erreur lors de la connexion WebSocket: ${e.message}"
                _statusText.value = "Erreur de connexion WebSocket"
            }
        }
    }
    
    /**
     * Se déconnecter du serveur
     */
    fun disconnect() {
        viewModelScope.launch {
            try {
                when (currentConnectionType) {
                    ConnectionType.WEBSOCKET -> webSocketService.disconnect()
                    ConnectionType.SOCKETIO -> socketIOService.disconnect()
                }
                _statusText.value = "Déconnecté"
            } catch (e: Exception) {
                _error.value = "Erreur lors de la déconnexion: ${e.message}"
            }
        }
    }
    
    /**
     * Envoyer un message
     */
    fun sendMessage(message: String) {
        viewModelScope.launch {
            try {
                when (currentConnectionType) {
                    ConnectionType.WEBSOCKET -> webSocketService.sendMessage(message)
                    ConnectionType.SOCKETIO -> socketIOService.sendMessage(message)
                }
            } catch (e: Exception) {
                _error.value = "Erreur lors de l'envoi: ${e.message}"
            }
        }
    }
    
    /**
     * Effacer les messages
     */
    fun clearMessages() {
        _messages.value = emptyList()
    }
    
    /**
     * Effacer l'erreur
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * Obtenir le service actuel
     */
    fun getCurrentService(): Any {
        return when (currentConnectionType) {
            ConnectionType.WEBSOCKET -> webSocketService
            ConnectionType.SOCKETIO -> socketIOService
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        webSocketService.disconnect()
        socketIOService.disconnect()
    }
    
    /**
     * Types de connexion supportés
     */
    enum class ConnectionType {
        WEBSOCKET,
        SOCKETIO
    }
}
