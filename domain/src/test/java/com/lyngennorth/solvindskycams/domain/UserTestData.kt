package com.lyngennorth.solvindskycams.domain

import com.lyngennorth.solvindskycams.domain.domainmodel.User
import com.lyngennorth.solvindskycams.domain.usecases.UpdateAlarmAvailableUntilEpochSeconds
import java.time.Instant
import java.util.concurrent.TimeUnit
import kotlin.random.Random

fun getUser(
    uid: String = "3eLt8xSOD5ZVs4MmYQpt06cqyyF2",
    followingSkycamKeys: Array<String> = arrayOf("lyngennorth", "alta"),
    alarmAvailableUntilEpochSeconds: Long = 0L
): User = User(
    uid=uid,
    followingSkycamKeys = followingSkycamKeys,
    alarmAvailableUntilEpochSeconds = alarmAvailableUntilEpochSeconds
)