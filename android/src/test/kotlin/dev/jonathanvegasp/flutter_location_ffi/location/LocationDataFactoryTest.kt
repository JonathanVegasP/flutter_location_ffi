package dev.jonathanvegasp.flutter_location_ffi.location

import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class LocationDataFactoryTest {
    @Test
    fun `test create without args creates default location`() {
        val size = LocationDataFactory.create().size

        assertEquals(13, size)
    }

    @Test
    @Config(
        sdk = [21]
    )
    fun `test create with args creates custom location`() {
        val location = Location(LocationManager.GPS_PROVIDER)

        location.extras = Bundle.EMPTY

        var size = LocationDataFactory.create(0F, location).size

        assertEquals(13, size)

        location.time = System.currentTimeMillis()

        location.altitude = 0.0

        location.bearing = 0F

        location.speed = 0F

        size = LocationDataFactory.create(0F, location).size

        assertEquals(13, size)
    }

    @Test
    @Config(
        sdk = [34]
    )
    fun `test create with args creates custom location for newer versions`() {
        val location = Location(LocationManager.GPS_PROVIDER)

        location.extras = Bundle.EMPTY

        var size = LocationDataFactory.create(0F, location).size

        assertEquals(13, size)

        location.verticalAccuracyMeters = 0F

        location.bearingAccuracyDegrees = 0F

        location.speedAccuracyMetersPerSecond = 0F

        location.mslAltitudeMeters = 0.0

        size = LocationDataFactory.create(0F, location).size

        assertEquals(13, size)
    }
}