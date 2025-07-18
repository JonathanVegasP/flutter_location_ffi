package dev.jonathanvegasp.flutter_location_ffi

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dev.jonathanvegasp.result_channel.ResultChannel

class LocationPermissionManager(private val activity: Activity, private val context: Context) :
    PermissionManager {
    private var channel: ResultChannel? = null

    override fun checkAndRequestPermission(channel: ResultChannel) {
        val activity = activity

        val hasPermission = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            channel.success(LocationPermission.GRANTED.ordinal)
            return
        }

        this.channel = channel

        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PermissionManager.LOCATION_REQUEST_PERMISSION
        )
    }

    override fun checkPermission(): LocationPermission {
        val activity = activity

        val hasPermission = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            return LocationPermission.GRANTED
        }

        val canAskFine = ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        return if (canAskFine) {
            LocationPermission.DENIED
        } else {
            LocationPermission.NEVER_ASK_AGAIN
        }
    }

    override fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
        }

        ContextCompat.startActivity(context, intent, null)
    }

    override fun openPermissionSettings() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
        }

        ContextCompat.startActivity(context, intent, null)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ): Boolean {
        if (PermissionManager.LOCATION_REQUEST_PERMISSION != requestCode) return false
        val channel = channel ?: return false
        val activity = activity
        val size = permissions.size

        repeat(size) {
            val permission = permissions[it]
            val result = grantResults[it]

            if (permission != Manifest.permission.ACCESS_FINE_LOCATION) {
                return false
            }

            if (result != PackageManager.PERMISSION_GRANTED) {
                val granted =
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                        LocationPermission.DENIED
                    } else {
                        LocationPermission.NEVER_ASK_AGAIN
                    }

                channel.success(granted.ordinal)
                this.channel = null
                return true
            }
        }

        channel.success(LocationPermission.GRANTED.ordinal)
        this.channel = null
        return true
    }

    override fun onDestroy() {
        channel?.failure(null)
        channel = null
    }
}
