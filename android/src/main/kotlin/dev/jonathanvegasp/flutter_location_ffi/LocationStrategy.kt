package dev.jonathanvegasp.flutter_location_ffi

import dev.jonathanvegasp.result_channel.ResultChannel

interface LocationStrategy : Destroyable {
    fun setSettings(settings: AndroidLocationSettings)

    fun getCurrent(result: ResultChannel)

    fun startUpdates(result: ResultChannel)

    fun stopUpdates()
}
