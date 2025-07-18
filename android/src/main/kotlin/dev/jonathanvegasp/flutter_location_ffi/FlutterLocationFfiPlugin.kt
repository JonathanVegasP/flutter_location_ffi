package dev.jonathanvegasp.flutter_location_ffi

import android.content.Context
import androidx.annotation.Keep
import dev.jonathanvegasp.result_channel.ResultChannel
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding

/** FlutterLocationFfiPlugin */
class FlutterLocationFfiPlugin : FlutterPlugin, ActivityAware {

    companion object {
        private const val NAME = "flutter_location_ffi"

        init {
            System.loadLibrary(NAME)
        }

        private var permissionManager: PermissionManager? = null
        private var locationStrategy: LocationStrategy? = null

        @JvmStatic
        @Keep
        fun checkAndRequestPermission(channel: ResultChannel) =
            permissionManager!!.checkAndRequestPermission(channel)

        @JvmStatic
        @Keep
        fun checkPermission() =
            ResultChannel.serializer.serialize(permissionManager!!.checkPermission().ordinal)

        @JvmStatic
        @Keep
        fun getCurrent(channel: ResultChannel) {
            locationStrategy!!.getCurrent(channel)
        }

        @JvmStatic
        @Keep
        fun startUpdates(channel: ResultChannel) {
            locationStrategy!!.startUpdates(channel)
        }

        @JvmStatic
        @Keep
        fun stopUpdates() {
            locationStrategy!!.stopUpdates()
        }

        @JvmStatic
        @Keep
        fun openAppSettings() {
            permissionManager!!.openAppSettings()
        }

        @JvmStatic
        @Keep
        fun openLocationSettings() {
            permissionManager!!.openPermissionSettings()
        }
    }

    private var activityPluginBinding: ActivityPluginBinding? = null
    private var context: Context? = null

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        context = flutterPluginBinding.applicationContext
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        context = null
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        val activity = binding.activity
        val manager = LocationPermissionManager(activity, context!!)
        binding.addRequestPermissionsResultListener(manager)

        activityPluginBinding = binding
        locationStrategy = LocationStrategyFactory.create(activity)
        permissionManager = manager
    }

    override fun onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity()
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        onAttachedToActivity(binding)
    }

    override fun onDetachedFromActivity() {
        activityPluginBinding!!.removeRequestPermissionsResultListener(permissionManager!!)
        activityPluginBinding = null
        locationStrategy!!.onDestroy()
        locationStrategy = null
        permissionManager!!.onDestroy()
        permissionManager = null
    }
}
