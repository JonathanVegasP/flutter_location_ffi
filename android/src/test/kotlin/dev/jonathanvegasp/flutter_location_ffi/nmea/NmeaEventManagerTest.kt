package dev.jonathanvegasp.flutter_location_ffi.nmea

import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import dev.jonathanvegasp.flutter_location_ffi.location.LocationDataFactory
import dev.jonathanvegasp.flutter_location_ffi.utils.mockStaticClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(
    sdk = [Build.VERSION_CODES.N]
)
class NmeaEventManagerTest {
    @Mock
    private lateinit var locationManager: LocationManager

    private lateinit var nmeaEventManager: NmeaEventManager

    companion object {
        private const val GGA_FOR_VALIDATION = "\$GPGGA"
        private const val GGA_WITHOUT_VALUE = "\$GPGGA,*"
        private const val GGA_FIRST_POSITION_FALSE =
            "\$GPGGA,092750.000,5321.6802,N,00630.3372,W,1,8,1.03,/61.7,M,55.2,M,,*76"
        private const val GGA_SECOND_POSITION_FALSE =
            "\$GPGGA,092750.000,5321.6802,N,00630.3372,W,1,8,1.03,61.7:,M,55.2,M,,*76"
        private const val GGA_HYPHEN_FALSE =
            "\$GPGGA,092750.000,5321.6802,N,00630.3372,W,1,8,1.03,-6-1.7,M,55.2,M,,*76"
        private const val GGA_DOT_FALSE =
            "\$GPGGA,092750.000,5321.6802,N,00630.3372,W,1,8,1.03,61..7:,M,55.2,M,,*76"
        private const val GGA =
            "\$GPGGA,092750.000,5321.6802,N,00630.3372,W,1,8,1.03,61.7,M,55.2,M,,*76"
        private const val GGA_NEGATIVE =
            "\$GPGGA,092750.000,5321.6802,N,00630.3372,W,1,8,1.03,-61.7,M,55.2,M,,*76"
    }

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        nmeaEventManager = NmeaEventManager(locationManager)
    }

    private inline fun getNmeaListener(block: () -> Unit): NmeaStreamListener {
        val argumentCaptor = ArgumentCaptor.forClass(NmeaStreamListener::class.java)

        block()

        verify(locationManager, atLeastOnce()).addNmeaListener(
            argumentCaptor.capture(),
            any(Handler::class.java)
        )

        return argumentCaptor.value
    }

    private inline fun verifyCalender(block: () -> Unit) {
        mockStaticClass<Calendar> {
            block()
            it.verify { Calendar.getInstance() }
        }
    }

    private inline fun verifyCalenderNeverCalled(block: () -> Unit) {
        mockStaticClass<Calendar> {
            block()
            it.verifyNoInteractions()
        }
    }

    private inline fun verifyCalendarAfter(result: Boolean, block: () -> Unit) {
        mockStaticClass<Calendar> {
            val mock: Calendar = mock()
            it.`when`<Any> { Calendar.getInstance() }.thenReturn(mock)
            `when`(mock.after(any(Calendar::class.java))).thenReturn(result)
            block()
            verify(mock).after(any(Calendar::class.java))
        }
    }

    @Test
    fun `test startUpdates starts message updates`() {
        var listener = getNmeaListener {
            nmeaEventManager.startUpdates()
        }

        listener.onNmeaMessage(null, System.currentTimeMillis())

        listener = getNmeaListener {
            nmeaEventManager.startUpdates()
            verify(locationManager).removeNmeaListener(listener)
        }

        listener.onNmeaMessage("", System.currentTimeMillis())
    }

    @Test
    fun `test stopUpdates stops message updates`() {
        val listener = getNmeaListener {
            nmeaEventManager.startUpdates()
        }

        listener.onNmeaMessage("1234567", System.currentTimeMillis())

        nmeaEventManager.stopUpdates()

        verify(locationManager).removeNmeaListener(listener)
    }

    @Test
    fun `test onDataReceived validates received message`() {
        verifyCalenderNeverCalled {
            nmeaEventManager.onDataReceived("$234567")
        }

        verifyCalenderNeverCalled {
            nmeaEventManager.onDataReceived("$23G567")
        }

        verifyCalenderNeverCalled {
            nmeaEventManager.onDataReceived("$23GG67")
        }

        verifyCalender {
            nmeaEventManager.onDataReceived(GGA_FOR_VALIDATION)
        }
    }

    @Test
    fun `test setAltitudeMsl set value for location object`() {
        val location = Location(LocationManager.GPS_PROVIDER)

        nmeaEventManager.setAltitudeMsl(location)

        assertNotNull(location.extras)
        assertTrue(location.extras!!.isEmpty)

        nmeaEventManager.onDataReceived(GGA_FOR_VALIDATION)

        verifyCalendarAfter(true) {
            nmeaEventManager.setAltitudeMsl(location)
        }

        verifyCalendarAfter(false) {
            nmeaEventManager.setAltitudeMsl(location)
        }

        assertEquals(0.0, location.extras!!.getDouble(LocationDataFactory.ALTITUDE_MSL_KEY), 0.0)

        nmeaEventManager.onDataReceived(GGA_WITHOUT_VALUE)

        verifyCalendarAfter(false) {
            nmeaEventManager.setAltitudeMsl(location)
        }

        assertEquals(0.0, location.extras!!.getDouble(LocationDataFactory.ALTITUDE_MSL_KEY), 0.0)

        nmeaEventManager.onDataReceived(GGA_FIRST_POSITION_FALSE)

        verifyCalendarAfter(false) {
            nmeaEventManager.setAltitudeMsl(location)
        }

        assertEquals(0.0, location.extras!!.getDouble(LocationDataFactory.ALTITUDE_MSL_KEY), 0.0)

        nmeaEventManager.onDataReceived(GGA_SECOND_POSITION_FALSE)

        verifyCalendarAfter(false) {
            nmeaEventManager.setAltitudeMsl(location)
        }

        assertEquals(0.0, location.extras!!.getDouble(LocationDataFactory.ALTITUDE_MSL_KEY), 0.0)

        nmeaEventManager.onDataReceived(GGA_HYPHEN_FALSE)

        verifyCalendarAfter(false) {
            nmeaEventManager.setAltitudeMsl(location)
        }

        assertEquals(0.0, location.extras!!.getDouble(LocationDataFactory.ALTITUDE_MSL_KEY), 0.0)

        nmeaEventManager.onDataReceived(GGA_DOT_FALSE)

        verifyCalendarAfter(false) {
            nmeaEventManager.setAltitudeMsl(location)
        }

        assertEquals(0.0, location.extras!!.getDouble(LocationDataFactory.ALTITUDE_MSL_KEY), 0.0)

        nmeaEventManager.onDataReceived(GGA)

        verifyCalendarAfter(false) {
            nmeaEventManager.setAltitudeMsl(location)
        }

    assertEquals(61.7, location.extras!!.getDouble(LocationDataFactory.ALTITUDE_MSL_KEY), 0.0)

        nmeaEventManager.onDataReceived(GGA_NEGATIVE)

        verifyCalendarAfter(false) {
            nmeaEventManager.setAltitudeMsl(location)
        }

        assertEquals(-61.7, location.extras!!.getDouble(LocationDataFactory.ALTITUDE_MSL_KEY), 0.0)
    }

    @Test
    fun `test onDestroy stops message updates and clean resources`() {
        nmeaEventManager.onDestroy()
    }
}