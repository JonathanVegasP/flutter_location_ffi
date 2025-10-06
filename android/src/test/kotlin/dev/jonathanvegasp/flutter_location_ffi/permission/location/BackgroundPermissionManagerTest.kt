package dev.jonathanvegasp.flutter_location_ffi.permission.location

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
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
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(
    sdk = [29]
)
class BackgroundPermissionManagerTest {
    @Mock
    private lateinit var activity: Activity

    @Mock
    private lateinit var resultChannel: ResultChannel

    private lateinit var backgroundPermissionManager: BackgroundPermissionManager

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        backgroundPermissionManager = BackgroundPermissionManager(activity)
    }

    private inline fun setPermission(result: Boolean, block: () -> Unit) {
        `when`(
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        ).thenReturn(if (result) PackageManager.PERMISSION_GRANTED else PackageManager.PERMISSION_DENIED)
        mockStaticClass<ActivityCompat> {
            it.`when`<Any> {
                ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            }.thenReturn(result)

            block()
        }
    }

    @Test
    @Config(
        sdk = [21]
    )
    fun `test checkPermission checks true when sdk is less than 29`() {
        assertEquals(PermissionStatus.GRANTED, backgroundPermissionManager.checkPermission())
    }

    @Test
    fun `test checkPermission checks background permission if sdk is 29 or higher`() {
        setPermission(true) {
            assertEquals(PermissionStatus.GRANTED, backgroundPermissionManager.checkPermission())
        }

        setPermission(false) {
            assertEquals(PermissionStatus.DENIED, backgroundPermissionManager.checkPermission())
        }
    }

    @Test
    fun `test checkAndRequestPermission checks and request android background location permission`() {
        setPermission(true) {
            backgroundPermissionManager.checkAndRequestPermission(resultChannel)
            verify(resultChannel).success(PermissionStatus.GRANTED)
        }

        setPermission(false) {
            backgroundPermissionManager.checkAndRequestPermission(resultChannel)
            backgroundPermissionManager.checkAndRequestPermission(resultChannel)
            verify(resultChannel).failure(null)
        }
    }

    @Test
    fun `test onRequestPermissionResult checks permission result`() {
        assertFalse(
            backgroundPermissionManager.onRequestPermissionsResult(
                0,
                arrayOf(),
                intArrayOf()
            )
        )

        assertFalse(
            backgroundPermissionManager.onRequestPermissionsResult(
                1002,
                arrayOf(),
                intArrayOf()
            )
        )

        setPermission(false) {
            backgroundPermissionManager.checkAndRequestPermission(resultChannel)
        }

        assertFalse(
            backgroundPermissionManager.onRequestPermissionsResult(
                1002,
                arrayOf(""),
                intArrayOf()
            )
        )

        assertTrue(
            backgroundPermissionManager.onRequestPermissionsResult(
                1002,
                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                intArrayOf(0)
            )
        )

        verify(resultChannel).success(PermissionStatus.GRANTED)

        backgroundPermissionManager.checkAndRequestPermission(resultChannel)

        setPermission(false) {
            assertTrue(
                backgroundPermissionManager.onRequestPermissionsResult(
                    1002,
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    intArrayOf(-1)
                )
            )
        }

        verify(resultChannel).success(PermissionStatus.NEVER_ASK_AGAIN)

        backgroundPermissionManager.checkAndRequestPermission(resultChannel)

        setPermission(true) {
            assertTrue(
                backgroundPermissionManager.onRequestPermissionsResult(
                    1002,
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    intArrayOf(-1)
                )
            )
        }

        verify(resultChannel).success(PermissionStatus.DENIED)
    }

    @Test
    fun `test onDestroy releases resources`() {
        setPermission(false) {
            backgroundPermissionManager.onDestroy()
        }

        verifyNoInteractions(resultChannel)

        backgroundPermissionManager.checkAndRequestPermission(resultChannel)

        backgroundPermissionManager.onDestroy()

        verify(resultChannel).failure(null)
    }
}