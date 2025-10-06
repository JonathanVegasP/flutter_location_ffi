package dev.jonathanvegasp.flutter_location_ffi.utils

import org.mockito.MockedStatic
import org.mockito.Mockito.mockStatic

internal inline fun <reified T> mockStaticClass(block: (MockedStatic<T>) -> Unit) {
    mockStatic(T::class.java).use(block)
}
