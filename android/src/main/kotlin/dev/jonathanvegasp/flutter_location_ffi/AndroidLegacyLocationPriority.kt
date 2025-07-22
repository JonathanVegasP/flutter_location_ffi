package dev.jonathanvegasp.flutter_location_ffi

import androidx.core.location.LocationRequestCompat

enum class AndroidLegacyLocationPriority : AndroidLocationPriorityStrategy {
    PRIORITY_REDUCED {
        override val level: Int
            get() = LocationRequestCompat.QUALITY_LOW_POWER
    },
    PRIORITY_PASSIVE {
        override val level: Int
            get() = LocationRequestCompat.QUALITY_LOW_POWER
    },
    PRIORITY_LOW_POWER {
        override val level: Int
            get() = LocationRequestCompat.QUALITY_LOW_POWER
    },
    PRIORITY_BALANCED_POWER {
        override val level: Int
            get() = LocationRequestCompat.QUALITY_BALANCED_POWER_ACCURACY
    },
    PRIORITY_HIGH {
        override val level: Int
            get() = LocationRequestCompat.QUALITY_HIGH_ACCURACY
    },
    PRIORITY_BEST {
        override val level: Int
            get() = LocationRequestCompat.QUALITY_HIGH_ACCURACY
    },
    PRIORITY_BEST_FOR_NAVIGATION {
        override val level: Int
            get() = LocationRequestCompat.QUALITY_HIGH_ACCURACY
    }
}