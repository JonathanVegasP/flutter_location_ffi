package dev.jonathanvegasp.flutter_location_ffi

object LocationDataFactory {
    fun create(
        isGpsEnabled: Boolean,
        latitude: Double,
        longitude: Double,
        accuracy: Float
    ): Array<Any> {
        return arrayOf(isGpsEnabled, latitude, longitude, accuracy)
    }

    fun create(): Array<Any> {
        val geolocation = 0.0
        return arrayOf(false, geolocation, geolocation, 0.0F)
    }
}
