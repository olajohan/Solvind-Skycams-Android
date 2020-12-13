package com.solvind.skycams.app.domain.usecases.alarm

import com.solvind.skycams.app.core.Resource
import com.solvind.skycams.app.core.UseCase
import com.solvind.skycams.app.domain.model.AlarmConfig
import com.solvind.skycams.app.domain.repo.IAlarmConfigRepo
import javax.inject.Inject

class GetAllAlarmsUseCase @Inject constructor(
    private val alarmConfigRepo: IAlarmConfigRepo
) : UseCase<List<AlarmConfig>, UseCase.None>() {

    override suspend fun run(params: None): Resource<List<AlarmConfig>> = alarmConfigRepo.getAllAlarmConfigs()

}