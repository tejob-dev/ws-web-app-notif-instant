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
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
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
            
            // Vérifier si les notifications sont activées
            val notificationsEnabled = areNotificationsEnabled()
            if (!notificationsEnabled) {
                Log.w(TAG, "⚠️ Les notifications ne sont pas activées - initialisation en mode dégradé")
                // Ne pas retourner false, continuer l'initialisation
            } else {
                Log.d(TAG, "✅ Permissions de notification accordées")
            }
            
            // Créer une notification persistante (même si les permissions ne sont pas accordées)
            createPersistentNotification()
            
            Log.d(TAG, "✅ Service de notifications initialisé avec succès (notifications: $notificationsEnabled)")
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
            // Vérifier les permissions avant de créer la notification
            if (!areNotificationsEnabled()) {
                Log.w(TAG, "⚠️ Permissions de notification non accordées - notification persistante ignorée")
                return
            }
            
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
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
            Log.d(TAG, "📌 Notification persistante créée avec succès")
        } catch (error: Exception) {
            Log.e(TAG, "❌ Erreur lors de la création de la notification persistante", error)
        }
    }
    
    /**
     * Afficher une notification pop-up
     */
    fun showPopUpNotification(message: Message) {
        serviceScope.launch {
            try {
                Log.d(TAG, "🔔 Tentative d'affichage de notification pour: ${message.content}")
                
                // Vérifier si les notifications sont activées
                if (!areNotificationsEnabled()) {
                    Log.w(TAG, "⚠️ Les notifications ne sont pas activées - impossible d'afficher la notification")
                    return@launch
                }
                
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("message_id", message.id)
                    putExtra("message_content", message.content)
                }
                
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    message.id.hashCode(),
                    intent,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    } else {
                        PendingIntent.FLAG_UPDATE_CURRENT
                    }
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
                
                Log.d(TAG, "📱 Notification pop-up affichée avec succès (ID: $notificationId): ${message.content}")
            } catch (error: Exception) {
                Log.e(TAG, "❌ Erreur lors de l'affichage de la notification pop-up", error)
            }
        }
    }
    
    /**
     * Gérer les messages WebSocket
     */
    fun handleWebSocketMessage(message: Message) {
        serviceScope.launch {
            try {
                Log.d(TAG, "📨 Traitement du message WebSocket: ${message.content}")
                
                // Afficher une notification pop-up
                showPopUpNotification(message)
                
                // Sauvegarder le message localement
                saveMessageLocally(message)
                
                Log.d(TAG, "✅ Message WebSocket traité avec succès")
            } catch (error: Exception) {
                Log.e(TAG, "❌ Erreur lors du traitement du message WebSocket", error)
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
     * Vérifier si les notifications sont activées ET si la permission POST_NOTIFICATIONS est accordée (pour API 33+)
     */
    fun areNotificationsEnabled(): Boolean {
        return try {
            // 1. Vérification générale de désactivation par l'utilisateur (pour toutes les versions)
            val enabled = notificationManager.areNotificationsEnabled()
            Log.d(TAG, "🔍 Vérification générale des notifications: $enabled")
            
            // 2. Vérification spécifique de la permission POST_NOTIFICATIONS (API 33+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // TIRAMISU est API 33
                val permissionGranted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                
                Log.d(TAG, "🔍 Vérification permission POST_NOTIFICATIONS (API 33+): $permissionGranted")
                
                if (!permissionGranted) {
                    Log.w(TAG, "⚠️ Permission POST_NOTIFICATIONS requise et non accordée (API 33+)")
                    Log.w(TAG, "💡 Solution: Demander la permission POST_NOTIFICATIONS dans l'activité")
                    return false
                }
            } else {
                Log.d(TAG, "📱 API < 33: Permission POST_NOTIFICATIONS non requise")
            }
            
            if (!enabled) {
                Log.w(TAG, "⚠️ Les notifications ne sont pas activées pour cette application")
                Log.w(TAG, "💡 Solution: Aller dans Paramètres > Applications > NotificationApp > Notifications")
            }
            
            // Retourne le statut combiné (général + permission API 33)
            val finalStatus = enabled && (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || 
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)
            
            Log.d(TAG, "🔍 Statut final des notifications: $finalStatus")
            finalStatus
            
        } catch (error: Exception) {
            Log.e(TAG, "❌ Erreur lors de la vérification des permissions de notification", error)
            false
        }
    }
    
    /**
     * Vérifier spécifiquement la permission POST_NOTIFICATIONS (API 33+)
     */
    fun isPostNotificationsPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            
            Log.d(TAG, "🔍 Permission POST_NOTIFICATIONS (API 33+): $granted")
            granted
        } else {
            Log.d(TAG, "📱 API < 33: Permission POST_NOTIFICATIONS non requise")
            true // Pour les versions antérieures, considérer comme accordée
        }
    }
    
    /**
     * Obtenir le statut du service
     */
    fun getServiceStatus(): Map<String, Any> {
        return mapOf(
            "notificationsEnabled" to areNotificationsEnabled(),
            "postNotificationsPermissionGranted" to isPostNotificationsPermissionGranted(),
            "apiLevel" to Build.VERSION.SDK_INT,
            "channelCreated" to true,
            "persistentNotificationActive" to true
        )
    }
}
