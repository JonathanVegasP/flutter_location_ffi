package dev.jonathanvegasp.flutter_location_ffi.location.android

import android.Manifest
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.core.location.LocationListenerCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.location.LocationRequestCompat
import dev.jonathanvegasp.flutter_location_ffi.location.LocationConstants
import dev.jonathanvegasp.flutter_location_ffi.location.LocationStrategy
import dev.jonathanvegasp.flutter_location_ffi.nmea.NmeaManager
import dev.jonathanvegasp.flutter_location_ffi.settings.AndroidLocationSettings
import dev.jonathanvegasp.result_channel.ResultChannel

internal class LocationManagerStrategy(
    private var settings: AndroidLocationSettings,
    private val locationManager: LocationManager,
    private val nmeaManager: NmeaManager
) : LocationStrategy {

    private var locationListenerStreamCompat: LocationListenerStreamCompat? = null

    override val isListening: Boolean
        get() = locationListenerStreamCompat != null

    override fun setSettings(settings: AndroidLocationSettings) {
        this.settings = settings
        locationListenerStreamCompat?.setSettings(settings)
    }

    override fun isServiceEnabled(channel: ResultChannel) {
        channel.success(LocationManagerCompat.isLocationEnabled(locationManager))
    }

    private fun getProvider(): String? {
        if (settings.isPassive()) {
            return LocationManager.PASSIVE_PROVIDER
        }

        val providers = locationManager.getProviders(true)

        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && providers.contains(LocationManager.FUSED_PROVIDER) -> LocationManager.FUSED_PROVIDER
            providers.contains(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            providers.contains(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> null
        }
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun requestLocation(provider: String, locationListenerCompat: LocationListenerCompat) {
        nmeaManager.startUpdates()

        LocationManagerCompat.requestLocationUpdates(
            locationManager,
            provider,
            LocationRequestCompat.Builder(settings.intervalMs)
                .setQuality(settings.getQuality())
                .setDurationMillis(settings.durationMs)
                .setMinUpdateDistanceMeters(settings.minUpdateDistanceMeters)
                .setMinUpdateIntervalMillis(settings.minUpdateIntervalMs)
                .setMaxUpdateDelayMillis(settings.maxUpdateDelayMs)
                .setMaxUpdates(settings.maxUpdates)
                .build(),
            locationListenerCompat,
            Looper.myLooper()!!
        )
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun getCurrent(result: ResultChannel) {
        val provider = getProvider()

        if (provider == null) {
            result.failure(LocationConstants.PROVIDER_DISABLED)
            return
        }

        requestLocation(
            provider,
            LocationListenerCallbackCompat(settings, result, locationManager, nmeaManager)
        )
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun startUpdates(result: ResultChannel) {
        val provider = getProvider()

        if (provider == null) {
            result.failure(LocationConstants.PROVIDER_DISABLED)
            return
        }

        locationListenerStreamCompat?.also {
            it.onDestroy()
        }

        val listener = LocationListenerStreamCompat(settings, result, locationManager, nmeaManager)

        requestLocation(provider, listener)

        locationListenerStreamCompat = listener
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun stopUpdates() {
        nmeaManager.stopUpdates()
        locationListenerStreamCompat?.also {
            it.onDestroy()
            locationListenerStreamCompat = null
        }
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onDestroy() {
        stopUpdates()
        nmeaManager.onDestroy()
    }
}
