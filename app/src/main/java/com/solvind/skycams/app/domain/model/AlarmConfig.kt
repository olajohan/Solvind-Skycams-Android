package com.solvind.skycams.app.domain.model

import java.time.Instant

data class AlarmConfig(
    val skycamKey: String,
    val alarmAvailableUntilEpochSeconds: Long,
    val isActive: Boolean
) {
    fun isActiveAndNotTimeout() = isActive && alarmAvailableUntilEpochSeconds > Instant.now().epochSecond
    fun hasTimedoutAndIsStillActive() = isActive && alarmAvailableUntilEpochSeconds < Instant.now().epochSecond
    fun hasTimedoutAndIsNotActive() = !isActive && alarmAvailableUntilEpochSeconds < Instant.now().epochSecond
}