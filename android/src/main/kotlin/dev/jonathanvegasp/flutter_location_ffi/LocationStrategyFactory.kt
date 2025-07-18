package dev.jonathanvegasp.flutter_location_ffi

import android.content.Context
import android.location.LocationManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailabilityLight
import com.google.android.gms.location.LocationServices

object LocationStrategyFactory {
    fun create(context: Context): LocationStrategy {
        val isAvailable =
            GoogleApiAvailabilityLight.getInstance()
                .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return if (isAvailable) {
            FusedLocationStrategy(
                LocationServices.getFusedLocationProviderClient(context),
                LocationStatusChecker(locationManager)
            )
        } else {
            LocationManagerStrategy(locationManager)
        }
    }
}
