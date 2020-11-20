package com.solvind.skycams.app.domain.repo

import com.solvind.skycams.app.core.Resource
import com.solvind.skycams.app.domain.model.AlarmConfig
import kotlinx.coroutines.flow.Flow

interface IAlarmConfigRepo {

    suspend fun getAlarmConfig(skycamKey: String): Resource<AlarmConfig>
    suspend fun getAllAlarmConfigs() : Resource<List<AlarmConfig>>
    fun getAlarmConfigFlow(skycamKey: String): Flow<AlarmConfig>
    fun getAllAlarmConfigFlows() : Flow<AlarmConfig>
    suspend fun setAlarmConfig(skycamKey: String, alarmAvailableUntil: Long, isActive: Boolean): Resource<kotlin.Unit>

}