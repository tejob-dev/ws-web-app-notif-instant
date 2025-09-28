package com.notificationapp.kotlin.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import com.notificationapp.kotlin.model.CompatibilityInfo
import com.notificationapp.kotlin.model.FeatureTests

/**
 * Utilitaires pour la compatibilité Android
 * Reproduit les fonctionnalités d'AndroidCompatibility.js Expo
 */
object AndroidCompatibility {
    
    /**
     * Obtenir les informations de l'appareil
     */
    fun getDeviceInfo(context: Context): CompatibilityInfo {
        val osVersion = Build.VERSION.RELEASE
        val modelName = Build.MODEL
        val brand = Build.BRAND
        val sdkVersion = Build.VERSION.SDK_INT
        
        val compatible = sdkVersion >= Build.VERSION_CODES.M // Android 6.0+
        val message = if (compatible) {
            "Appareil compatible avec les notifications en temps réel"
        } else {
            "Appareil non compatible - Android 6.0+ requis"
        }
        
        val features = mutableListOf<String>()
        
        // Vérifier les fonctionnalités disponibles
        if (sdkVersion >= Build.VERSION_CODES.M) {
            features.add("Notifications push")
        }
        if (sdkVersion >= Build.VERSION_CODES.O) {
            features.add("Canaux de notification")
        }
        if (sdkVersion >= Build.VERSION_CODES.LOLLIPOP) {
            features.add("WebSocket natif")
        }
        if (sdkVersion >= Build.VERSION_CODES.M) {
            features.add("Service en arrière-plan")
        }
        if (sdkVersion >= Build.VERSION_CODES.JELLY_BEAN) {
            features.add("Stockage local")
        }
        
        return CompatibilityInfo(
            compatible = compatible,
            message = message,
            features = features
        )
    }
    
    /**
     * Tester les fonctionnalités
     */
    fun testFeatures(context: Context): FeatureTests {
        return FeatureTests(
            webSocketConnection = testWebSocketConnection(),
            notifications = testNotifications(context),
            backgroundTask = testBackgroundTask(context),
            persistentNotification = testPersistentNotification(context),
            localStorage = testLocalStorage(context)
        )
    }
    
    /**
     * Tester la connexion WebSocket
     */
    private fun testWebSocketConnection(): Boolean {
        return try {
            // Test basique de disponibilité WebSocket
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Tester les notifications
     */
    private fun testNotifications(context: Context): Boolean {
        return try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.areNotificationsEnabled()
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Tester les tâches en arrière-plan
     */
    private fun testBackgroundTask(context: Context): Boolean {
        return try {
            // Vérifier si les services en arrière-plan sont autorisés
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Tester les notifications persistantes
     */
    private fun testPersistentNotification(context: Context): Boolean {
        return try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.areNotificationsEnabled() && 
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Tester le stockage local
     */
    private fun testLocalStorage(context: Context): Boolean {
        return try {
            // Test basique d'écriture/lecture
            val prefs = context.getSharedPreferences("test_storage", Context.MODE_PRIVATE)
            prefs.edit().putString("test", "value").apply()
            val result = prefs.getString("test", null) == "value"
            prefs.edit().remove("test").apply()
            result
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Vérifier les permissions
     */
    fun checkPermissions(context: Context): Map<String, Boolean> {
        return mapOf(
            "POST_NOTIFICATIONS" to (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED),
            "VIBRATE" to (context.checkSelfPermission(android.Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED),
            "WAKE_LOCK" to (context.checkSelfPermission(android.Manifest.permission.WAKE_LOCK) == PackageManager.PERMISSION_GRANTED),
            "FOREGROUND_SERVICE" to (context.checkSelfPermission(android.Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED),
            "INTERNET" to (context.checkSelfPermission(android.Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED),
            "ACCESS_NETWORK_STATE" to (context.checkSelfPermission(android.Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED)
        )
    }
    
    /**
     * Obtenir l'ID unique de l'appareil
     */
    fun getDeviceId(context: Context): String {
        return try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        } catch (e: Exception) {
            "${Build.MODEL}-${System.currentTimeMillis()}"
        }
    }
    
    /**
     * Vérifier si l'optimisation de la batterie est désactivée
     */
    fun isBatteryOptimizationDisabled(context: Context): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
                powerManager.isIgnoringBatteryOptimizations(context.packageName)
            } else {
                true
            }
        } catch (e: Exception) {
            false
        }
    }
}
