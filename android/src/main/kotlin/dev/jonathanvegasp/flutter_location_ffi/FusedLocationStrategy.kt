package dev.jonathanvegasp.flutter_location_ffi

import android.Manifest
import android.os.Looper
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import dev.jonathanvegasp.result_channel.ResultChannel

class FusedLocationStrategy(
    private val locationProviderClient: FusedLocationProviderClient,
    private val statusChecker: StatusChecker,
    private var settings: AndroidLocationSettings
) : LocationStrategy {
    private var locationCallback: FusedLocationStreamCallback? = null

    override fun setSettings(settings: AndroidLocationSettings) {
        this.settings = settings
        locationCallback?.setSettings(settings)
    }

    private fun buildLocationRequest(): LocationRequest =
        LocationRequest.Builder(settings.priority.level, settings.intervalMs)
            .setGranularity(settings.granularity)
            .setWaitForAccurateLocation(settings.waitForAccurateLocation)
            .setMinUpdateDistanceMeters(settings.minUpdateDistanceMeters)
            .setMinUpdateIntervalMillis(settings.minUpdateIntervalMs)
            .setMaxUpdateDelayMillis(settings.maxUpdateDelayMs)
            .setMaxUpdateAgeMillis(settings.maxUpdateAgeMillis)
            .setMaxUpdates(settings.maxUpdates)
            .build()

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
            FusedLocationCallback(settings, result, statusChecker, locationProviderClient),
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
        val callback =
            FusedLocationStreamCallback(settings, result, statusChecker, locationProviderClient)

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
