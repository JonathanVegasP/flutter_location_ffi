package dev.jonathanvegasp.flutter_location_ffi

import android.Manifest
import android.os.Looper
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import dev.jonathanvegasp.result_channel.ResultChannel

class FusedLocationStrategy(
    private val locationProviderClient: FusedLocationProviderClient,
    private val statusChecker: StatusChecker
) : LocationStrategy {
    companion object {
        @JvmStatic
        private fun buildLocationRequest(): LocationRequest =
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
                .setWaitForAccurateLocation(true)
                .setMinUpdateDistanceMeters(1F)
                .setMinUpdateIntervalMillis(1000L)
                .build()
    }

    private var locationCallback: FusedLocationStreamCallback? = null

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun getCurrent(result: ResultChannel) {
        val statusChecker = statusChecker

        if (!statusChecker.isEnabled()) {
            result.success(LocationDataFactory.create())

            return
        }

        val locationProviderClient = locationProviderClient

        locationProviderClient.requestLocationUpdates(
            buildLocationRequest(),
            FusedLocationCallback(result, statusChecker, locationProviderClient),
            Looper.myLooper()
        )
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun startUpdates(result: ResultChannel) {
        if (locationCallback != null) {
            result.failure(LocationStrategy.ON_START_ERROR)
            return
        }

        val statusChecker = statusChecker

        if (!statusChecker.isEnabled()) {
            result.success(LocationDataFactory.create())
        }

        val locationProviderClient = locationProviderClient
        val callback = FusedLocationStreamCallback(result, statusChecker, locationProviderClient)

        locationProviderClient.requestLocationUpdates(
            buildLocationRequest(),
            callback,
            Looper.myLooper()
        )

        locationCallback = callback
    }

    override fun stopUpdates() {
        locationCallback?.onDestroy()
        locationCallback = null
    }

    override fun onDestroy() {
        stopUpdates()
    }
}
