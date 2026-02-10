package com.englesoft.netspeedindicator.domain.model

/**
 * Domain model representing current internet speed
 * Pure Kotlin - no Android dependencies
 */
data class SpeedModel(
    val downloadBytesPerSecond: Long = 0L,
    val uploadBytesPerSecond: Long = 0L,
    val timestamp: Long = System.currentTimeMillis()
)
