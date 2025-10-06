package dev.jonathanvegasp.flutter_location_ffi.location

import android.app.Service
import android.content.Context
import android.location.LocationManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailabilityLight
import com.google.android.gms.location.LocationServices
import dev.jonathanvegasp.flutter_location_ffi.location.android.LocationManagerStrategy
import dev.jonathanvegasp.flutter_location_ffi.location.googleplay.FusedLocationStrategy
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito.RETURNS_MOCKS
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class LocationStrategyFactoryTest {
    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var googleApiAvailabilityLight: GoogleApiAvailabilityLight

    @Mock
    private lateinit var locationManager: LocationManager

    private lateinit var locationServices: MockedStatic<LocationServices>

    private lateinit var api: MockedStatic<GoogleApiAvailabilityLight>

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        `when`(context.getSystemService(Service.LOCATION_SERVICE)).thenReturn(locationManager)
        api = mockStatic(GoogleApiAvailabilityLight::class.java)
        api.`when`<Any> { GoogleApiAvailabilityLight.getInstance() }
            .thenReturn(googleApiAvailabilityLight)
        locationServices = mockStatic(LocationServices::class.java, RETURNS_MOCKS)
    }

    @After
    fun tearDown() {
        locationServices.close()
        api.close()
    }

    @Test
    @Config(
        sdk = [21]
    )
    fun `test create creates location strategy with nmea legacy`() {
        `when`(googleApiAvailabilityLight.isGooglePlayServicesAvailable(context)).thenReturn(
            ConnectionResult.API_UNAVAILABLE
        )
        assertTrue(LocationStrategyFactory.create(context) is LocationManagerStrategy)
        `when`(googleApiAvailabilityLight.isGooglePlayServicesAvailable(context)).thenReturn(
            ConnectionResult.SUCCESS
        )
        assertTrue(LocationStrategyFactory.create(context) is FusedLocationStrategy)
    }

    @Test
    @Config(
        sdk = [24]
    )
    fun `test create creates location strategy`() {
        `when`(googleApiAvailabilityLight.isGooglePlayServicesAvailable(context)).thenReturn(
            ConnectionResult.API_UNAVAILABLE
        )
        assertTrue(LocationStrategyFactory.create(context) is LocationManagerStrategy)
        `when`(googleApiAvailabilityLight.isGooglePlayServicesAvailable(context)).thenReturn(
            ConnectionResult.SUCCESS
        )
        assertTrue(LocationStrategyFactory.create(context) is FusedLocationStrategy)
    }
}