package dev.jonathanvegasp.flutter_location_ffi.permission.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dev.jonathanvegasp.flutter_location_ffi.permission.PermissionStatus
import dev.jonathanvegasp.flutter_location_ffi.permission.PermissionUtils
import dev.jonathanvegasp.result_channel.ResultChannel
import io.flutter.plugin.common.PluginRegistry

internal class BackgroundPermissionManager(private val activity: Activity) :
    PluginRegistry.RequestPermissionsResultListener {
    companion object {
        private const val BACKGROUND_REQUEST_PERMISSION = 1002
    }

    private var channel: ResultChannel? = null

    private fun hasPermission(activity: Activity): Boolean {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            }

            else -> {
                true
            }
        }
    }

    @SuppressLint("NewApi")
    fun checkAndRequestPermission(channel: ResultChannel) {
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
            arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
            BACKGROUND_REQUEST_PERMISSION
        )
    }

    @SuppressLint("NewApi")
    fun checkPermission(): Int {
        val activity = activity

        if (hasPermission(activity)) {
            return PermissionStatus.GRANTED
        }

        return PermissionStatus.DENIED
    }

    fun onDestroy() {
        channel?.also {
            it.failure(null)
            channel = null
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ): Boolean {
        if (BACKGROUND_REQUEST_PERMISSION != requestCode) return false
        val channel = channel ?: return false
        val activity = activity

        val permission = permissions[0]

        if (permission != Manifest.permission.ACCESS_BACKGROUND_LOCATION) return false

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            channel.success(PermissionStatus.GRANTED)
            this.channel = null
            return true
        }

        val result = when {
            ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                permission
            ) -> PermissionStatus.DENIED

            else -> PermissionStatus.NEVER_ASK_AGAIN
        }

        channel.success(result)

        this.channel = null

        return true
    }
}