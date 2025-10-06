package dev.jonathanvegasp.flutter_location_ffi.location

import dev.jonathanvegasp.flutter_location_ffi.settings.AndroidLocationSettings
import dev.jonathanvegasp.result_channel.ResultChannel

internal interface LocationStrategy {
    val isListening: Boolean

    fun setSettings(settings: AndroidLocationSettings)

    fun isServiceEnabled(channel: ResultChannel)

    fun getCurrent(result: ResultChannel)

    fun startUpdates(result: ResultChannel)

    fun stopUpdates()

    fun onDestroy()
}
