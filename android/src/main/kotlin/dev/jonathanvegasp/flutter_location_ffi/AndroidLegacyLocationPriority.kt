package dev.jonathanvegasp.flutter_location_ffi

import androidx.core.location.LocationRequestCompat

enum class AndroidLegacyLocationPriority : AndroidLocationPriorityStrategy {
    PRIORITY_REDUCED {
        override val level: Int
            get() = LocationRequestCompat.QUALITY_LOW_POWER
        override val isHighPriority: Boolean
            get() = false
    },
    PRIORITY_PASSIVE {
        override val level: Int
            get() = LocationRequestCompat.QUALITY_LOW_POWER
        override val isHighPriority: Boolean
            get() = false
    },
    PRIORITY_LOW_POWER {
        override val level: Int
            get() = LocationRequestCompat.QUALITY_LOW_POWER
        override val isHighPriority: Boolean
            get() = false
    },
    PRIORITY_BALANCED_POWER {
        override val level: Int
            get() = LocationRequestCompat.QUALITY_BALANCED_POWER_ACCURACY
        override val isHighPriority: Boolean
            get() = false
    },
    PRIORITY_HIGH {
        override val level: Int
            get() = LocationRequestCompat.QUALITY_HIGH_ACCURACY
        override val isHighPriority: Boolean
            get() = true
    },
    PRIORITY_BEST {
        override val level: Int
            get() = LocationRequestCompat.QUALITY_HIGH_ACCURACY
        override val isHighPriority: Boolean
            get() = true
    },
    PRIORITY_BEST_FOR_NAVIGATION {
        override val level: Int
            get() = LocationRequestCompat.QUALITY_HIGH_ACCURACY
        override val isHighPriority: Boolean
            get() = true
    }
}