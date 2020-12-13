package com.solvind.skycams.app.domain.model

import com.solvind.skycams.app.core.DEFAULT_ALARM_THRESHOLD
import java.time.Instant
import java.util.concurrent.TimeUnit

data class AlarmConfig(
    val skycamKey: String,
    val alarmAvailableUntilEpochSeconds: Long,
    val isActive: Boolean,
    val threshold: Int = DEFAULT_ALARM_THRESHOLD
) {
    fun hasTimedOut() = alarmAvailableUntilEpochSeconds < Instant.now().epochSecond
    fun isActiveAndHasNotTimedOut() = isActive && !hasTimedOut()
    fun timeLeftMilli() = TimeUnit.SECONDS.toMillis(alarmAvailableUntilEpochSeconds - Instant.now().epochSecond)
}