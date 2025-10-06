package dev.jonathanvegasp.flutter_location_ffi.location

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ServiceController
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class LocationServiceTest {
    private lateinit var locationService: ServiceController<LocationService>

    @Before
    fun setUp() {
        locationService = Robolectric.buildService(LocationService::class.java)
    }

    @Test
    @Config(
        sdk = [21]
    )
    fun `test onCreate create resources for old sdk`() {
        locationService.create()
    }

    @Test
    @Config(
        sdk = [34]
    )
    fun `test onCreate create resources for new sdk`() {
        locationService.create()
    }

    @Test
    fun `test onBind binds service`() {
        val service = locationService.create().get()
        val binder = service.onBind(null) as LocationService.LocationServiceBinder

        assertEquals(service, binder.getService())
    }
}