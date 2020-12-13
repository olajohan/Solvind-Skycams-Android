package com.solvind.skycams.app.domain.usecases.alarm

import com.solvind.skycams.app.core.UseCaseFlow
import com.solvind.skycams.app.domain.model.AlarmConfig
import com.solvind.skycams.app.domain.repo.IAlarmConfigRepo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

open class GetAlarmConfigFlowUseCase @Inject constructor(
    private val alarmConfigRepo: IAlarmConfigRepo
) : UseCaseFlow<Flow<AlarmConfig>, GetAlarmConfigFlowUseCase.Params>() {
    data class Params(val skycamKey: String)

    override fun run(params: Params): Flow<AlarmConfig> = alarmConfigRepo.getAlarmConfigFlow(params.skycamKey)


}