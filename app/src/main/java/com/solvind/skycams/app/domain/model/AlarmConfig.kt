package com.solvind.skycams.app.domain.model

import java.time.Instant
import java.util.concurrent.TimeUnit

data class AlarmConfig(
    val skycamKey: String,
    val alarmAvailableUntilEpochSeconds: Long,
    val isActive: Boolean
) {
    fun hasTimedOut() = alarmAvailableUntilEpochSeconds < Instant.now().epochSecond
    fun isActiveAndHasNotTimedOut() = isActive && !hasTimedOut()
    fun timeLeftMilli() = TimeUnit.SECONDS.toMillis(alarmAvailableUntilEpochSeconds - Instant.now().epochSecond)
}