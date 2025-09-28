package com.notificationapp.kotlin.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.notificationapp.kotlin.service.NotificationService

/**
 * Receiver pour les notifications
 */
class NotificationReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "NotificationReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "com.notificationapp.kotlin.NOTIFICATION_RECEIVED" -> {
                Log.d(TAG, "📨 Notification reçue")
                handleNotificationReceived(context, intent)
            }
            "com.notificationapp.kotlin.NOTIFICATION_CLICKED" -> {
                Log.d(TAG, "👆 Notification cliquée")
                handleNotificationClicked(context, intent)
            }
        }
    }
    
    private fun handleNotificationReceived(context: Context, intent: Intent) {
        try {
            val messageId = intent.getStringExtra("message_id")
            val messageContent = intent.getStringExtra("message_content")
            
            Log.d(TAG, "📨 Notification traitée: $messageContent")
            
            // Ici vous pouvez ajouter votre logique de traitement
        } catch (error: Exception) {
            Log.e(TAG, "❌ Erreur lors du traitement de la notification", error)
        }
    }
    
    private fun handleNotificationClicked(context: Context, intent: Intent) {
        try {
            val messageId = intent.getStringExtra("message_id")
            val messageContent = intent.getStringExtra("message_content")
            
            Log.d(TAG, "👆 Interaction avec notification: $messageContent")
            
            // Ouvrir l'activité principale
            val mainIntent = Intent(context, com.notificationapp.kotlin.MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("message_id", messageId)
                putExtra("message_content", messageContent)
            }
            context.startActivity(mainIntent)
            
        } catch (error: Exception) {
            Log.e(TAG, "❌ Erreur lors du traitement du clic de notification", error)
        }
    }
}
