package dev.jonathanvegasp.flutter_location_ffi.settings

import android.location.Location
import android.location.LocationManager
import dev.jonathanvegasp.result_channel.toLong
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AndroidLocationSettingsTest {
    companion object {
        private val DEFAULT_ARRAY: Array<Any?> = arrayOf(
            3,
            10000L,
            200.0,
            0,
            false,
            Long.MAX_VALUE,
            0.0,
            10000L,
            0L,
            0L,
            Int.MAX_VALUE,
            false,
            0,
            false,
            false,
            false,
            true,
            "Background service is running",
            "Background Service is running",
            null
        )
    }

    private fun AndroidLocationSettings.assertSettingsEquals(data: Array<Any?>) {
        val settings = this
        assertEquals(data[1].toLong(), settings.intervalMs)
        assertEquals(data[3] as Int, settings.granularity)
        assertEquals(data[4] as Boolean, settings.waitForAccurateLocation)
        assertEquals(
            data[5].toLong().let { if (it > 0) it else Long.MAX_VALUE },
            settings.durationMs
        )
        assertEquals((data[6] as Double).toFloat(), settings.minUpdateDistanceMeters)
        assertEquals(data[7].toLong(), settings.minUpdateIntervalMs)
        assertEquals(data[8].toLong(), settings.maxUpdateDelayMs)
        assertEquals(data[9].toLong(), settings.maxUpdateAgeMillis)
        assertEquals(
            (data[10] as Int).let { if (it > 0) it else Int.MAX_VALUE },
            settings.maxUpdates
        )
        assertEquals(data[11] as Boolean, settings.showLocationServiceDialogWhenRequested)

        val notificationSettings = settings.androidLocationNotificationSettings
        assertEquals((data[12] as Int) - 1, notificationSettings.getNotificationPriority())
        assertEquals((data[12] as Int) + 2, notificationSettings.getNotificationChannelPriority())
        assertEquals(data[13] as Boolean, notificationSettings.showBadge)
        assertEquals(data[14] as Boolean, notificationSettings.vibrationEnabled)
        assertEquals(data[15] as Boolean, notificationSettings.lightsEnabled)
        assertEquals(data[16] as Boolean, notificationSettings.silent)
        assertEquals(data[17] as String, notificationSettings.title)
        assertEquals(data[18] as String, notificationSettings.message)
        assertEquals(data[19] as String?, notificationSettings.info)
    }

    @Test
    fun `test default creates android location settings`() {
        val settings = AndroidLocationSettings::class.java.getDeclaredMethod("default")
            .invoke(null) as AndroidLocationSettings

        assertFalse(settings.isPassive())

        assertEquals(102, settings.getPriority())
        assertEquals(102, settings.getQuality())

        val location = Location(LocationManager.GPS_PROVIDER)

        assertEquals(0.0F, settings.validate(location))
        location.accuracy = 201F
        assertNull(settings.validate(location))
        location.accuracy = 0F
        assertEquals(0F, settings.validate(location))

        settings.assertSettingsEquals(DEFAULT_ARRAY)
    }

    @Test
    fun `test Companion default creates android location settings`() {
        val settings = AndroidLocationSettings.Companion.default()

        assertFalse(settings.isPassive())

        assertEquals(102, settings.getPriority())
        assertEquals(102, settings.getQuality())

        val location = Location(LocationManager.GPS_PROVIDER)

        assertEquals(0.0F, settings.validate(location))
        location.accuracy = 201F
        assertNull(settings.validate(location))
        location.accuracy = 0F
        assertEquals(0F, settings.validate(location))

        settings.assertSettingsEquals(DEFAULT_ARRAY)
    }

    @Test
    fun `test create creates custom android location settings`() {
        var passive = DEFAULT_ARRAY.copyOf()
        passive[0] = 0
        passive[12] = 1
        var settings =
            AndroidLocationSettings::class.java.getDeclaredMethod("create", Array<Any?>::class.java)
                .invoke(null, passive) as AndroidLocationSettings
        assertTrue(settings.isPassive())
        assertEquals(105, settings.getPriority())
        assertEquals(104, settings.getQuality())
        settings.assertSettingsEquals(passive)

        passive = DEFAULT_ARRAY.copyOf()
        passive[0] = 1
        passive[12] = 2
        settings = AndroidLocationSettings.create(passive)
        assertTrue(settings.isPassive())
        assertEquals(105, settings.getPriority())
        assertEquals(104, settings.getQuality())
        settings.assertSettingsEquals(passive)

        passive = DEFAULT_ARRAY.copyOf()
        passive[0] = 2
        passive[12] = 3
        settings = AndroidLocationSettings.create(passive)
        assertFalse(settings.isPassive())
        assertEquals(104, settings.getPriority())
        assertEquals(104, settings.getQuality())
        settings.assertSettingsEquals(passive)

        passive = DEFAULT_ARRAY.copyOf()
        passive[0] = 4
        passive[12] = 3
        settings = AndroidLocationSettings.create(passive)
        assertFalse(settings.isPassive())
        assertEquals(100, settings.getPriority())
        assertEquals(100, settings.getQuality())
        settings.assertSettingsEquals(passive)

        passive = DEFAULT_ARRAY.copyOf()
        passive[0] = 5
        passive[12] = 3
        settings = AndroidLocationSettings.create(passive)
        assertFalse(settings.isPassive())
        assertEquals(100, settings.getPriority())
        assertEquals(100, settings.getQuality())
        settings.assertSettingsEquals(passive)

        passive = DEFAULT_ARRAY.copyOf()
        passive[0] = 6
        passive[12] = 3
        passive[5] = 0L
        passive[10] = 0
        settings = AndroidLocationSettings.create(passive)
        assertFalse(settings.isPassive())
        assertEquals(100, settings.getPriority())
        assertEquals(100, settings.getQuality())
        settings.assertSettingsEquals(passive)

        passive = DEFAULT_ARRAY.copyOf()
        passive[0] = 7
        passive[12] = 4
        settings = AndroidLocationSettings.create(passive)
        assertFalse(settings.isPassive())
        assertThrows(
            "Priority must be between 0 and 6: 7",
            IllegalArgumentException::class.java
        ) { settings.getPriority() }
        assertThrows(
            "Priority must be between 0 and 6: 7",
            IllegalArgumentException::class.java
        ) { settings.getQuality() }
        assertThrows(
            "Priority must be between 0 and 3: 4",
            IllegalArgumentException::class.java
        ) { settings.androidLocationNotificationSettings.getNotificationPriority() }
        assertThrows(
            "Priority must be between 0 and 3: 4",
            IllegalArgumentException::class.java
        ) { settings.androidLocationNotificationSettings.getNotificationChannelPriority() }
    }
}