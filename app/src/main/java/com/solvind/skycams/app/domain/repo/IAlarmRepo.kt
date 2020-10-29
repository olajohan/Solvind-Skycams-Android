package com.solvind.skycams.app.domain.repo

import com.solvind.skycams.app.core.Resource
import com.solvind.skycams.app.domain.model.Alarm
import kotlinx.coroutines.flow.Flow

interface IAlarmRepo {

    suspend fun getAlarm(skycamKey: String): Resource<Alarm>
    suspend fun getAllAlarms() : Resource<List<Alarm>>
    fun getAlarmFlow(skycamKey: String): Flow<Alarm>
    fun getAllAlarmsFlow() : Flow<List<Alarm>>
    suspend fun setAlarm(skycamKey: String, alarmAvailableUntil: Long, isActive: Boolean = false): Resource<Unit>

}