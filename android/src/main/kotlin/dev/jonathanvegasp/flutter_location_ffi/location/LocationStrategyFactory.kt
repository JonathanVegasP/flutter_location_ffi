package dev.jonathanvegasp.flutter_location_ffi.location

import android.content.Context
import android.location.LocationManager
import android.os.Build
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailabilityLight
import com.google.android.gms.location.LocationServices
import dev.jonathanvegasp.flutter_location_ffi.location.android.LocationManagerStrategy
import dev.jonathanvegasp.flutter_location_ffi.location.googleplay.FusedLocationStrategy
import dev.jonathanvegasp.flutter_location_ffi.nmea.LegacyNmeaManager
import dev.jonathanvegasp.flutter_location_ffi.nmea.NmeaEventManager
import dev.jonathanvegasp.flutter_location_ffi.settings.AndroidLocationSettings

internal object LocationStrategyFactory {
    @JvmStatic
    fun create(
        context: Context,
    ): LocationStrategy {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val nmeaManager =
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> NmeaEventManager(locationManager)
                else -> LegacyNmeaManager(
                    locationManager
                )
            }

        val settings = AndroidLocationSettings.default()

        return when {
            GoogleApiAvailabilityLight.getInstance()
                .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS -> {
                FusedLocationStrategy(
                    settings,
                    nmeaManager,
                    LocationServices.getFusedLocationProviderClient(context),
                    LocationServices.getSettingsClient(context),
                )
            }

            else -> {
                LocationManagerStrategy(
                    settings,
                    locationManager,
                    nmeaManager,
                )
            }
        }
    }
}
