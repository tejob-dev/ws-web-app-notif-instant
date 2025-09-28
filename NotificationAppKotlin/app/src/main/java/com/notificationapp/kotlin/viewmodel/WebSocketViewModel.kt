package com.notificationapp.kotlin.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.notificationapp.kotlin.model.Message
import com.notificationapp.kotlin.service.WebSocketService
import kotlinx.coroutines.launch

/**
 * ViewModel pour la gestion WebSocket (basé sur le projet Kotlin-Websocket)
 * Utilise LiveData pour l'observation des changements d'état
 */
class WebSocketViewModel(application: Application) : AndroidViewModel(application) {
    
    private val webSocketService = WebSocketService()
    
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
    
    init {
        // Initialiser le statut
        _statusText.value = "Déconnecté"
        _connectionStatus.value = false
        
        // Configurer les callbacks du service WebSocket
        setupWebSocketCallbacks()
    }
    
    /**
     * Configurer les callbacks du service WebSocket
     */
    private fun setupWebSocketCallbacks() {
        webSocketService.setOnConnectionStatusListener { isConnected ->
            _connectionStatus.value = isConnected
            _statusText.value = if (isConnected) "Connecté" else "Déconnecté"
        }
        
        webSocketService.setOnMessageListener { message ->
            // Ajouter le nouveau message à la liste
            val currentMessages = _messages.value?.toMutableList() ?: mutableListOf()
            currentMessages.add(0, message) // Ajouter au début
            _messages.value = currentMessages
        }
        
        webSocketService.setOnErrorListener { error ->
            _error.value = error
        }
    }
    
    /**
     * Se connecter au serveur WebSocket
     */
    fun connect() {
        viewModelScope.launch {
            try {
                _statusText.value = "Connexion en cours..."
                val success = webSocketService.connect()
                
                if (!success) {
                    _error.value = "Impossible de se connecter au serveur WebSocket"
                    _statusText.value = "Erreur de connexion"
                }
            } catch (e: Exception) {
                _error.value = "Erreur lors de la connexion: ${e.message}"
                _statusText.value = "Erreur de connexion"
            }
        }
    }
    
    /**
     * Se déconnecter du serveur WebSocket
     */
    fun disconnect() {
        viewModelScope.launch {
            try {
                webSocketService.disconnect()
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
                webSocketService.sendMessage(message)
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
    
    override fun onCleared() {
        super.onCleared()
        webSocketService.disconnect()
    }
}
