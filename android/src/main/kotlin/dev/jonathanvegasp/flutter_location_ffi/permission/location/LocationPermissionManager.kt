package dev.jonathanvegasp.flutter_location_ffi.permission.location

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dev.jonathanvegasp.flutter_location_ffi.permission.PermissionStatus
import dev.jonathanvegasp.flutter_location_ffi.permission.PermissionUtils
import dev.jonathanvegasp.result_channel.ResultChannel
import io.flutter.plugin.common.PluginRegistry

internal class LocationPermissionManager(private val activity: Activity) :
    PluginRegistry.RequestPermissionsResultListener {
    companion object {
        private const val LOCATION_REQUEST_PERMISSION = 1001
    }

    private var channel: ResultChannel? = null

    private fun hasPermission(activity: Activity): Boolean {
        val hasCoarse = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasFine = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return hasCoarse || hasFine
    }

    fun checkAndRequestPermission(channel: ResultChannel) {
        val activity = activity

        if (hasPermission(activity)) {
            channel.success(PermissionStatus.GRANTED)
            return
        }

        this.channel?.also {
            it.failure(null)
        }

        this.channel = channel

        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ),
            LOCATION_REQUEST_PERMISSION
        )
    }

    fun checkPermission(): Int {
        val activity = activity

        if (hasPermission(activity)) {
            return PermissionStatus.GRANTED
        }

        return PermissionStatus.DENIED
    }

    fun openAppSettings() {
        ContextCompat.startActivity(
            activity,
            PermissionUtils.buildIntent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", activity.packageName, null)
            },
            null
        )
    }

    fun openPermissionSettings() {
        ContextCompat.startActivity(
            activity,
            PermissionUtils.buildIntent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
            null
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ): Boolean {
        if (LOCATION_REQUEST_PERMISSION != requestCode) return false
        val channel = channel ?: return false

        val coarse = permissions[0]

        if (coarse != Manifest.permission.ACCESS_COARSE_LOCATION) {
            return false
        }

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            channel.success(PermissionStatus.GRANTED)
            this.channel = null
            return true
        }

        val fine = permissions[1]

        if (fine != Manifest.permission.ACCESS_FINE_LOCATION) return false

        if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            channel.success(PermissionStatus.GRANTED)

            this.channel = null

            return true
        }

        val result = when {
            ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                coarse
            ) -> {
                PermissionStatus.DENIED
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                fine
            ) -> {
                PermissionStatus.DENIED
            }

            else -> {
                PermissionStatus.NEVER_ASK_AGAIN
            }
        }

        channel.success(result)

        this.channel = null

        return true
    }

    fun onDestroy() {
        channel?.also {
            it.failure(null)
            channel = null
        }
    }
}
