package com.notificationapp.kotlin.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.notificationapp.kotlin.MainActivity
import com.notificationapp.kotlin.R
import com.notificationapp.kotlin.config.AppConfig
import com.notificationapp.kotlin.model.Message
import com.notificationapp.kotlin.model.MobileToken
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

/**
 * Service de notifications Android
 * Reproduit les fonctionnalités du NotificationService.js Expo
 */
class NotificationService(private val context: Context) {
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val notificationManager = NotificationManagerCompat.from(context)
    private val notificationIdCounter = AtomicInteger(AppConfig.Notifications.MESSAGE_NOTIFICATION_ID_BASE)
    private val gson = Gson()
    
    companion object {
        private const val TAG = "NotificationService"
    }
    
    init {
        setupNotificationChannel()
    }
    
    /**
     * Configurer le canal de notification Android
     */
    private fun setupNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                AppConfig.Notifications.CHANNEL_ID,
                AppConfig.Notifications.CHANNEL_NAME,
                android.app.NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = AppConfig.Notifications.CHANNEL_DESCRIPTION
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 250, 250)
                enableLights(true)
                lightColor = android.graphics.Color.parseColor("#FF231F7C")
                setShowBadge(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "📱 Canal de notification créé")
        }
    }
    
    /**
     * Initialiser le service
     */
    suspend fun initialize(): Boolean {
        return try {
            Log.d(TAG, "🚀 Initialisation du service de notifications...")
            
            // Créer une notification persistante
            createPersistentNotification()
            
            Log.d(TAG, "✅ Service de notifications initialisé avec succès")
            true
        } catch (error: Exception) {
            Log.e(TAG, "❌ Erreur lors de l'initialisation du service", error)
            false
        }
    }
    
    /**
     * Créer une notification persistante dans la barre d'état
     */
    fun createPersistentNotification() {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            val notification = NotificationCompat.Builder(context, AppConfig.Notifications.CHANNEL_ID)
                .setContentTitle("🔔 Service de notifications actif")
                .setContentText("L'application écoute les notifications en temps réel")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setAutoCancel(false)
                .build()
            
            notificationManager.notify(AppConfig.Notifications.PERSISTENT_NOTIFICATION_ID, notification)
            Log.d(TAG, "📌 Notification persistante créée")
        } catch (error: Exception) {
            Log.e(TAG, "Erreur lors de la création de la notification persistante", error)
        }
    }
    
    /**
     * Afficher une notification pop-up
     */
    fun showPopUpNotification(message: Message) {
        serviceScope.launch {
            try {
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("message_id", message.id)
                    putExtra("message_content", message.content)
                }
                
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    message.id.hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                
                val notification = NotificationCompat.Builder(context, AppConfig.Notifications.CHANNEL_ID)
                    .setContentTitle("🔔 Nouvelle notification")
                    .setContentText(message.content)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setAutoCancel(true)
                    .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
                    .setVibrate(longArrayOf(0, 250, 250, 250))
                    .build()
                
                val notificationId = notificationIdCounter.getAndIncrement()
                notificationManager.notify(notificationId, notification)
                
                Log.d(TAG, "📱 Notification pop-up affichée: ${message.content}")
            } catch (error: Exception) {
                Log.e(TAG, "Erreur lors de l'affichage de la notification pop-up", error)
            }
        }
    }
    
    /**
     * Gérer les messages WebSocket
     */
    fun handleWebSocketMessage(message: Message) {
        serviceScope.launch {
            try {
                // Afficher une notification pop-up
                showPopUpNotification(message)
                
                // Sauvegarder le message localement
                saveMessageLocally(message)
                
            } catch (error: Exception) {
                Log.e(TAG, "Erreur lors du traitement du message WebSocket", error)
            }
        }
    }
    
    /**
     * Sauvegarder le message localement
     */
    private suspend fun saveMessageLocally(message: Message) {
        try {
            val prefs = context.getSharedPreferences(AppConfig.Preferences.PREF_NAME, Context.MODE_PRIVATE)
            val messagesJson = prefs.getString("messages", "[]") ?: "[]"
            
            // Parse les messages existants
            val type = object : TypeToken<List<Message>>() {}.type
            val existingMessages = gson.fromJson<List<Message>>(messagesJson, type) ?: emptyList()
            
            // Vérifier si le message existe déjà pour éviter les doublons
            val existingMessage = existingMessages.find { 
                it.id == message.id && it.timestamp == message.timestamp 
            }
            
            if (existingMessage != null) {
                Log.d(TAG, "📝 Message déjà existant, ignoré: ${message.id}")
                return
            }
            
            // Ajouter le nouveau message au début de la liste
            val updatedMessages = listOf(message) + existingMessages
            
            // Garder seulement les 100 derniers messages
            val limitedMessages = updatedMessages.take(AppConfig.Database.MAX_MESSAGES_STORED)
            
            // Sauvegarder la liste mise à jour
            val updatedJson = gson.toJson(limitedMessages)
            prefs.edit().putString("messages", updatedJson).apply()
            
            Log.d(TAG, "💾 Message sauvegardé localement: ${message.content}")
        } catch (error: Exception) {
            Log.e(TAG, "Erreur lors de la sauvegarde du message", error)
        }
    }
    
    /**
     * Récupérer les messages stockés
     */
    suspend fun getStoredMessages(): List<Message> {
        return try {
            val prefs = context.getSharedPreferences(AppConfig.Preferences.PREF_NAME, Context.MODE_PRIVATE)
            val messagesJson = prefs.getString("messages", "[]") ?: "[]"
            
            // Parse JSON et retourner la liste des messages
            val type = object : TypeToken<List<Message>>() {}.type
            val messages = gson.fromJson<List<Message>>(messagesJson, type) ?: emptyList()
            
            Log.d(TAG, "📨 ${messages.size} messages chargés depuis le stockage local")
            messages
        } catch (error: Exception) {
            Log.e(TAG, "Erreur lors de la récupération des messages", error)
            emptyList()
        }
    }
    
    /**
     * Effacer toutes les notifications
     */
    fun clearAllNotifications() {
        try {
            notificationManager.cancelAll()
            Log.d(TAG, "🗑️ Toutes les notifications effacées")
        } catch (error: Exception) {
            Log.e(TAG, "Erreur lors de l'effacement des notifications", error)
        }
    }
    
    /**
     * Effacer une notification spécifique
     */
    fun clearNotification(notificationId: Int) {
        try {
            notificationManager.cancel(notificationId)
            Log.d(TAG, "🗑️ Notification $notificationId effacée")
        } catch (error: Exception) {
            Log.e(TAG, "Erreur lors de l'effacement de la notification $notificationId", error)
        }
    }
    
    /**
     * Vérifier si les notifications sont activées
     */
    fun areNotificationsEnabled(): Boolean {
        return try {
            notificationManager.areNotificationsEnabled()
        } catch (error: Exception) {
            Log.e(TAG, "Erreur lors de la vérification des permissions de notification", error)
            false
        }
    }
    
    /**
     * Obtenir le statut du service
     */
    fun getServiceStatus(): Map<String, Any> {
        return mapOf(
            "notificationsEnabled" to areNotificationsEnabled(),
            "channelCreated" to true,
            "persistentNotificationActive" to true
        )
    }
}
