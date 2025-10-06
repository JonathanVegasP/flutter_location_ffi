package dev.jonathanvegasp.flutter_location_ffi.settings

import android.location.Location
import androidx.core.location.LocationRequestCompat
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.Priority
import dev.jonathanvegasp.result_channel.toLong

internal class AndroidLocationSettings private constructor(
   private val priority: Int,
    val intervalMs: Long,
    private val accuracyFilter: Float,
    val granularity: Int,
    val waitForAccurateLocation: Boolean,
    val durationMs: Long,
    val minUpdateDistanceMeters: Float,
    val minUpdateIntervalMs: Long,
    val maxUpdateDelayMs: Long,
    val maxUpdateAgeMillis: Long,
    val maxUpdates: Int,
    val showLocationServiceDialogWhenRequested: Boolean,
    val androidLocationNotificationSettings: AndroidLocationNotificationSettings
) {

    fun validate(location: Location): Float? {
        if (!location.hasAccuracy()) return 0.0F
        val accuracy = location.accuracy
        return when {
            accuracy > accuracyFilter -> null
            else -> accuracy
        }
    }

    fun getPriority() = when (priority) {
        0 -> Priority.PRIORITY_PASSIVE
        1 -> Priority.PRIORITY_PASSIVE
        2 -> Priority.PRIORITY_LOW_POWER
        3 -> Priority.PRIORITY_BALANCED_POWER_ACCURACY
        4 -> Priority.PRIORITY_HIGH_ACCURACY
        5 -> Priority.PRIORITY_HIGH_ACCURACY
        6 -> Priority.PRIORITY_HIGH_ACCURACY
        else -> throw IllegalArgumentException("Priority must be between 0 and 6: $priority")
    }

    fun getQuality() = when (priority) {
        0 -> LocationRequestCompat.QUALITY_LOW_POWER
        1 -> LocationRequestCompat.QUALITY_LOW_POWER
        2 -> LocationRequestCompat.QUALITY_LOW_POWER
        3 -> LocationRequestCompat.QUALITY_BALANCED_POWER_ACCURACY
        4 -> LocationRequestCompat.QUALITY_HIGH_ACCURACY
        5 -> LocationRequestCompat.QUALITY_HIGH_ACCURACY
        6 -> LocationRequestCompat.QUALITY_HIGH_ACCURACY
        else -> throw IllegalArgumentException("Priority must be between 0 and 6: $priority")
    }

    fun isPassive() = priority < 2

    companion object {
        @JvmStatic
        fun default(): AndroidLocationSettings {
            return AndroidLocationSettings(
                priority = 3,
                intervalMs = 10000L,
                accuracyFilter = 200.0F,
                granularity = Granularity.GRANULARITY_PERMISSION_LEVEL,
                waitForAccurateLocation = false,
                durationMs = Long.MAX_VALUE,
                minUpdateDistanceMeters = 0.0F,
                minUpdateIntervalMs = 10000L,
                maxUpdateDelayMs = 0L,
                maxUpdateAgeMillis = 0L,
                maxUpdates = Int.MAX_VALUE,
                showLocationServiceDialogWhenRequested = false,
                AndroidLocationNotificationSettings(
                    priority = 0,
                    showBadge = false,
                    vibrationEnabled = false,
                    lightsEnabled = false,
                    silent = true,
                    title = "Background service is running",
                    message = "Background Service is running",
                    info = null
                )
            )
        }

        @JvmStatic
        fun create(
            data: Array<Any?>
        ): AndroidLocationSettings {
            var durationMs = data[5].toLong()

            durationMs = when {
                durationMs > 0L -> durationMs
                else -> Long.MAX_VALUE
            }

            var maxUpdates = data[10] as Int

            maxUpdates = when {
                maxUpdates > 0 -> maxUpdates
                else -> Int.MAX_VALUE
            }

            return AndroidLocationSettings(
                priority = data[0] as Int,
                intervalMs = data[1].toLong(),
                accuracyFilter = (data[2] as Double).toFloat(),
                granularity = data[3] as Int,
                waitForAccurateLocation = data[4] as Boolean,
                durationMs = durationMs,
                minUpdateDistanceMeters = (data[6] as Double).toFloat(),
                minUpdateIntervalMs = data[7].toLong(),
                maxUpdateDelayMs = data[8].toLong(),
                maxUpdateAgeMillis = data[9].toLong(),
                maxUpdates = maxUpdates,
                showLocationServiceDialogWhenRequested = data[11] as Boolean,
                androidLocationNotificationSettings = AndroidLocationNotificationSettings(
                    priority = data[12] as Int,
                    showBadge = data[13] as Boolean,
                    vibrationEnabled = data[14] as Boolean,
                    lightsEnabled = data[15] as Boolean,
                    silent = data[16] as Boolean,
                    title = data[17] as String,
                    message = data[18] as String,
                    info = data[19] as String?
                )
            )
        }
    }
}