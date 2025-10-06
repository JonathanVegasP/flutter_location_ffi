package dev.jonathanvegasp.flutter_location_ffi.location.android

import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import androidx.core.location.LocationListenerCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.location.LocationRequestCompat
import dev.jonathanvegasp.flutter_location_ffi.location.LocationConstants
import dev.jonathanvegasp.flutter_location_ffi.location.LocationDataFactory
import dev.jonathanvegasp.flutter_location_ffi.nmea.NmeaManager
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
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.MockedConstruction
import org.mockito.MockedStatic
import org.mockito.Mockito.RETURNS_MOCKS
import org.mockito.Mockito.RETURNS_SELF
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.mockConstruction
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.Mockito.`when`
import org.mockito.Mockito.withSettings
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(
    sdk = [21]
)
internal class LocationManagerStrategyTest {
    @Mock
    private lateinit var settings: AndroidLocationSettings

    @Mock
    private lateinit var mockLocationManager: LocationManager

    @Mock
    private lateinit var mockNmeaManager: NmeaManager

    @Mock
    private lateinit var mockResultChannel: ResultChannel

    @Mock
    private lateinit var location: Location

    private lateinit var locationRequestCompat: MockedConstruction<LocationRequestCompat.Builder>

    private lateinit var strategy: LocationManagerStrategy

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        locationRequestCompat = mockConstruction(
            LocationRequestCompat.Builder::class.java, withSettings().defaultAnswer {
                if (it.method.returnType == LocationRequestCompat.Builder::class.java) {
                    return@defaultAnswer RETURNS_SELF.answer(it)
                }

                RETURNS_MOCKS.answer(it)
            }
        )
        strategy = LocationManagerStrategy(
            settings,
            mockLocationManager,
            mockNmeaManager
        )
    }

    @After
    fun tearDown() {
        locationRequestCompat.close()
    }

    private fun LocationListenerCompat.testRemove(it: MockedStatic<LocationManagerCompat>) {
        it.verify {
            LocationManagerCompat.removeUpdates(mockLocationManager, this)
        }

        verify(mockResultChannel, atLeastOnce()).failure(null)
    }

    private inline fun LocationListenerCompat.testRemove(block: () -> Unit) {
        mockStaticClass<LocationManagerCompat> {
            block()

            it.verify {
                LocationManagerCompat.removeUpdates(mockLocationManager, this)
            }
        }
    }

    private inline fun <reified T : LocationListenerCompat> getLocationListenerCompat(
        provider: String,
        block: (MockedStatic<LocationManagerCompat>) -> Unit
    ): T {
        `when`(mockLocationManager.getProviders(true)).thenReturn(listOf(provider))

        val argumentCaptor = ArgumentCaptor.forClass(T::class.java)

        mockStaticClass<LocationManagerCompat> {
            block(it)

            it.verify {
                LocationManagerCompat.requestLocationUpdates(
                    eq(mockLocationManager),
                    eq(provider),
                    any(LocationRequestCompat::class.java),
                    argumentCaptor.capture(),
                    any(Looper::class.java)
                )
            }
        }

        verify(mockNmeaManager, atLeastOnce()).startUpdates()

        return argumentCaptor.value
    }

    private inline fun testLocationData(block: () -> Unit) {
        val data = emptyArray<Any?>()

        mockStaticClass<LocationDataFactory> {
            it.`when`<Any> { LocationDataFactory.create(0F, location) }.thenReturn(data)

            block()

            it.verify { LocationDataFactory.create(0F, location) }
        }

        verify(mockNmeaManager, atLeastOnce()).setAltitudeMsl(location)

        verify(mockResultChannel, atLeastOnce()).success(data)
    }

    private inline fun noTestLocationData(block: () -> Unit) {
        mockStaticClass<LocationManagerCompat> { mockedStatic ->
            mockStaticClass<LocationDataFactory> {
                block()

                it.verifyNoInteractions()
            }

            mockedStatic.verifyNoInteractions()
        }

        verifyNoMoreInteractions(mockNmeaManager)

        verifyNoMoreInteractions(mockResultChannel)
    }

    private inline fun testLocationDataWithoutArgs(block: () -> Unit) {
        val data = emptyArray<Any?>()

        mockStaticClass<LocationDataFactory> {
            it.`when`<Any> { LocationDataFactory.create() }.thenReturn(data)

            block()

            it.verify { LocationDataFactory.create() }
        }

        verify(mockResultChannel, atLeastOnce()).success(data)
    }

    @Test
    fun `test isServiceEnabled when location is enabled`() {
        `when`(LocationManagerCompat.isLocationEnabled(mockLocationManager)).thenReturn(true)

        strategy.isServiceEnabled(mockResultChannel)

        verify(mockResultChannel).success(true)
    }

    @Test
    fun `test isServiceEnabled when location is false`() {
        `when`(LocationManagerCompat.isLocationEnabled(mockLocationManager)).thenReturn(false)

        strategy.isServiceEnabled(mockResultChannel)

        verify(mockResultChannel).success(false)
    }

    @Test
    fun `test getCurrent returns provider disabled when no provider available`() {
        `when`(mockLocationManager.getProviders(true)).thenReturn(emptyList())

        strategy.getCurrent(mockResultChannel)

        verify(mockResultChannel).failure(LocationConstants.PROVIDER_DISABLED)
    }

    @Test
    @Config(
        sdk = [Build.VERSION_CODES.S]
    )
    fun `test startUpdates returns provider disabled when no provider available`() {
        `when`(mockLocationManager.getProviders(true)).thenReturn(emptyList())

        strategy.startUpdates(mockResultChannel)

        verify(mockResultChannel).failure(LocationConstants.PROVIDER_DISABLED)
    }

    @Test
    fun `test setSettings changes provider rules for subsequent locations`() {
        `when`(settings.isPassive()).thenReturn(true)
        `when`(settings.validate(location)).thenReturn(0F)

        strategy.setSettings(settings)

        var callback: LocationListenerStreamCompat =
            getLocationListenerCompat(LocationManager.PASSIVE_PROVIDER) {
                strategy.startUpdates(mockResultChannel)
            }

        testLocationData {
            callback.onLocationChanged(location)
        }

        assertTrue(strategy.isListening)

        `when`(settings.isPassive()).thenReturn(false)
        `when`(settings.validate(location)).thenReturn(null)

        strategy.setSettings(settings)

        callback = getLocationListenerCompat(LocationManager.GPS_PROVIDER) {
            strategy.startUpdates(mockResultChannel)

            callback.testRemove(it)
        }

        noTestLocationData {
            callback.onLocationChanged(location)
        }

        assertTrue(strategy.isListening)
    }

    @Test
    fun `test getCurrent starts location updates`() {
        val callback: LocationListenerCallbackCompat =
            getLocationListenerCompat(LocationManager.GPS_PROVIDER) {
                strategy.getCurrent(mockResultChannel)
            }

        testLocationData {
            callback.testRemove {
                callback.onLocationChanged(location)
            }
        }

        assertFalse(strategy.isListening)

        `when`(settings.validate(location)).thenReturn(null)

        noTestLocationData {
            callback.onLocationChanged(location)
        }

        callback.testRemove {
            testLocationDataWithoutArgs {
                callback.onProviderDisabled(LocationManager.GPS_PROVIDER)
            }
        }
    }

    @Test
    fun `test startUpdates starts location updates`() {
        val callback: LocationListenerStreamCompat =
            getLocationListenerCompat(LocationManager.NETWORK_PROVIDER) {
                strategy.startUpdates(mockResultChannel)
            }

        testLocationData {
            callback.onLocationChanged(location)
        }

        assertTrue(strategy.isListening)

        testLocationDataWithoutArgs {
            callback.onProviderDisabled(LocationManager.NETWORK_PROVIDER)
        }
    }

    @Test
    @Config(
        sdk = [Build.VERSION_CODES.S]
    )
    fun `test stopUpdates stops location updates`() {
        val listener: LocationListenerStreamCompat =
            getLocationListenerCompat(LocationManager.FUSED_PROVIDER) {
                strategy.startUpdates(mockResultChannel)
            }

        testLocationData {
            listener.onLocationChanged(location)
        }

        assertTrue(strategy.isListening)

        strategy.stopUpdates()

        verify(mockNmeaManager).stopUpdates()

        assertFalse(strategy.isListening)
    }

    @Test
    fun `test onDestroy calls stopUpdates`() {
        strategy.onDestroy()

        verify(mockNmeaManager).stopUpdates()

        verify(mockNmeaManager).onDestroy()

        assertFalse(strategy.isListening)
    }
}