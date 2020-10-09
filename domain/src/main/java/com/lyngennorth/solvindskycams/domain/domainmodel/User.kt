package com.lyngennorth.solvindskycams.domain.domainmodel

data class User(
    val uid: String,
    val followingSkycamKeys: Array<String>,
    val alarmAvailableUntilEpochSeconds: Long
)