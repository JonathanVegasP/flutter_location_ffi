package dev.jonathanvegasp.flutter_location_ffi

import android.content.Context
import android.location.LocationManager
import android.os.Build
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailabilityLight
import com.google.android.gms.location.LocationServices

object LocationStrategyFactory {
    private var _isGooglePlayAvailable = false

    val isGooglePlayAvailable: Boolean
        get() = _isGooglePlayAvailable

    fun create(context: Context): LocationStrategy {
        val isAvailable = GoogleApiAvailabilityLight.getInstance()
            .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS

        _isGooglePlayAvailable = isAvailable

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val nmeaManager =
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> NmeaEventManager(locationManager)
                else -> LegacyNmeaManager(
                    locationManager
                )
            }

        return if (isAvailable) {
            FusedLocationStrategy(
                LocationServices.getFusedLocationProviderClient(context),
                LocationStatusChecker(locationManager),
                nmeaManager,
                AndroidLocationSettings.default(AndroidLocationPriority.PRIORITY_BALANCED_POWER)
            )
        } else {
            LocationManagerStrategy(
                locationManager,
                nmeaManager,
                AndroidLocationSettings.default(AndroidLegacyLocationPriority.PRIORITY_BALANCED_POWER)
            )
        }
    }
}
