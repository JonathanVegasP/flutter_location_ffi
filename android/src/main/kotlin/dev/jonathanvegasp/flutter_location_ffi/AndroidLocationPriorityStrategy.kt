package dev.jonathanvegasp.flutter_location_ffi

interface AndroidLocationPriorityStrategy {
    val level: Int
    val isHighPriority: Boolean
}
