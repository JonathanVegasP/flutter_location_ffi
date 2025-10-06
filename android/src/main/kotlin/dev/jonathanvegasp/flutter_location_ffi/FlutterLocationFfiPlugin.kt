package dev.jonathanvegasp.flutter_location_ffi

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.annotation.Keep
import dev.jonathanvegasp.flutter_location_ffi.location.LocationService
import dev.jonathanvegasp.flutter_location_ffi.location.LocationStrategy
import dev.jonathanvegasp.flutter_location_ffi.location.LocationStrategyFactory
import dev.jonathanvegasp.flutter_location_ffi.location.googleplay.FusedLocationStrategy
import dev.jonathanvegasp.flutter_location_ffi.permission.location.BackgroundPermissionManager
import dev.jonathanvegasp.flutter_location_ffi.permission.location.LocationPermissionManager
import dev.jonathanvegasp.flutter_location_ffi.permission.notification.NotificationPermissionManager
import dev.jonathanvegasp.flutter_location_ffi.settings.AndroidLocationSettings
import dev.jonathanvegasp.result_channel.BinarySerializer
import dev.jonathanvegasp.result_channel.ResultChannel
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.PluginRegistry
import java.nio.ByteBuffer

/** FlutterLocationFfiPlugin */
class FlutterLocationFfiPlugin : FlutterPlugin, ActivityAware, ServiceConnection {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private var locationPermissionManager: LocationPermissionManager? = null

        @SuppressLint("StaticFieldLeak")
        private var backgroundPermissionManager: BackgroundPermissionManager? = null

        @SuppressLint("StaticFieldLeak")
        private var notificationPermissionManager: NotificationPermissionManager? = null
        private var locationService: LocationService? = null
        private var locationStrategy: LocationStrategy? = null
        private var settings: AndroidLocationSettings = AndroidLocationSettings.default()

        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        @Keep
        fun setSettings(byteArray: ByteBuffer) {
            val data = BinarySerializer.deserialize(byteArray)

            val settings = AndroidLocationSettings.create(
                data as Array<Any?>
            )

            this.settings = settings

            locationService?.also {
                it.setSettings(settings)

                return
            }

            locationStrategy!!.setSettings(settings)
        }

        @JvmStatic
        @Keep
        fun checkAndRequestPermission(channel: ResultChannel) =
            locationPermissionManager!!.checkAndRequestPermission(channel)

        @JvmStatic
        @Keep
        fun checkPermission() =
            BinarySerializer.serialize(locationPermissionManager!!.checkPermission())

        @JvmStatic
        @Keep
        fun checkAndRequestBackgroundPermission(channel: ResultChannel) =
            backgroundPermissionManager!!.checkAndRequestPermission(channel)

        @JvmStatic
        @Keep
        fun checkBackgroundPermission() =
            BinarySerializer.serialize(backgroundPermissionManager!!.checkPermission())

        @JvmStatic
        @Keep
        fun checkAndRequestNotificationPermission(channel: ResultChannel) =
            notificationPermissionManager!!.checkAndRequestPermission(channel)

        @JvmStatic
        @Keep
        fun checkNotificationPermission() =
            BinarySerializer.serialize(notificationPermissionManager!!.checkPermission())

        @JvmStatic
        @Keep
        fun isServiceEnabled(channel: ResultChannel) {
            locationService?.also {
                it.isServiceEnabled(channel)

                return
            }

            locationStrategy!!.isServiceEnabled(channel)
        }

        @JvmStatic
        @Keep
        fun getCurrent(channel: ResultChannel) {
            locationService?.also {
                it.getCurrent(channel)

                return
            }

            locationStrategy!!.getCurrent(channel)
        }

        @JvmStatic
        @Keep
        fun startUpdates(channel: ResultChannel) {
            locationService?.also {
                it.startUpdates(channel)

                return
            }

            locationStrategy!!.startUpdates(channel)
        }

        @JvmStatic
        @Keep
        fun stopUpdates() {
            locationService?.also {
                it.stopUpdates()
                return
            }

            locationStrategy!!.stopUpdates()
        }

        @JvmStatic
        @Keep
        fun openAppSettings() {
            locationPermissionManager!!.openAppSettings()
        }

        @JvmStatic
        @Keep
        fun openLocationSettings() {
            locationPermissionManager!!.openPermissionSettings()
        }

        @JvmStatic
        @Keep
        fun openNotificationSettings() {
            notificationPermissionManager!!.openPermissionSettings()
        }
    }

    private var activityPluginBinding: ActivityPluginBinding? = null

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        val context = flutterPluginBinding.applicationContext

        locationStrategy = LocationStrategyFactory.create(context)

        context.bindService(
            Intent(context, LocationService::class.java),
            this,
            Context.BIND_AUTO_CREATE
        )

    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        binding.applicationContext.unbindService(this)
        locationStrategy = null
        locationService = null
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        val activity = binding.activity
        val manager = LocationPermissionManager(activity)
        val backgroundManager = BackgroundPermissionManager(activity)
        val notificationManager = NotificationPermissionManager(activity)
        val strategy = locationStrategy

        binding.addRequestPermissionsResultListener(manager)
        binding.addRequestPermissionsResultListener(backgroundManager)
        binding.addRequestPermissionsResultListener(notificationManager)

        if (strategy is FusedLocationStrategy) {
            strategy.setActivity(activity)
        }

        if (strategy is PluginRegistry.ActivityResultListener) {
            binding.addActivityResultListener(strategy)
        }

        activityPluginBinding = binding
        locationPermissionManager = manager
        backgroundPermissionManager = backgroundManager
        notificationPermissionManager = notificationManager
    }

    override fun onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity()
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        onAttachedToActivity(binding)
    }

    override fun onDetachedFromActivity() {
        val binding = activityPluginBinding!!
        val manager = locationPermissionManager!!
        val backgroundManager = backgroundPermissionManager!!
        val notificationManager = notificationPermissionManager!!
        val strategy = locationStrategy

        binding.removeRequestPermissionsResultListener(manager)
        binding.removeRequestPermissionsResultListener(backgroundManager)
        binding.removeRequestPermissionsResultListener(notificationManager)

        if (strategy is PluginRegistry.ActivityResultListener) {
            binding.removeActivityResultListener(strategy)
        }

        if (strategy is FusedLocationStrategy) {
            strategy.setActivity(null)
        }

        manager.onDestroy()

        activityPluginBinding = null
        locationPermissionManager = null
        backgroundPermissionManager = null
        notificationPermissionManager = null
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val s = (service as LocationService.LocationServiceBinder).getService()
        s.init(
            settings,
            notificationPermissionManager!!,
            backgroundPermissionManager!!,
            locationStrategy!!
        )
        locationService = s
    }

    override fun onServiceDisconnected(name: ComponentName?) {
    }
}
