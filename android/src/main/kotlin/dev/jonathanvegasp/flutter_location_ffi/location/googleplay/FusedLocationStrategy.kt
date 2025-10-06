package dev.jonathanvegasp.flutter_location_ffi.location.googleplay

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.annotation.VisibleForTesting
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.SettingsClient
import dev.jonathanvegasp.flutter_location_ffi.location.LocationConstants
import dev.jonathanvegasp.flutter_location_ffi.location.LocationStrategy
import dev.jonathanvegasp.flutter_location_ffi.nmea.NmeaManager
import dev.jonathanvegasp.flutter_location_ffi.provider.LocationStatusChecker
import dev.jonathanvegasp.flutter_location_ffi.provider.LocationStatusCheckerCoordinator
import dev.jonathanvegasp.flutter_location_ffi.provider.LocationStatusCheckerDelegate
import dev.jonathanvegasp.flutter_location_ffi.provider.SettingsLocationStatusCheckerDelegate
import dev.jonathanvegasp.flutter_location_ffi.settings.AndroidLocationSettings
import dev.jonathanvegasp.result_channel.ResultChannel
import io.flutter.plugin.common.PluginRegistry

internal class FusedLocationStrategy(
    private var settings: AndroidLocationSettings,
    private val nmeaManager: NmeaManager,
    private val locationProviderClient: FusedLocationProviderClient,
    private val settingsClient: SettingsClient,
) : LocationStrategy, PluginRegistry.ActivityResultListener {
    private var activity: Activity? = null
    private var locationCallback: FusedLocationStreamCallback? = null
    private val locationStatusCheckerCoordinator = LocationStatusCheckerCoordinator()

    override val isListening: Boolean
        get() = locationCallback != null

    fun setActivity(activity: Activity?) {
        this.activity = activity
    }

    private fun checkProviderStatus(delegate: LocationStatusCheckerDelegate) {
        val statusChecker = LocationStatusChecker(
            activity,
            settings,
            LocationRequest.Builder(
                settings.getPriority(),
                settings.intervalMs
            )
                .setGranularity(settings.granularity)
                .setWaitForAccurateLocation(settings.waitForAccurateLocation)
                .setDurationMillis(settings.durationMs)
                .setMinUpdateDistanceMeters(settings.minUpdateDistanceMeters)
                .setMinUpdateIntervalMillis(settings.minUpdateIntervalMs)
                .setMaxUpdateDelayMillis(settings.maxUpdateDelayMs)
                .setMaxUpdateAgeMillis(settings.maxUpdateAgeMillis)
                .setMaxUpdates(settings.maxUpdates)
                .build(),
            settingsClient,
            locationStatusCheckerCoordinator,
            delegate,
        )

        statusChecker.check()
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun requestLocation(
        locationRequest: LocationRequest,
        locationCallback: LocationCallback
    ) {
        nmeaManager.startUpdates()

        locationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.myLooper()
        )
    }

    override fun setSettings(settings: AndroidLocationSettings) {
        this.settings = settings
        locationCallback?.setSettings(settings)
    }

    override fun isServiceEnabled(channel: ResultChannel) {
        checkProviderStatus(SettingsLocationStatusCheckerDelegate(channel))
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun getCurrent(result: ResultChannel) {
        checkProviderStatus(FusedLocationSingleUpdateDelegate(result, this))
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun startUpdates(result: ResultChannel) {
        checkProviderStatus(FusedLocationStreamDelegate(result, this))
    }

    override fun stopUpdates() {
        nmeaManager.stopUpdates()
        locationCallback?.also {
            it.onDestroy()
            locationCallback = null
        }
    }

    override fun onDestroy() {
        stopUpdates()
        nmeaManager.onDestroy()
        activity = null
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun getCurrentOnProviderEnabled(
        resultChannel: ResultChannel,
        locationRequest: LocationRequest
    ) = requestLocation(
        locationRequest,
        FusedLocationCallback(settings, resultChannel, locationProviderClient, nmeaManager)
    )


    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun startUpdatesOnProviderEnabled(
        resultChannel: ResultChannel,
        locationRequest: LocationRequest
    ) {
        locationCallback?.also {
            it.onDestroy()
        }

        val callback = FusedLocationStreamCallback(
            settings,
            resultChannel,
            locationProviderClient,
            nmeaManager
        )

        requestLocation(locationRequest, callback)

        locationCallback = callback
    }

    fun onProviderDisabled(resultChannel: ResultChannel) {
        resultChannel.failure(LocationConstants.PROVIDER_DISABLED)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean =
        locationStatusCheckerCoordinator.onActivityResult(requestCode, resultCode, data)
}
