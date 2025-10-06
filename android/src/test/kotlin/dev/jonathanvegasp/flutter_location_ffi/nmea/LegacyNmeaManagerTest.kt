package dev.jonathanvegasp.flutter_location_ffi.nmea

import android.location.LocationManager
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@Suppress("DEPRECATION")
@RunWith(RobolectricTestRunner::class)
class LegacyNmeaManagerTest {
    @Mock
    private lateinit var locationManager: LocationManager

    private lateinit var legacyNmeaManager: LegacyNmeaManager

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        legacyNmeaManager = LegacyNmeaManager(locationManager)
    }

    private inline fun getNmeaListener(block: () -> Unit): LegacyNmeaStreamListener {
        val argumentCaptor = ArgumentCaptor.forClass(LegacyNmeaStreamListener::class.java)
        block()
        verify(locationManager, atLeastOnce()).addNmeaListener(argumentCaptor.capture())
        return argumentCaptor.value
    }

    @Test
    fun `test startUpdates starts message updates`() {
        var listener = getNmeaListener {
            legacyNmeaManager.startUpdates()
        }

        listener.onNmeaReceived(System.currentTimeMillis(), null)

        listener = getNmeaListener {
            legacyNmeaManager.startUpdates()
            verify(locationManager).removeNmeaListener(listener)
        }

        listener.onNmeaReceived(System.currentTimeMillis(), "")
    }

    @Test
    fun `test stopUpdates stops message updates`() {
        legacyNmeaManager.stopUpdates()

        verifyNoInteractions(locationManager)

        val listener = getNmeaListener {
            legacyNmeaManager.startUpdates()
        }

        listener.onNmeaReceived(System.currentTimeMillis(), "1234567")

        legacyNmeaManager.stopUpdates()

        verify(locationManager).removeNmeaListener(listener)
    }
}