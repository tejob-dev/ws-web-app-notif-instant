package com.notificationapp.kotlin.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.notificationapp.kotlin.service.BackgroundService

/**
 * Receiver pour le démarrage automatique au boot
 */
class BootReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "BootReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_REPLACED -> {
                Log.d(TAG, "🚀 Démarrage automatique détecté")
                
                // Démarrer le service en arrière-plan
                val serviceIntent = Intent(context, BackgroundService::class.java)
                context.startService(serviceIntent)
                
                Log.d(TAG, "✅ Service en arrière-plan démarré automatiquement")
            }
        }
    }
}
