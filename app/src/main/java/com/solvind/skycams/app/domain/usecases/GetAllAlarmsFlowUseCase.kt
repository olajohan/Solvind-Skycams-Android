package com.solvind.skycams.app.domain.usecases

import com.solvind.skycams.app.core.UseCaseFlow
import com.solvind.skycams.app.domain.model.Alarm
import com.solvind.skycams.app.domain.repo.IAlarmRepo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllAlarmsFlowUseCase @Inject constructor(
    private val alarmRepo: IAlarmRepo
) : UseCaseFlow<Flow<List<Alarm>>, UseCaseFlow.None>() {
    override fun run(params: None): Flow<List<Alarm>> = alarmRepo.getAllAlarmsFlow()
}