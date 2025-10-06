package dev.jonathanvegasp.flutter_location_ffi.provider

import android.app.Activity
import android.content.Intent
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import dev.jonathanvegasp.flutter_location_ffi.settings.AndroidLocationSettings
import io.flutter.plugin.common.PluginRegistry.ActivityResultListener

internal class LocationStatusChecker(
    private val activity: Activity?,
    private val settings: AndroidLocationSettings,
    private val locationRequest: LocationRequest,
    private val settingsClient: SettingsClient,
    private val locationStatusCheckerCoordinator: LocationStatusCheckerCoordinator,
    private val delegate: LocationStatusCheckerDelegate,
) :
    OnSuccessListener<LocationSettingsResponse>, OnFailureListener, ActivityResultListener {

    companion object {
        private const val PROVIDER_REQUEST_RESOLUTION = 1004
    }

    fun check() {
        settingsClient.checkLocationSettings(
            LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true)
                .build()
        )
            .addOnSuccessListener(this)
            .addOnFailureListener(this)
    }

    override fun onSuccess(p0: LocationSettingsResponse?) {
        val state = p0?.locationSettingsStates ?: return

        if (!state.isLocationUsable) {
            delegate.onProviderDisabled()
            return
        }

        delegate.onProviderEnabled(locationRequest)
    }

    override fun onFailure(p0: Exception) {
        if (activity == null || !settings.showLocationServiceDialogWhenRequested || p0 !is ResolvableApiException) {
            delegate.onProviderDisabled()
            return
        }

        locationStatusCheckerCoordinator.append(this)

        p0.startResolutionForResult(activity, PROVIDER_REQUEST_RESOLUTION)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode != PROVIDER_REQUEST_RESOLUTION) return false

        if (Activity.RESULT_OK == resultCode) {
            delegate.onProviderEnabled(locationRequest)
            return true
        }

        delegate.onProviderDisabled()

        return true
    }
}
