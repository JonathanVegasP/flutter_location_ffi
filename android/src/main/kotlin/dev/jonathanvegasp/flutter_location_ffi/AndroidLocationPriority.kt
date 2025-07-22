package dev.jonathanvegasp.flutter_location_ffi

import com.google.android.gms.location.Priority

enum class AndroidLocationPriority: AndroidLocationPriorityStrategy {
    PRIORITY_REDUCED {
        override val level: Int
            get() = Priority.PRIORITY_PASSIVE
    },
    PRIORITY_PASSIVE {
        override val level: Int
            get() = Priority.PRIORITY_PASSIVE
    },
    PRIORITY_LOW_POWER {
        override val level: Int
            get() = Priority.PRIORITY_LOW_POWER
    },
    PRIORITY_BALANCED_POWER {
        override val level: Int
            get() = Priority.PRIORITY_BALANCED_POWER_ACCURACY
    },
    PRIORITY_HIGH {
        override val level: Int
            get() = Priority.PRIORITY_HIGH_ACCURACY
    },
    PRIORITY_BEST {
        override val level: Int
            get() = Priority.PRIORITY_HIGH_ACCURACY
    },
    PRIORITY_BEST_FOR_NAVIGATION {
        override val level: Int
            get() = Priority.PRIORITY_HIGH_ACCURACY
    }
}
