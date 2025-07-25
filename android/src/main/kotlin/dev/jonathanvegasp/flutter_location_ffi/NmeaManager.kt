package dev.jonathanvegasp.flutter_location_ffi

import android.location.Location
import android.os.Bundle
import java.util.Calendar

abstract class NmeaManager : NmeaDataReceiver, Destroyable {
    private var message: String? = null
    private var messageTime: Calendar? = null

    abstract fun getCurrent(nmeaDataReceiver: NmeaDataReceiver)

    fun setAltitudeMsl(location: Location) {
        if (location.extras == null) {
            location.extras = Bundle.EMPTY
        }

        val message = message ?: return
        val now = Calendar.getInstance()

        now.add(Calendar.SECOND, -5)

        if (now.after(messageTime!!)) return

        val length = message.length
        var commaCount = 0
        var buildAltitude = false
        var isNegative = false
        var decimalDivider = 0.0
        var hasDigit = false
        var result = 0.0

        for (i in 7 until length) {
            when (val c = message[i]) {
                ',' -> {
                    when (++commaCount) {
                        8 -> buildAltitude = true
                        9 -> break
                    }
                }

                '*' -> break
                else -> {
                    if (!buildAltitude) continue

                    when (c) {
                        '-' -> isNegative = true
                        '.' -> decimalDivider = 0.1
                        in '0'..'9' -> {
                            hasDigit = true
                            val digit = c - '0'
                            when (decimalDivider) {
                                0.0 -> {
                                    result = result * 10 + digit
                                }

                                else -> {
                                    result += digit * decimalDivider
                                    decimalDivider *= 0.1
                                }
                            }
                        }
                    }
                }
            }
        }

        if (!hasDigit) return

        location.extras!!.putDouble(LocationDataFactory.altitudeMslKey, if (isNegative) -result else result)
    }

    override fun onDataReceived(nmea: String, calendar: Calendar) {
        message = nmea
        messageTime = calendar
    }

    abstract fun startUpdates()

    abstract fun stopUpdates()

    override fun onDestroy() {
        stopUpdates()
        message = null
        messageTime = null
    }
}
