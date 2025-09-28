package com.notificationapp.kotlin.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Modèle de données pour les messages
 * Reproduit la structure de l'app Expo
 */
@Parcelize
data class Message(
    val id: String,
    val content: String,
    val type: String,
    val timestamp: String
) : Parcelable

/**
 * Modèle de données pour le statut de connexion
 */
data class ConnectionStatus(
    val isConnected: Boolean,
    val deviceId: String?,
    val lastConnectionTime: Long,
    val reconnectAttempts: Int
)

/**
 * Modèle de données pour les informations de l'appareil
 */
data class DeviceInfo(
    val deviceId: String,
    val platform: String,
    val modelName: String,
    val brand: String,
    val osVersion: String,
    val isServiceRunning: Boolean,
    val isPolling: Boolean,
    val compatibility: CompatibilityInfo
)

/**
 * Modèle de données pour les informations de compatibilité
 */
data class CompatibilityInfo(
    val compatible: Boolean,
    val message: String,
    val features: List<String>
)

/**
 * Modèle de données pour les tests de fonctionnalités
 */
data class FeatureTests(
    val webSocketConnection: Boolean,
    val notifications: Boolean,
    val backgroundTask: Boolean,
    val persistentNotification: Boolean,
    val localStorage: Boolean
)

/**
 * Modèle de données pour les tokens mobiles
 */
data class MobileToken(
    val token: String,
    val platform: String,
    val deviceId: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Modèle de données pour les événements WebSocket
 */
data class WebSocketEvent(
    val type: String,
    val data: Any?,
    val timestamp: Long = System.currentTimeMillis()
)
