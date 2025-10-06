package dev.jonathanvegasp.flutter_location_ffi.location.googleplay

import android.app.Activity
import android.location.Location
import android.os.Looper
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.LocationSettingsResult
import com.google.android.gms.location.LocationSettingsStates
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task
import dev.jonathanvegasp.flutter_location_ffi.location.LocationConstants
import dev.jonathanvegasp.flutter_location_ffi.location.LocationDataFactory
import dev.jonathanvegasp.flutter_location_ffi.nmea.NmeaManager
import dev.jonathanvegasp.flutter_location_ffi.provider.LocationStatusChecker
import dev.jonathanvegasp.flutter_location_ffi.settings.AndroidLocationSettings
import dev.jonathanvegasp.flutter_location_ffi.utils.mockStaticClass
import dev.jonathanvegasp.result_channel.ResultChannel
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.MockedConstruction
import org.mockito.Mockito.RETURNS_MOCKS
import org.mockito.Mockito.RETURNS_SELF
import org.mockito.Mockito.any
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockConstructionWithAnswer
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class FusedLocationStrategyTest {
    @Mock
    private lateinit var settings: AndroidLocationSettings

    @Mock
    private lateinit var nmeaManager: NmeaManager

    @Mock
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @Mock
    private lateinit var settingsClient: SettingsClient

    @Mock
    private lateinit var resultChannel: ResultChannel

    @Mock
    private lateinit var location: Location

    private val task: Task<LocationSettingsResponse> = mock(RETURNS_SELF)

    private lateinit var locationRequest: MockedConstruction<LocationRequest.Builder>

    private lateinit var fusedLocationStrategy: FusedLocationStrategy

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        locationRequest =
            mockConstructionWithAnswer(LocationRequest.Builder::class.java, RETURNS_MOCKS)

        fusedLocationStrategy = FusedLocationStrategy(
            settings,
            nmeaManager,
            fusedLocationProviderClient,
            settingsClient
        )
    }

    @After
    fun tearDown() {
        locationRequest.close()
    }

    private inline fun getLocationStatusChecker(block: () -> Unit): LocationStatusChecker {
        val argumentCaptor = ArgumentCaptor.forClass(LocationStatusChecker::class.java)

        `when`(settingsClient.checkLocationSettings(any())).thenReturn(task)

        block()

        verify(
            settingsClient,
            atLeastOnce()
        ).checkLocationSettings(any(LocationSettingsRequest::class.java))

        verify(task, atLeastOnce()).addOnSuccessListener(argumentCaptor.capture())

        return argumentCaptor.value
    }

    private fun LocationStatusChecker.checkStatus(boolean: Boolean?) {
        onSuccess(
            LocationSettingsResponse(
                LocationSettingsResult(
                    Status.RESULT_SUCCESS, if (boolean == null) null else
                        LocationSettingsStates(
                            boolean, boolean, boolean, boolean, boolean, boolean
                        )
                )
            )
        )
    }

    private fun configureSuccessLocation(): LocationResult {
        `when`(settings.validate(location)).thenReturn(0F)

        return LocationResult.create(mutableListOf(location))
    }

    private inline fun testLocationData(block: () -> Unit) {
        val data = emptyArray<Any?>()

        mockStaticClass<LocationDataFactory> {
            it.`when`<Any> { LocationDataFactory.create(0F, location) }
                .thenReturn(data)
            block()
            it.verify { LocationDataFactory.create(0F, location) }
        }

        verify(resultChannel, atLeastOnce()).success(data)
    }

    private inline fun testLocationDataWithoutArgs(block: () -> Unit) {
        val data = emptyArray<Any?>()

        mockStaticClass<LocationDataFactory> {
            it.`when`<Any> { LocationDataFactory.create() }
                .thenReturn(data)

            block()

            it.verify { LocationDataFactory.create() }
        }

        verify(resultChannel, atLeastOnce()).success(data)
    }


    private inline fun <reified T : LocationCallback> getLocationCallback(block: () -> Unit): T {
        val argumentCaptor = ArgumentCaptor.forClass(T::class.java)

        block()

        verify(nmeaManager, atLeastOnce()).startUpdates()

        verify(fusedLocationProviderClient, atLeastOnce()).requestLocationUpdates(
            any(LocationRequest::class.java),
            argumentCaptor.capture(),
            any(Looper::class.java)
        )

        return argumentCaptor.value
    }

    @Test
    fun `test isServiceEnabled when location is disabled`() {
        val locationStatus = getLocationStatusChecker {
            fusedLocationStrategy.isServiceEnabled(resultChannel)
        }

        locationStatus.onSuccess(null)

        verifyNoInteractions(resultChannel)

        locationStatus.checkStatus(null)

        verifyNoInteractions(resultChannel)

        locationStatus.checkStatus(false)

        verify(resultChannel).success(false)
    }

    @Test
    fun `test isServiceEnabled when location is enabled`() {

        val statusChecker = getLocationStatusChecker {
            fusedLocationStrategy.isServiceEnabled(resultChannel)
        }

        statusChecker.checkStatus(true)

        verify(settingsClient).checkLocationSettings(any(LocationSettingsRequest::class.java))

        verify(resultChannel).success(true)
    }

    @Test
    fun `test getCurrent returns provider disabled when no provider available`() {
        var statusChecker = getLocationStatusChecker {
            fusedLocationStrategy.getCurrent(resultChannel)
        }

        statusChecker.onFailure(RuntimeException())

        verify(resultChannel).failure(LocationConstants.PROVIDER_DISABLED)

        fusedLocationStrategy.setActivity(mock())

        statusChecker = getLocationStatusChecker {
            fusedLocationStrategy.getCurrent(resultChannel)
        }

        statusChecker.onFailure(RuntimeException())

        verify(resultChannel, times(2)).failure(LocationConstants.PROVIDER_DISABLED)
    }

    @Test
    fun `test startUpdates returns provider disabled when no provider available`() {
        `when`(settings.showLocationServiceDialogWhenRequested).thenReturn(true)

        fusedLocationStrategy.setActivity(mock())

        val statusChecker = getLocationStatusChecker {
            fusedLocationStrategy.startUpdates(resultChannel)
        }

        statusChecker.onFailure(RuntimeException())

        verify(resultChannel).failure(LocationConstants.PROVIDER_DISABLED)

        statusChecker.onFailure(ResolvableApiException(Status.RESULT_SUCCESS))

        verifyNoMoreInteractions(resultChannel)

        fusedLocationStrategy.onActivityResult(0, Activity.RESULT_CANCELED, null)

        verifyNoMoreInteractions(resultChannel)

        statusChecker.onFailure(ResolvableApiException(Status.RESULT_SUCCESS))

        verifyNoMoreInteractions(resultChannel)

        fusedLocationStrategy.onActivityResult(1004, Activity.RESULT_CANCELED, null)

        verify(resultChannel, times(2)).failure(LocationConstants.PROVIDER_DISABLED)
    }

    @Test
    fun `test setSettings changes verification rules when no provider available`() {
        `when`(settings.showLocationServiceDialogWhenRequested).thenReturn(true)

        fusedLocationStrategy.setActivity(mock())

        fusedLocationStrategy.setSettings(settings)

        var statusChecker = getLocationStatusChecker {
            fusedLocationStrategy.startUpdates(resultChannel)
        }

        statusChecker.onFailure(ResolvableApiException(Status.RESULT_SUCCESS))

        var callback: FusedLocationStreamCallback = getLocationCallback {
            fusedLocationStrategy.onActivityResult(1004, Activity.RESULT_OK, null)
        }

        testLocationData {
            callback.onLocationResult(configureSuccessLocation())
        }

        verify(nmeaManager).setAltitudeMsl(location)

        fusedLocationStrategy.setSettings(settings)

        statusChecker = getLocationStatusChecker {
            fusedLocationStrategy.startUpdates(resultChannel)
        }

        callback = getLocationCallback {
            statusChecker.checkStatus(true)
            verify(fusedLocationProviderClient).removeLocationUpdates(callback)
            verify(resultChannel).failure(null)
        }

        `when`(settings.validate(location)).thenReturn(null)

        callback.onLocationResult(LocationResult.create(mutableListOf(location)))

        verifyNoMoreInteractions(nmeaManager)

        verifyNoMoreInteractions(resultChannel)

        callback.onLocationResult(LocationResult.create(mutableListOf()))

        verifyNoMoreInteractions(nmeaManager)

        verifyNoMoreInteractions(resultChannel)
    }

    @Test
    fun `test getCurrent starts location updates`() {
        val statusChecker = getLocationStatusChecker {
            fusedLocationStrategy.getCurrent(resultChannel)
        }

        val callback: FusedLocationCallback = getLocationCallback {
            statusChecker.checkStatus(true)
        }

        assertFalse(fusedLocationStrategy.isListening)

        callback.onLocationResult(LocationResult.create(mutableListOf()))

        verifyNoMoreInteractions(nmeaManager)

        verifyNoInteractions(resultChannel)

        `when`(settings.validate(location)).thenReturn(null)

        callback.onLocationResult(LocationResult.create(mutableListOf(location)))

        verifyNoMoreInteractions(nmeaManager)

        verifyNoInteractions(resultChannel)

        testLocationData {
            callback.onLocationResult(configureSuccessLocation())
        }

        verify(fusedLocationProviderClient).removeLocationUpdates(callback)

        verify(nmeaManager).setAltitudeMsl(location)

        callback.onLocationAvailability(LocationAvailability.zza)

        verifyNoMoreInteractions(fusedLocationProviderClient)

        verifyNoMoreInteractions(resultChannel)

        testLocationDataWithoutArgs {
            callback.onLocationAvailability(LocationAvailability.zzb)
        }

        verify(fusedLocationProviderClient, times(2)).removeLocationUpdates(callback)
    }

    @Test
    fun `test startUpdates starts location updates`() {
        val statusChecker = getLocationStatusChecker {
            fusedLocationStrategy.startUpdates(resultChannel)
        }

        val callback: FusedLocationStreamCallback = getLocationCallback {
            statusChecker.checkStatus(true)
        }

        assertTrue(fusedLocationStrategy.isListening)

        testLocationData {
            callback.onLocationResult(configureSuccessLocation())
        }

        verify(nmeaManager).setAltitudeMsl(location)

        callback.onLocationAvailability(LocationAvailability.zza)

        verifyNoMoreInteractions(resultChannel)

        testLocationDataWithoutArgs {
            callback.onLocationAvailability(LocationAvailability.zzb)
        }
    }

    @Test
    fun `test stopUpdates stops location updates`() {
        val statusChecker = getLocationStatusChecker {
            fusedLocationStrategy.startUpdates(resultChannel)
        }

        val callback: FusedLocationStreamCallback = getLocationCallback {
            statusChecker.checkStatus(true)
        }

        assertTrue(fusedLocationStrategy.isListening)

        testLocationData {
            callback.onLocationResult(configureSuccessLocation())
        }

        verify(nmeaManager).setAltitudeMsl(location)

        fusedLocationStrategy.stopUpdates()

        verify(nmeaManager).stopUpdates()

        assertFalse(fusedLocationStrategy.isListening)
    }

    @Test
    fun `test onDestroy stops location updates`() {
        fusedLocationStrategy.onDestroy()

        verify(nmeaManager).stopUpdates()

        verify(nmeaManager).onDestroy()
    }
}