package dev.jonathanvegasp.flutter_location_ffi

import dev.jonathanvegasp.result_channel.ResultChannel

interface LocationStrategy : Destroyable {
    companion object {
        const val ON_START_ERROR = "Cannot start a new update while another is already in progress."
    }

    fun getCurrent(result: ResultChannel)

    fun startUpdates(result: ResultChannel)

    fun stopUpdates()
}
