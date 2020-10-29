package com.solvind.skycams.app.domain.usecases

import com.solvind.skycams.app.core.UseCaseFlow
import com.solvind.skycams.app.domain.model.Alarm
import com.solvind.skycams.app.domain.repo.IAlarmRepo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAlarmFlowUseCase @Inject constructor(
    private val alarmRepo: IAlarmRepo
) : UseCaseFlow<Flow<Alarm>, GetAlarmFlowUseCase.Params>() {
    data class Params(val skycamKey: String)

    override fun run(params: Params): Flow<Alarm> = alarmRepo.getAlarmFlow(params.skycamKey)


}