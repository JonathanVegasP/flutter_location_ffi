package dev.jonathanvegasp.flutter_location_ffi.settings

import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

internal class AndroidLocationNotificationSettings(
    private val priority: Int,
    val showBadge: Boolean,
    val vibrationEnabled: Boolean,
    val lightsEnabled: Boolean,
    val silent: Boolean,
    val title: String,
    val message: String,
    val info: String?
) {
    fun getNotificationPriority() = when (priority) {
        0 -> NotificationCompat.PRIORITY_LOW
        1 -> NotificationCompat.PRIORITY_DEFAULT
        2 -> NotificationCompat.PRIORITY_HIGH
        3 -> NotificationCompat.PRIORITY_MAX
        else -> throw IllegalArgumentException("Priority must be between 0 and 3: $priority")
    }

    fun getNotificationChannelPriority() = when (priority) {
        0 -> NotificationManagerCompat.IMPORTANCE_LOW
        1 -> NotificationManagerCompat.IMPORTANCE_DEFAULT
        2 -> NotificationManagerCompat.IMPORTANCE_HIGH
        3 -> NotificationManagerCompat.IMPORTANCE_MAX
        else -> throw IllegalArgumentException("Priority must be between 0 and 3: $priority")
    }
}