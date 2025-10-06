package dev.jonathanvegasp.flutter_location_ffi.permission

import android.content.Intent

internal object PermissionUtils {
    @JvmStatic
    fun buildIntent(action: String) = Intent(action).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP or
                Intent.FLAG_ACTIVITY_NO_HISTORY or
                Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
    }
}