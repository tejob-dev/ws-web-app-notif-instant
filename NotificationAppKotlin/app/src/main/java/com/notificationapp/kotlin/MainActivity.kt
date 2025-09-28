package com.notificationapp.kotlin

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.notificationapp.kotlin.adapter.MessageAdapter
import com.notificationapp.kotlin.config.AppConfig
import com.notificationapp.kotlin.databinding.ActivityMainBinding
import com.notificationapp.kotlin.model.*
import com.notificationapp.kotlin.service.BackgroundService
import com.notificationapp.kotlin.service.NotificationService
import com.notificationapp.kotlin.service.SocketIOService
import com.notificationapp.kotlin.service.WebSocketService
import com.notificationapp.kotlin.utils.AndroidCompatibility
import kotlinx.coroutines.launch

/**
 * Activité principale de l'application NotificationApp Kotlin
 * Reproduit l'interface utilisateur de l'app Expo
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var messageAdapter: MessageAdapter
    private var backgroundService: BackgroundService? = null
    private lateinit var notificationService: NotificationService
    private lateinit var socketIOService: SocketIOService
    private lateinit var webSocketService: WebSocketService
    
    private var isServiceRunning = false
    private var connectionStatus = "Déconnecté"
    private val messages = mutableListOf<Message>()
    private var deviceInfo: DeviceInfo? = null
    private var compatibilityInfo: CompatibilityInfo? = null
    private var featureTests: FeatureTests? = null
    
    // Launcher pour les permissions
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        val postNotificationsGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions[Manifest.permission.POST_NOTIFICATIONS] ?: false
        } else {
            true // Pour les versions antérieures, considérer comme accordée
        }
        
        Log.d("MainActivity", "🔐 Résultat des permissions:")
        Log.d("MainActivity", "   - Toutes accordées: $allGranted")
        Log.d("MainActivity", "   - POST_NOTIFICATIONS: $postNotificationsGranted")
        
        if (allGranted) {
            Log.d("MainActivity", "✅ Toutes les permissions accordées")
            initializeApp()
        } else {
            Log.w("MainActivity", "⚠️ Certaines permissions refusées")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !postNotificationsGranted) {
                Toast.makeText(this, "⚠️ Permission POST_NOTIFICATIONS requise pour les notifications sur Android 13+", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "⚠️ Permissions requises pour les notifications", Toast.LENGTH_LONG).show()
            }
            // Initialiser quand même l'app en mode dégradé
            initializeApp()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        checkPermissions()
    }
    
    /**
     * Configurer l'interface utilisateur
     */
    private fun setupUI() {
        // Initialiser les services
        socketIOService = SocketIOService()
        webSocketService = WebSocketService()
        
        // Configurer les callbacks Socket.IO
        setupSocketIOCallbacks()
        
        // Configurer la RecyclerView pour les messages
        messageAdapter = MessageAdapter(messages)
        binding.recyclerViewMessages.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = messageAdapter
        }
        
        // Configurer les boutons
        binding.buttonToggleService.setOnClickListener {
            toggleService()
        }
        
        binding.buttonClearMessages.setOnClickListener {
            clearMessages()
        }
        
        // Boutons Socket.IO et WebSocket
        binding.buttonConnectSocketIO.setOnClickListener {
            connectSocketIO()
        }
        
        binding.buttonConnectWebSocket.setOnClickListener {
            connectWebSocket()
        }
        
        binding.buttonSendTestMessage.setOnClickListener {
            sendTestMessage()
        }
        
        binding.buttonDisconnect.setOnClickListener {
            disconnectAll()
        }
        
        // Masquer les informations au début
        binding.layoutCompatibility.visibility = View.GONE
        binding.layoutFeatureTests.visibility = View.GONE
        binding.layoutDeviceInfo.visibility = View.GONE
    }
    
    /**
     * Vérifier les permissions
     */
    private fun checkPermissions() {
        val permissions = mutableListOf<String>()
        
        // Permission POST_NOTIFICATIONS requise pour Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            Log.d("MainActivity", "📱 API 33+: Permission POST_NOTIFICATIONS requise")
        } else {
            Log.d("MainActivity", "📱 API < 33: Permission POST_NOTIFICATIONS non requise")
        }
        
        // Autres permissions nécessaires
        permissions.addAll(arrayOf(
            Manifest.permission.VIBRATE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE
        ))
        
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            Log.d("MainActivity", "🔐 Permissions à demander: ${permissionsToRequest.joinToString(", ")}")
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            Log.d("MainActivity", "✅ Toutes les permissions sont accordées")
            initializeApp()
        }
    }
    
    /**
     * Initialiser l'application
     */
    private fun initializeApp() {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                
                // Initialiser les services
                initializeServices()
                
                // Charger les messages stockés
                loadStoredMessages()
                
                // Vérifier la compatibilité
                checkCompatibility()
                
                binding.progressBar.visibility = View.GONE
                
            } catch (error: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@MainActivity, "❌ Erreur lors de l'initialisation: ${error.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    /**
     * Initialiser les services
     */
    private fun initializeServices() {
        Log.d("MainActivity", "🔧 Initialisation des services...")
        
        // Initialiser le service de notifications
        notificationService = NotificationService(this)
        Log.d("MainActivity", "✅ NotificationService initialisé")
        
        // Initialiser le service Socket.IO
        socketIOService = SocketIOService()
        Log.d("MainActivity", "✅ SocketIOService initialisé")
        
        // Démarrer le service Socket.IO
        val socketServiceIntent = Intent(this, SocketIOService::class.java)
        Log.d("MainActivity", "🚀 Démarrage du SocketIOService...")
        startService(socketServiceIntent)
        
        // Obtenir la référence au service Socket.IO lié
        Log.d("MainActivity", "🔗 Liaison au SocketIOService...")
        bindService(socketServiceIntent, socketServiceConnection, BIND_AUTO_CREATE)
        
        // Démarrer aussi le service en arrière-plan pour compatibilité
        val backgroundServiceIntent = Intent(this, BackgroundService::class.java)
        Log.d("MainActivity", "🚀 Démarrage du BackgroundService...")
        startService(backgroundServiceIntent)
        
        // Obtenir la référence au service en arrière-plan lié
        Log.d("MainActivity", "🔗 Liaison au BackgroundService...")
        bindService(backgroundServiceIntent, serviceConnection, BIND_AUTO_CREATE)
    }
    
    /**
     * Connexion au service Socket.IO
     */
    private val socketServiceConnection = object : android.content.ServiceConnection {
        override fun onServiceConnected(name: android.content.ComponentName?, service: IBinder?) {
            Log.d("MainActivity", "🔗 SocketServiceConnection.onServiceConnected appelé")
            try {
                val socketService = (service as SocketIOService.SocketIOBinder).getService()
                Log.d("MainActivity", "✅ SocketIOService connecté avec succès")
                
                // Configurer les listeners
                setupSocketIOListeners(socketService)
                
                // Lancer la connexion automatique
                lifecycleScope.launch {
                    launchSocketIOConnection(socketService)
                }
                
                // Mettre à jour l'interface utilisateur
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "✅ Socket.IO connecté", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "❌ Erreur lors de la connexion au SocketIOService", e)
            }
        }
        
        override fun onServiceDisconnected(name: android.content.ComponentName?) {
            Log.d("MainActivity", "❌ SocketIOService déconnecté")
        }
    }
    
    /**
     * Connexion au service Background
     */
    private val serviceConnection = object : android.content.ServiceConnection {
        override fun onServiceConnected(name: android.content.ComponentName?, service: IBinder?) {
            Log.d("MainActivity", "🔗 ServiceConnection.onServiceConnected appelé")
            try {
                backgroundService = (service as BackgroundService.BackgroundServiceBinder).getService()
                Log.d("MainActivity", "✅ BackgroundService connecté avec succès")
                
                // Mettre à jour l'interface utilisateur maintenant que le service est connecté
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "✅ Service en arrière-plan connecté", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "❌ Erreur lors de la connexion au service", e)
            }
        }
        
        override fun onServiceDisconnected(name: android.content.ComponentName?) {
            backgroundService = null
            Log.d("MainActivity", "❌ BackgroundService déconnecté")
        }
    }
    
    /**
     * Configurer les listeners Socket.IO
     */
    private fun setupSocketIOListeners(socketService: SocketIOService) {
        Log.d("MainActivity", "🔧 Configuration des listeners Socket.IO...")
        
        // Listener pour le statut de connexion
        socketService.setOnConnectionStatusListener { isConnected ->
            runOnUiThread {
                connectionStatus = if (isConnected) "Connecté" else "Déconnecté"
                Log.d("MainActivity", "📊 Statut Socket.IO: $connectionStatus")
                updateConnectionStatus()
            }
        }
        
        // Listener pour les messages
        socketService.setOnMessageListener { message ->
            runOnUiThread {
                Log.d("MainActivity", "📨 Message Socket.IO reçu: ${message.content}")
                messages.add(0, message) // Ajouter au début de la liste
                messageAdapter.notifyItemInserted(0)
                binding.recyclerViewMessages.scrollToPosition(0)
                
                // Afficher un toast pour confirmer la réception
                Toast.makeText(this@MainActivity, "📨 Message reçu: ${message.content}", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Listener pour les erreurs
        socketService.setOnErrorListener { error ->
            runOnUiThread {
                Log.e("MainActivity", "❌ Erreur Socket.IO: $error")
                Toast.makeText(this@MainActivity, "❌ Erreur Socket.IO: $error", Toast.LENGTH_LONG).show()
            }
        }
        
        Log.d("MainActivity", "✅ Listeners Socket.IO configurés")
    }
    
    /**
     * Lancer la connexion Socket.IO automatique
     */
    private suspend fun launchSocketIOConnection(socketService: SocketIOService) {
        try {
            Log.d("MainActivity", "🚀 Lancement de la connexion Socket.IO automatique...")
            
            runOnUiThread {
                Toast.makeText(this@MainActivity, "🔄 Connexion Socket.IO en cours...", Toast.LENGTH_SHORT).show()
            }
            
            val connected = socketService.connect()
            
            runOnUiThread {
                if (connected) {
                    Log.d("MainActivity", "✅ Connexion Socket.IO réussie")
                    Toast.makeText(this@MainActivity, "✅ Connexion Socket.IO réussie", Toast.LENGTH_SHORT).show()
                    connectionStatus = "Connecté"
                } else {
                    Log.w("MainActivity", "⚠️ Échec de la connexion Socket.IO")
                    Toast.makeText(this@MainActivity, "⚠️ Échec de la connexion Socket.IO", Toast.LENGTH_LONG).show()
                    connectionStatus = "Déconnecté"
                }
                updateConnectionStatus()
            }
            
        } catch (error: Exception) {
            Log.e("MainActivity", "❌ Erreur lors de la connexion Socket.IO", error)
            runOnUiThread {
                Toast.makeText(this@MainActivity, "❌ Erreur Socket.IO: ${error.message}", Toast.LENGTH_LONG).show()
                connectionStatus = "Erreur"
                updateConnectionStatus()
            }
        }
    }
    
    /**
     * Mettre à jour l'affichage du statut de connexion
     */
    private fun updateConnectionStatus() {
        binding.textConnectionStatus.text = "Statut: $connectionStatus"
        
        // Changer la couleur selon le statut
        val color = when (connectionStatus) {
            "Connecté" -> android.graphics.Color.GREEN
            "Déconnecté" -> android.graphics.Color.RED
            "Erreur" -> android.graphics.Color.parseColor("#FFA500") // Orange
            "Arrêté" -> android.graphics.Color.parseColor("#FFA500") // Orange
            else -> android.graphics.Color.GRAY
        }
        binding.textConnectionStatus.setTextColor(color)
        
        // Mettre à jour le texte du bouton si disponible
        try {
            binding.buttonToggleService.text = if (isServiceRunning) "🛑 Arrêter le service" else "▶️ Démarrer le service"
        } catch (e: Exception) {
            // Le bouton n'existe peut-être pas dans le layout
        }
    }
    
    /**
     * Charger les messages stockés
     */
    private fun loadStoredMessages() {
        lifecycleScope.launch {
            try {
                val storedMessages = notificationService.getStoredMessages()
                messages.clear()
                messages.addAll(storedMessages)
                messageAdapter.notifyDataSetChanged()
                
                binding.textMessagesCount.text = "📨 Messages reçus (${messages.size})"
            } catch (error: Exception) {
                Toast.makeText(this@MainActivity, "Erreur lors du chargement des messages", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * Vérifier la compatibilité
     */
    private fun checkCompatibility() {
        try {
            // Obtenir les informations de compatibilité
            compatibilityInfo = AndroidCompatibility.getDeviceInfo(this)
            deviceInfo = backgroundService?.getDeviceInfo()
            
            // Effectuer les tests de fonctionnalités
            featureTests = AndroidCompatibility.testFeatures(this)
            
            // Afficher les informations
            displayCompatibilityInfo()
            displayFeatureTests()
            displayDeviceInfo()
            
        } catch (error: Exception) {
            Toast.makeText(this, "Erreur lors de la vérification de compatibilité", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Afficher les informations de compatibilité
     */
    private fun displayCompatibilityInfo() {
        compatibilityInfo?.let { info ->
            binding.layoutCompatibility.visibility = View.VISIBLE
            binding.textCompatibilityStatus.text = info.message
            binding.textCompatibilityStatus.setTextColor(
                if (info.compatible) getColor(android.R.color.holo_green_dark) 
                else getColor(android.R.color.holo_red_dark)
            )
            binding.textOsVersion.text = "Version Android: ${deviceInfo?.osVersion}"
            binding.textModelName.text = "Modèle: ${deviceInfo?.modelName}"
            binding.textBrand.text = "Marque: ${deviceInfo?.brand}"
            
            // Afficher les fonctionnalités disponibles
            if (info.features.isNotEmpty()) {
                binding.textFeaturesAvailable.text = "Fonctionnalités disponibles:\n" + 
                    info.features.joinToString("\n") { "• $it" }
            }
        }
    }
    
    /**
     * Afficher les tests de fonctionnalités
     */
    private fun displayFeatureTests() {
        featureTests?.let { tests ->
            binding.layoutFeatureTests.visibility = View.VISIBLE
            binding.textWebsocketTest.text = "WebSocket: ${if (tests.webSocketConnection) "✅" else "❌"}"
            binding.textNotificationsTest.text = "Notifications: ${if (tests.notifications) "✅" else "❌"}"
            binding.textBackgroundTaskTest.text = "Tâches arrière-plan: ${if (tests.backgroundTask) "✅" else "❌"}"
            binding.textPersistentNotificationTest.text = "Notification persistante: ${if (tests.persistentNotification) "✅" else "❌"}"
            binding.textLocalStorageTest.text = "Stockage local: ${if (tests.localStorage) "✅" else "❌"}"
        }
    }
    
    /**
     * Afficher les informations de l'appareil
     */
    private fun displayDeviceInfo() {
        deviceInfo?.let { info ->
            binding.layoutDeviceInfo.visibility = View.VISIBLE
            binding.textPlatform.text = "Plateforme: ${info.platform}"
            binding.textServiceActive.text = "Service actif: ${if (info.isServiceRunning) "Oui" else "Non"}"
            binding.textPollingActive.text = "Polling local: ${if (info.isPolling) "Oui" else "Non"}"
            binding.textDeviceId.text = "ID Appareil: ${info.deviceId}"
        }
    }
    
    /**
     * Basculer l'état du service
     */
    private fun toggleService() {
        lifecycleScope.launch {
            try {
                if (backgroundService == null) {
                    Toast.makeText(this@MainActivity, "⏳ Service en cours de connexion...", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                
                if (isServiceRunning) {
                    val success = backgroundService!!.stop()
                    if (success) {
                        isServiceRunning = false
                        connectionStatus = "Arrêté"
                        updateConnectionStatus()
                        Toast.makeText(this@MainActivity, "🛑 Service arrêté", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val success = backgroundService!!.start()
                    if (success) {
                        isServiceRunning = true
                        connectionStatus = "Connecté"
                        updateConnectionStatus()
                        Toast.makeText(this@MainActivity, "▶️ Service démarré", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (error: Exception) {
                Toast.makeText(this@MainActivity, "Erreur lors du changement d'état du service", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * Effacer les messages
     */
    private fun clearMessages() {
        lifecycleScope.launch {
            try {
                // Effacer les messages de la mémoire
                messages.clear()
                messageAdapter.notifyDataSetChanged()
                
                // Effacer les messages du stockage local
                val prefs = getSharedPreferences(AppConfig.Preferences.PREF_NAME, MODE_PRIVATE)
                prefs.edit().remove("messages").apply()
                
                binding.textMessagesCount.text = "📨 Messages reçus (0)"
                Toast.makeText(this@MainActivity, "🗑️ Messages effacés", Toast.LENGTH_SHORT).show()
            } catch (error: Exception) {
                Toast.makeText(this@MainActivity, "Erreur lors de l'effacement des messages", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * Reconnecter manuellement Socket.IO
     */
    private fun reconnectSocketIO() {
        lifecycleScope.launch {
            try {
                Log.d("MainActivity", "🔄 Reconnexion manuelle Socket.IO...")
                
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "🔄 Reconnexion Socket.IO...", Toast.LENGTH_SHORT).show()
                }
                
                // Si le service Socket.IO est disponible, reconnecter
                if (::socketIOService.isInitialized) {
                    val connected = socketIOService.connect()
                    
                    runOnUiThread {
                        if (connected) {
                            Log.d("MainActivity", "✅ Reconnexion Socket.IO réussie")
                            Toast.makeText(this@MainActivity, "✅ Reconnexion Socket.IO réussie", Toast.LENGTH_SHORT).show()
                            connectionStatus = "Connecté"
                        } else {
                            Log.w("MainActivity", "⚠️ Échec de la reconnexion Socket.IO")
                            Toast.makeText(this@MainActivity, "⚠️ Échec de la reconnexion Socket.IO", Toast.LENGTH_LONG).show()
                            connectionStatus = "Déconnecté"
                        }
                        updateConnectionStatus()
                    }
                } else {
                    Log.w("MainActivity", "⚠️ SocketIOService non initialisé")
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "⚠️ Service Socket.IO non disponible", Toast.LENGTH_LONG).show()
                    }
                }
                
            } catch (error: Exception) {
                Log.e("MainActivity", "❌ Erreur lors de la reconnexion Socket.IO", error)
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "❌ Erreur reconnexion: ${error.message}", Toast.LENGTH_LONG).show()
                    connectionStatus = "Erreur"
                    updateConnectionStatus()
                }
            }
        }
    }
    
    /**
     * Configurer les callbacks Socket.IO
     */
    private fun setupSocketIOCallbacks() {
        socketIOService.setOnConnectionStatusListener { isConnected ->
            runOnUiThread {
                if (isConnected) {
                    connectionStatus = "Socket.IO Connecté"
                    updateConnectionStatus()
                    Toast.makeText(this@MainActivity, "✅ Socket.IO connecté au port 5022", Toast.LENGTH_SHORT).show()
                } else {
                    connectionStatus = "Socket.IO Déconnecté"
                    updateConnectionStatus()
                }
            }
        }
        
        socketIOService.setOnMessageListener { message ->
            runOnUiThread {
                messages.add(0, message)
                messageAdapter.notifyItemInserted(0)
                binding.textMessagesCount.text = "📨 Messages reçus (${messages.size})"
                Toast.makeText(this@MainActivity, "📨 Message Socket.IO reçu: ${message.content}", Toast.LENGTH_SHORT).show()
            }
        }
        
        socketIOService.setOnErrorListener { error ->
            runOnUiThread {
                Toast.makeText(this@MainActivity, "❌ Erreur Socket.IO: $error", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    /**
     * Se connecter avec Socket.IO
     */
    private fun connectSocketIO() {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                connectionStatus = "Connexion Socket.IO..."
                updateConnectionStatus()
                
                val success = socketIOService.connect()
                
                if (!success) {
                    connectionStatus = "Échec Socket.IO"
                    updateConnectionStatus()
                    Toast.makeText(this@MainActivity, "❌ Impossible de se connecter avec Socket.IO", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                connectionStatus = "Erreur Socket.IO"
                updateConnectionStatus()
                Toast.makeText(this@MainActivity, "❌ Erreur Socket.IO: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }
    
    /**
     * Se connecter avec WebSocket
     */
    private fun connectWebSocket() {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                connectionStatus = "Connexion WebSocket..."
                updateConnectionStatus()
                
                val success = webSocketService.connect()
                
                if (success) {
                    connectionStatus = "WebSocket Connecté"
                    updateConnectionStatus()
                    Toast.makeText(this@MainActivity, "✅ WebSocket connecté au port 5023", Toast.LENGTH_SHORT).show()
                } else {
                    connectionStatus = "Échec WebSocket"
                    updateConnectionStatus()
                    Toast.makeText(this@MainActivity, "❌ Impossible de se connecter avec WebSocket", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                connectionStatus = "Erreur WebSocket"
                updateConnectionStatus()
                Toast.makeText(this@MainActivity, "❌ Erreur WebSocket: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }
    
    /**
     * Envoyer un message de test
     */
    private fun sendTestMessage() {
        lifecycleScope.launch {
            try {
                val testMessage = "Test message from Android - ${System.currentTimeMillis()}"
                
                // Essayer Socket.IO d'abord
                if (socketIOService.getStatus().isConnected) {
                    socketIOService.sendMessage(testMessage)
                    Toast.makeText(this@MainActivity, "📤 Message envoyé via Socket.IO", Toast.LENGTH_SHORT).show()
                } else if (webSocketService.getStatus().isConnected) {
                    webSocketService.sendMessage(testMessage)
                    Toast.makeText(this@MainActivity, "📤 Message envoyé via WebSocket", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "⚠️ Aucune connexion active", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "❌ Erreur envoi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * Déconnecter tous les services
     */
    private fun disconnectAll() {
        lifecycleScope.launch {
            try {
                socketIOService.disconnect()
                webSocketService.disconnect()
                connectionStatus = "Déconnecté"
                updateConnectionStatus()
                Toast.makeText(this@MainActivity, "🔌 Toutes les connexions fermées", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "❌ Erreur déconnexion: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Mettre à jour le statut de connexion quand l'app revient au premier plan
        updateConnectionStatus()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Arrêter le service si nécessaire
        if (isServiceRunning) {
            lifecycleScope.launch {
                backgroundService?.stop()
            }
        }
        
        // Déconnecter Socket.IO et WebSocket
        try {
            socketIOService.disconnect()
            webSocketService.disconnect()
        } catch (e: Exception) {
            Log.e("MainActivity", "Erreur lors de la déconnexion", e)
        }
    }
}
