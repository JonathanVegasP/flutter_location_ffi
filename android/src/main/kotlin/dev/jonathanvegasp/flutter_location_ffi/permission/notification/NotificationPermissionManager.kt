package dev.jonathanvegasp.flutter_location_ffi.permission.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
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

internal class NotificationPermissionManager(private val activity: Activity) :
    PluginRegistry.RequestPermissionsResultListener {
    companion object {
        private const val NOTIFICATION_REQUEST_PERMISSION = 1003
    }

    private var channel: ResultChannel? = null

    private fun hasPermission(activity: Activity): Boolean {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.POST_NOTIFICATIONS
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
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            NOTIFICATION_REQUEST_PERMISSION
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

    fun openPermissionSettings() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                ContextCompat.startActivity(
                    activity,
                    PermissionUtils.buildIntent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, activity.packageName)
                    },
                    null
                )
            }

            else -> {
                ContextCompat.startActivity(
                    activity,
                    PermissionUtils.buildIntent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", activity.packageName, null)
                    },
                    null
                )
            }
        }
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
        if (requestCode != NOTIFICATION_REQUEST_PERMISSION) return false
        val channel = channel ?: return false

        val permission = permissions[0]

        if (permission != Manifest.permission.POST_NOTIFICATIONS) return false

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            channel.success(PermissionStatus.GRANTED)

            this.channel = null

            return true
        }

        val result = when {
            ActivityCompat.shouldShowRequestPermissionRationale(activity, permission) -> {
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
}