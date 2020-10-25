package com.solvind.skycams.skycam.domain.model

data class Alarm(
    val skycamKey: String,
    val alarmAvailableUntilEpochSeconds: Long,
    val isActive: Boolean
)