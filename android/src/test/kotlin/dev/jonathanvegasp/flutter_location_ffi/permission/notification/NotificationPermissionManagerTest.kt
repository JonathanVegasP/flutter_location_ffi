package dev.jonathanvegasp.flutter_location_ffi.permission.notification

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.app.ActivityCompat
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
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(
    sdk = [33]
)
class NotificationPermissionManagerTest {
    @Mock
    private lateinit var activity: Activity

    @Mock
    private lateinit var resultChannel: ResultChannel

    private lateinit var notificationPermissionManager: NotificationPermissionManager

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        notificationPermissionManager = NotificationPermissionManager(activity)
    }

    private inline fun getIntent(block: () -> Unit): Intent {
        val argumentCaptor = ArgumentCaptor.forClass(Intent::class.java)

        `when`(activity.packageName).thenReturn("app")

        block()

        verify(activity, atLeastOnce()).startActivity(argumentCaptor.capture(), any())

        return argumentCaptor.value
    }

    private inline fun setPermission(result: Boolean, block: () -> Unit) {
        `when`(
            activity.checkPermission(eq(Manifest.permission.POST_NOTIFICATIONS), anyInt(), anyInt())
        ).thenReturn(if (result) PackageManager.PERMISSION_GRANTED else PackageManager.PERMISSION_DENIED)

        mockStaticClass<ActivityCompat> {
            it.`when`<Any> {
                ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }.thenReturn(result)

            block()
        }
    }

    @Test
    @Config(
        sdk = [21]
    )
    fun `test checkPermission always true when sdk is less than 33`() {
        assertEquals(PermissionStatus.GRANTED, notificationPermissionManager.checkPermission())
    }

    @Test
    fun `test checkPermission checks android notification permission when sdk is 33 or higher`() {

        setPermission(true) {
            assertEquals(PermissionStatus.GRANTED, notificationPermissionManager.checkPermission())
        }

        setPermission(false) {
            assertEquals(PermissionStatus.DENIED, notificationPermissionManager.checkPermission())
        }
    }

    @Test
    fun `test checkAndRequestPermission checks and request android notification permission`() {
        setPermission(true) {
            notificationPermissionManager.checkAndRequestPermission(resultChannel)
        }

        verify(resultChannel).success(PermissionStatus.GRANTED)

        setPermission(false) {
            notificationPermissionManager.checkAndRequestPermission(resultChannel)
        }

        verifyNoMoreInteractions(resultChannel)

        setPermission(false) {
            notificationPermissionManager.checkAndRequestPermission(resultChannel)
        }

        verify(resultChannel).failure(null)
    }

    @Test
    @Config(
        sdk = [21]
    )
    fun `test openPermissionSettings opens app settings when sdk is less than 33`() {
        val intent = getIntent {
            notificationPermissionManager.openPermissionSettings()
        }

        assertEquals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, intent.action)
    }

    @Test
    fun `test openPermissionSettings opens notification settings when sdk is 33 or higher`() {
        val intent = getIntent {
            notificationPermissionManager.openPermissionSettings()
        }

        assertEquals(Settings.ACTION_APP_NOTIFICATION_SETTINGS, intent.action)
    }

    @Test
    fun `test onRequestPermissionResult checks permission result`() {
        assertFalse(
            notificationPermissionManager.onRequestPermissionsResult(
                0,
                arrayOf(),
                intArrayOf()
            )
        )

        assertFalse(
            notificationPermissionManager.onRequestPermissionsResult(
                1003,
                arrayOf(),
                intArrayOf()
            )
        )

        setPermission(false) {
            notificationPermissionManager.checkAndRequestPermission(resultChannel)
        }

        assertFalse(
            notificationPermissionManager.onRequestPermissionsResult(
                1003,
                arrayOf(""),
                intArrayOf()
            )
        )

        assertTrue(
            notificationPermissionManager.onRequestPermissionsResult(
                1003,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                intArrayOf(0)
            )
        )

        verify(resultChannel).success(PermissionStatus.GRANTED)

        notificationPermissionManager.checkAndRequestPermission(resultChannel)

        setPermission(false) {
            assertTrue(
                notificationPermissionManager.onRequestPermissionsResult(
                    1003,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    intArrayOf(-1)
                )
            )
        }

        verify(resultChannel).success(PermissionStatus.NEVER_ASK_AGAIN)

        notificationPermissionManager.checkAndRequestPermission(resultChannel)

        setPermission(true) {
            assertTrue(
                notificationPermissionManager.onRequestPermissionsResult(
                    1003,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    intArrayOf(-1)
                )
            )
        }

        verify(resultChannel).success(PermissionStatus.DENIED)
    }

    @Test
    fun `test onDestroy releases resources`() {
        setPermission(false) {
            notificationPermissionManager.onDestroy()
        }

        verifyNoInteractions(resultChannel)

        notificationPermissionManager.checkAndRequestPermission(resultChannel)

        notificationPermissionManager.onDestroy()

        verify(resultChannel).failure(null)
    }
}