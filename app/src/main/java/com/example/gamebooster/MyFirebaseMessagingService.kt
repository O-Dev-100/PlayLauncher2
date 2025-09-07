package com.example.gamebooster

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.example.gamebooster.MainActivity
import com.example.gamebooster.R

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FirebaseMessaging"
        private const val CHANNEL_BOOST = "boost_channel"
        private const val CHANNEL_NEWS = "news_channel"
        private const val CHANNEL_UPDATES = "updates_channel"
        private const val CHANNEL_ACHIEVEMENTS = "achievements_channel"
        private const val CHANNEL_GENERAL = "general_channel"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Nuevo token FCM: $token")
        // Aquí podrías enviar el token a tu servidor
        sendTokenToServer(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Mensaje recibido de: ${remoteMessage.from}")

        // Procesar datos del mensaje
        val data = remoteMessage.data
        val notification = remoteMessage.notification

        when (data["type"] ?: "general") {
            "boost" -> handleBoostNotification(notification, data)
            "news" -> handleNewsNotification(notification, data)
            "update" -> handleUpdateNotification(notification, data)
            "achievement" -> handleAchievementNotification(notification, data)
            else -> handleGeneralNotification(notification, data)
        }
    }

    private fun handleBoostNotification(notification: RemoteMessage.Notification?, data: Map<String, String>) {
        val title = notification?.title ?: data["title"] ?: "Optimización Completada"
        val message = notification?.body ?: data["message"] ?: "Tu dispositivo ha sido optimizado exitosamente"
        val boostLevel = data["boost_level"] ?: "Normal"

        sendNotification(
            title = title,
            message = message,
            channelId = CHANNEL_BOOST,
            notificationId = 1001,
            icon = R.drawable.playlauncher, // Cambiado a playlauncher.png
            priority = NotificationCompat.PRIORITY_HIGH,
            extras = mapOf("boost_level" to boostLevel)
        )
    }

    private fun handleNewsNotification(notification: RemoteMessage.Notification?, data: Map<String, String>) {
        val title = notification?.title ?: data["title"] ?: "Nueva Noticia Gaming"
        val message = notification?.body ?: data["message"] ?: "Descubre las últimas noticias del mundo gaming"
        val newsUrl = data["news_url"]

        sendNotification(
            title = title,
            message = message,
            channelId = CHANNEL_NEWS,
            notificationId = 1002,
            icon = R.drawable.playlauncher, // Cambiado a playlauncher.png
            priority = NotificationCompat.PRIORITY_DEFAULT,
            extras = mapOf("news_url" to (newsUrl ?: ""))
        )
    }

    private fun handleUpdateNotification(notification: RemoteMessage.Notification?, data: Map<String, String>) {
        val title = notification?.title ?: data["title"] ?: "Nueva Actualización"
        val message = notification?.body ?: data["message"] ?: "GameBooster ha sido actualizado con nuevas funciones"
        val version = data["version"] ?: "1.0.0"

        sendNotification(
            title = title,
            message = message,
            channelId = CHANNEL_UPDATES,
            notificationId = 1003,
            icon = R.drawable.playlauncher, // Cambiado a playlauncher.png
            priority = NotificationCompat.PRIORITY_LOW,
            extras = mapOf("version" to version)
        )
    }

    private fun handleAchievementNotification(notification: RemoteMessage.Notification?, data: Map<String, String>) {
        val title = notification?.title ?: data["title"] ?: "¡Logro Desbloqueado!"
        val message = notification?.body ?: data["message"] ?: "Has desbloqueado un nuevo logro"
        val achievementName = data["achievement_name"] ?: "Logro"

        sendNotification(
            title = title,
            message = message,
            channelId = CHANNEL_ACHIEVEMENTS,
            notificationId = 1004,
            icon = R.drawable.playlauncher, // Cambiado a playlauncher.png
            priority = NotificationCompat.PRIORITY_HIGH,
            extras = mapOf("achievement_name" to achievementName)
        )
    }

    private fun handleGeneralNotification(notification: RemoteMessage.Notification?, data: Map<String, String>) {
        val title = notification?.title ?: data["title"] ?: "GameBooster"
        val message = notification?.body ?: data["message"] ?: "¡Vuelve a GameBooster y descubre novedades para gamers!"

        sendNotification(
            title = title,
            message = message,
            channelId = CHANNEL_GENERAL,
            notificationId = 1005,
            icon = R.drawable.playlauncher, // Cambiado a playlauncher.png
            priority = NotificationCompat.PRIORITY_DEFAULT
        )
    }

    private fun sendNotification(
        title: String,
        message: String,
        channelId: String,
        notificationId: Int,
        icon: Int,
        priority: Int,
        extras: Map<String, String> = emptyMap()
    ) {
        // Crear intent para abrir la app
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            extras.forEach { (key, value) ->
                putExtra(key, value)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this, notificationId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        // Crear notificación
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(priority)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))

        // Agregar acciones si es necesario
        when (channelId) {
            CHANNEL_BOOST -> {
                notificationBuilder.addAction(
                    R.drawable.ic_power,
                    "Ver Estadísticas",
                    createActionPendingIntent("VIEW_STATS", notificationId)
                )
            }
            CHANNEL_NEWS -> {
                notificationBuilder.addAction(
                    R.drawable.ic_trending,
                    "Leer Noticia",
                    createActionPendingIntent("READ_NEWS", notificationId)
                )
            }
            CHANNEL_UPDATES -> {
                notificationBuilder.addAction(
                    R.drawable.ic_star,
                    "Ver Cambios",
                    createActionPendingIntent("VIEW_CHANGES", notificationId)
                )
            }
        }

        // Crear canal de notificación si es necesario
        createNotificationChannel(channelId, getChannelName(channelId), getChannelDescription(channelId))

        // Mostrar notificación
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notificationBuilder.build())

        Log.d(TAG, "Notificación enviada: $title - $message")
    }

    private fun createNotificationChannel(channelId: String, name: String, description: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                name,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                this.description = description
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun getChannelName(channelId: String): String {
        return when (channelId) {
            CHANNEL_BOOST -> "Optimizaciones"
            CHANNEL_NEWS -> "Noticias Gaming"
            CHANNEL_UPDATES -> "Actualizaciones"
            CHANNEL_ACHIEVEMENTS -> "Logros"
            else -> "General"
        }
    }

    private fun getChannelDescription(channelId: String): String {
        return when (channelId) {
            CHANNEL_BOOST -> "Notificaciones sobre optimizaciones completadas"
            CHANNEL_NEWS -> "Últimas noticias del mundo gaming"
            CHANNEL_UPDATES -> "Nuevas versiones y mejoras"
            CHANNEL_ACHIEVEMENTS -> "Logros desbloqueados"
            else -> "Notificaciones generales"
        }
    }

    private fun createActionPendingIntent(action: String, notificationId: Int): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            // CORRECCIÓN: Se usa "this.action" para asignar la acción al Intent y no al parámetro.
            this.action = action
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            this, notificationId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0)
        )
    }

    private fun sendTokenToServer(token: String) {
        // Implementar envío del token a tu servidor
        Log.d(TAG, "Token enviado al servidor: $token")
        // Aquí podrías hacer una llamada HTTP a tu servidor
    }
}