package dev.jonathanvegasp.flutter_location_ffi.location

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dev.jonathanvegasp.flutter_location_ffi.permission.PermissionStatus
import dev.jonathanvegasp.flutter_location_ffi.permission.location.BackgroundPermissionManager
import dev.jonathanvegasp.flutter_location_ffi.permission.notification.NotificationPermissionManager
import dev.jonathanvegasp.flutter_location_ffi.settings.AndroidLocationSettings
import dev.jonathanvegasp.result_channel.ResultChannel

internal class LocationService : Service() {
    companion object {
        private const val NOTIFICATION_ID = 1011

        private const val CHANNEL_ID = "flutter_location_channel"

        private const val POWER_LOCK = "LocationService::WakeLock"

        private const val WIFI_LOCK = "LocationService::WifiLock"
    }

    private val binder = LocationServiceBinder()

    private var settings: AndroidLocationSettings? = null

    private var notificationPermissionManager: NotificationPermissionManager? = null

    private var backgroundPermissionManager: BackgroundPermissionManager? = null

    private var strategy: LocationStrategy? = null

    private var isForeground = false

    private var powerLock: PowerManager.WakeLock? = null

    private var wifiLock: WifiManager.WifiLock? = null

    internal inner class LocationServiceBinder : Binder() {
        fun getService(): LocationService = this@LocationService
    }

    @Suppress("DEPRECATION")
    override fun onCreate() {
        super.onCreate()

        val powerManager =
            getSystemService(Context.POWER_SERVICE) as PowerManager

        powerLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, POWER_LOCK).apply {
            setReferenceCounted(false)
        }

        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        val wifiMode = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                WifiManager.WIFI_MODE_FULL_LOW_LATENCY
            }

            else -> {
                WifiManager.WIFI_MODE_FULL_HIGH_PERF
            }
        }

        wifiLock = wifiManager.createWifiLock(wifiMode, WIFI_LOCK).apply {
            setReferenceCounted(false)
        }
    }


    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    @SuppressLint("MissingPermission")
    private fun updateNotification() {
        if (notificationPermissionManager!!.checkPermission() != PermissionStatus.GRANTED) return

        val notificationSettings = settings!!.androidLocationNotificationSettings

        val notificationManager = NotificationManagerCompat.from(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                NotificationChannelCompat.Builder(
                    CHANNEL_ID,
                    notificationSettings.getNotificationChannelPriority()
                )
                    .setName(applicationInfo.name)
                    .setShowBadge(notificationSettings.showBadge)
                    .setVibrationEnabled(notificationSettings.vibrationEnabled)
                    .setLightsEnabled(notificationSettings.lightsEnabled)
                    .build()
            )
        }

        if (isForeground) {
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setPriority(notificationSettings.getNotificationPriority())
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                .setAutoCancel(false)
                .setOngoing(true)
                .setSilent(notificationSettings.silent)
                .setSmallIcon(applicationInfo.icon)
                .setContentTitle(notificationSettings.title)
                .setContentText(notificationSettings.message)
                .setContentInfo(notificationSettings.info)
                .build()

            notificationManager.notify(NOTIFICATION_ID, notification)
        }
    }

    @SuppressLint("WakelockTimeout")
    private fun startBackground() {
        if (!isForeground && backgroundPermissionManager!!.checkPermission() == PermissionStatus.GRANTED) {
            val settings = settings!!.androidLocationNotificationSettings
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setPriority(settings.getNotificationPriority())
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                .setAutoCancel(false)
                .setOngoing(true)
                .setSilent(settings.silent)
                .setSmallIcon(applicationInfo.icon)
                .setContentTitle(settings.title)
                .setContentText(settings.message)
                .setContentInfo(settings.info)
                .build()

            startForeground(NOTIFICATION_ID, notification)

            val powerLock = powerLock!!

            if (!powerLock.isHeld) {
                powerLock.acquire()
            }

            val wifiLock = wifiLock!!

            if (!wifiLock.isHeld) {
                wifiLock.acquire()
            }

            isForeground = true
        }
    }

    @Suppress("DEPRECATION")
    private fun stopBackground() {
        if (isForeground) {
            isForeground = false

            val powerLock = powerLock!!

            if (powerLock.isHeld) {
                powerLock.release()
            }

            val wifiLock = wifiLock!!

            if (wifiLock.isHeld) {
                wifiLock.release()
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
                return
            }

            stopForeground(true)
        }
    }

    fun init(
        settings: AndroidLocationSettings,
        notificationPermissionManager: NotificationPermissionManager,
        backgroundPermissionManager: BackgroundPermissionManager,
        strategy: LocationStrategy
    ) {
        this.settings = settings
        this.notificationPermissionManager = notificationPermissionManager
        this.backgroundPermissionManager = backgroundPermissionManager
        this.strategy = strategy

        updateNotification()

        if (strategy.isListening) {
            startBackground()
        }
    }

    fun setSettings(settings: AndroidLocationSettings) {
        this.settings = settings

        updateNotification()

        strategy!!.setSettings(settings)
    }

    fun isServiceEnabled(channel: ResultChannel) {
        strategy!!.isServiceEnabled(channel)
    }

    fun getCurrent(result: ResultChannel) {
        strategy!!.getCurrent(result)
    }

    fun startUpdates(result: ResultChannel) {
        strategy!!.startUpdates(result)

        startBackground()
    }

    fun stopUpdates() {
        stopBackground()

        strategy!!.stopUpdates()
    }

    override fun onDestroy() {
        strategy!!.onDestroy()
        strategy = null
        stopBackground()
        powerLock = null
        wifiLock = null
        super.onDestroy()
    }
}