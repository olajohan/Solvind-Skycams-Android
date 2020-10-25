package com.solvind.skycams.skycam.domain.repo

import com.solvind.skycams.skycam.Result
import com.solvind.skycams.skycam.domain.model.Alarm
import kotlinx.coroutines.flow.Flow

interface IAlarmRepo {

    suspend fun getAlarm(skycamKey: String): Result<Alarm>
    fun getAlarmFlow(skycamKey: String): Flow<Alarm>
    fun getAllAlarmsFlow() : Flow<List<Alarm>>
    suspend fun setAlarm(skycamKey: String, alarmAvailableUntil: Long, isActive: Boolean = false): Result<Unit>

}