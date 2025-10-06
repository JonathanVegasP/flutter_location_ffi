package dev.jonathanvegasp.flutter_location_ffi.permission.location

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dev.jonathanvegasp.flutter_location_ffi.permission.PermissionStatus
import dev.jonathanvegasp.flutter_location_ffi.utils.mockStaticClass
import dev.jonathanvegasp.result_channel.ResultChannel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LocationPermissionManagerTest {
    @Mock
    private lateinit var activity: Activity

    @Mock
    private lateinit var resultChannel: ResultChannel

    private lateinit var locationPermissionManager: LocationPermissionManager

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        `when`(activity.packageName).thenReturn("app")
        locationPermissionManager = LocationPermissionManager(activity)
    }

    private inline fun getIntent(block: () -> Unit): Intent {
        val argumentCaptor = ArgumentCaptor.forClass(Intent::class.java)

        block()

        verify(activity, atLeastOnce()).startActivity(argumentCaptor.capture(), any())

        return argumentCaptor.value
    }

    private fun hasNoPermission() {
        `when`(
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ).thenReturn(-1)

        `when`(
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ).thenReturn(-1)
    }

    private inline fun requestPermission(block: () -> Unit) {
        mockStaticClass<ActivityCompat> {
            block()

            it.verify {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ),
                    1001
                )
            }
        }
    }

    @Test
    fun `test checkPermission checks android location permission`() {
        hasNoPermission()

        assertEquals(PermissionStatus.DENIED, locationPermissionManager.checkPermission())

        `when`(
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ).thenReturn(0)

        assertEquals(PermissionStatus.GRANTED, locationPermissionManager.checkPermission())
    }

    @Test
    fun `test checkAndRequestPermission checks and request android location permission`() {
        `when`(
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ).thenReturn(-1)

        `when`(
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ).thenReturn(0)

        locationPermissionManager.checkAndRequestPermission(resultChannel)

        verify(resultChannel).success(PermissionStatus.GRANTED)

        hasNoPermission()

        requestPermission {
            locationPermissionManager.checkAndRequestPermission(resultChannel)
        }

        requestPermission {
            locationPermissionManager.checkAndRequestPermission(resultChannel)
        }

        verify(resultChannel).failure(null)
    }

    @Test
    fun `test openAppSettings opens application settings`() {
        val intent = getIntent {
            locationPermissionManager.openAppSettings()
        }

        assertEquals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, intent.action)
    }

    @Test
    fun `test openAppSettings opens location settings`() {
        val intent = getIntent {
            locationPermissionManager.openPermissionSettings()
        }

        assertEquals(Settings.ACTION_LOCATION_SOURCE_SETTINGS, intent.action)
    }

    @Test
    fun `test onRequestPermissionResult checks permission result`() {
        assertFalse(
            locationPermissionManager.onRequestPermissionsResult(
                0,
                arrayOf(),
                intArrayOf()
            )
        )

        assertFalse(
            locationPermissionManager.onRequestPermissionsResult(
                1001,
                arrayOf(),
                intArrayOf()
            )
        )

        hasNoPermission()

        locationPermissionManager.checkAndRequestPermission(resultChannel)

        assertFalse(
            locationPermissionManager.onRequestPermissionsResult(
                1001,
                arrayOf(""),
                intArrayOf()
            )
        )

        assertTrue(
            locationPermissionManager.onRequestPermissionsResult(
                1001,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                intArrayOf(0)
            )
        )

        verify(resultChannel).success(PermissionStatus.GRANTED)

        locationPermissionManager.checkAndRequestPermission(resultChannel)

        assertFalse(
            locationPermissionManager.onRequestPermissionsResult(
                1001,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, ""),
                intArrayOf(-1, -1)
            )
        )

        assertTrue(
            locationPermissionManager.onRequestPermissionsResult(
                1001,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                intArrayOf(-1, 0)
            )
        )

        verify(resultChannel, times(2)).success(PermissionStatus.GRANTED)

        locationPermissionManager.checkAndRequestPermission(resultChannel)

        assertTrue(
            locationPermissionManager.onRequestPermissionsResult(
                1001,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                intArrayOf(-1, -1)
            )
        )

        verify(resultChannel).success(PermissionStatus.NEVER_ASK_AGAIN)

        locationPermissionManager.checkAndRequestPermission(resultChannel)

        mockStaticClass<ActivityCompat> {
            it.`when`<Any> {
                ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            }.thenReturn(true)

            assertTrue(
                locationPermissionManager.onRequestPermissionsResult(
                    1001,
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ),
                    intArrayOf(-1, -1)
                )
            )

            verify(resultChannel).success(PermissionStatus.DENIED)

            locationPermissionManager.checkAndRequestPermission(resultChannel)

            it.`when`<Any> {
                ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            }.thenReturn(false)

            it.`when`<Any> {
                ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            }.thenReturn(true)

            assertTrue(
                locationPermissionManager.onRequestPermissionsResult(
                    1001,
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ),
                    intArrayOf(-1, -1)
                )
            )
        }

        verify(resultChannel, times(2)).success(PermissionStatus.DENIED)
    }

    @Test
    fun `test onDestroy releases resources`() {
        hasNoPermission()

        locationPermissionManager.onDestroy()

        verifyNoInteractions(resultChannel)

        locationPermissionManager.checkAndRequestPermission(resultChannel)

        locationPermissionManager.onDestroy()

        verify(resultChannel).failure(null)
    }
}