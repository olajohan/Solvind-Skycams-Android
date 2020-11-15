package com.solvind.skycams.app.domain.usecases

import com.solvind.skycams.app.core.UseCaseFlow
import com.solvind.skycams.app.domain.model.AlarmConfig
import com.solvind.skycams.app.domain.repo.IAlarmConfigRepo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

open class GetAllAlarmsFlowUseCase @Inject constructor(
    private val alarmConfigRepo: IAlarmConfigRepo
) : UseCaseFlow<Flow<List<AlarmConfig>>, UseCaseFlow.None>() {
    override fun run(params: None): Flow<List<AlarmConfig>> = alarmConfigRepo.getAllAlarmConfigFlows()
}