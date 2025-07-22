package dev.jonathanvegasp.flutter_location_ffi

import android.Manifest
import android.location.LocationManager
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.core.location.LocationManagerCompat
import androidx.core.location.LocationRequestCompat
import dev.jonathanvegasp.result_channel.ResultChannel

class LocationManagerStrategy(
    private val locationManager: LocationManager,
    private var settings: AndroidLocationSettings
) : LocationStrategy {
    private var locationListenerStreamCompat: LocationListenerStreamCompat? = null

    override fun setSettings(settings: AndroidLocationSettings) {
        this.settings = settings
    }

    private fun buildLocationRequest(): LocationRequestCompat =
        LocationRequestCompat.Builder(settings.intervalMs)
            .setQuality(settings.priority.level)
            .setDurationMillis(settings.durationMs)
            .setMinUpdateDistanceMeters(settings.minUpdateDistanceMeters)
            .setMinUpdateIntervalMillis(settings.minUpdateIntervalMs)
            .setMaxUpdateDelayMillis(settings.maxUpdateDelayMs)
            .setMaxUpdates(settings.maxUpdates)
            .build()

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun getCurrent(result: ResultChannel) {
        val locationManager = locationManager

        if (!LocationManagerCompat.isLocationEnabled(locationManager)) {
            result.success(LocationDataFactory.create())

            return
        }

        LocationManagerCompat.requestLocationUpdates(
            locationManager,
            LocationManager.GPS_PROVIDER,
            buildLocationRequest(),
            LocationListenerCallbackCompat(result, locationManager),
            Looper.myLooper()!!
        )
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun startUpdates(result: ResultChannel) {
        val locationManager = locationManager

        if (!LocationManagerCompat.isLocationEnabled(locationManager)) {
            result.success(LocationDataFactory.create())
        }

        val listener = LocationListenerStreamCompat(result, locationManager)

        LocationManagerCompat.requestLocationUpdates(
            locationManager,
            LocationManager.GPS_PROVIDER,
            buildLocationRequest(),
            listener,
            Looper.myLooper()!!
        )

        locationListenerStreamCompat = listener
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun stopUpdates() {
        locationListenerStreamCompat?.onDestroy()

        locationListenerStreamCompat = null
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onDestroy() {
        stopUpdates()
    }
}
