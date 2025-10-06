package dev.jonathanvegasp.flutter_location_ffi.nmea

import android.location.Location
import android.os.Bundle
import dev.jonathanvegasp.flutter_location_ffi.location.LocationDataFactory
import java.util.Calendar

internal abstract class NmeaManager {
    private var message: String? = null
    private var messageTime: Calendar? = null

    companion object {
        private const val FLAG_BUILD_ALTITUDE = 0x01
        private const val FLAG_IS_NEGATIVE = 0x02
        private const val FLAG_HAS_DIGIT = 0x04
        private const val FLAG_LAST_HYPHEN = 0x08
        private const val FLAG_LAST_DOT = 0x10
    }

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
        var decimalDivider = 0.0
        var result = 0.0
        var flags = 0 // 4 bytes da memoria

        for (i in 7 until length) {
            when (val c = message[i]) {
                ',' -> {
                    when (++commaCount) {
                        8 -> flags = flags or FLAG_BUILD_ALTITUDE
                        9 -> break
                    }
                }

                '*' -> break
                else -> {
                    if ((flags and FLAG_BUILD_ALTITUDE) == 0) continue

                    when (c) {
                        '-' -> {
                            if ((flags and FLAG_LAST_HYPHEN) != 0) {
                                flags = flags and FLAG_HAS_DIGIT.inv()
                                break
                            }

                            flags = flags or FLAG_LAST_HYPHEN or FLAG_IS_NEGATIVE
                        }

                        '.' -> {
                            if ((flags and FLAG_LAST_DOT) != 0) {
                                flags = flags and FLAG_HAS_DIGIT.inv()
                                break
                            }

                            flags = flags or FLAG_LAST_DOT

                            decimalDivider = 0.1
                        }

                        in '0'..'9' -> {
                            flags = flags or FLAG_HAS_DIGIT
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

                        else -> {
                            flags = flags and FLAG_HAS_DIGIT.inv()
                            break
                        }
                    }
                }
            }
        }

        if ((flags and FLAG_HAS_DIGIT) == 0) return

        location.extras!!.putDouble(
            LocationDataFactory.ALTITUDE_MSL_KEY, when {
                (flags and FLAG_IS_NEGATIVE) != 0 -> -result
                else -> result
            }
        )
    }

    fun onDataReceived(nmea: String?) {
        if (nmea == null || nmea.length < 6) return

        if (!(nmea[0] == '$' && nmea[3] == 'G' && nmea[4] == 'G' && nmea[5] == 'A')) return

        message = nmea
        messageTime = Calendar.getInstance()
    }

    abstract fun startUpdates()

    abstract fun stopUpdates()

    fun onDestroy() {
        stopUpdates()
        message = null
        messageTime = null
    }
}
