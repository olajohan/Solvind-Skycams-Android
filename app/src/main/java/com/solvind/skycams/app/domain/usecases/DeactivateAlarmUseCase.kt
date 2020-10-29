package com.solvind.skycams.app.domain.usecases

import com.solvind.skycams.app.core.Failure
import com.solvind.skycams.app.core.Resource
import com.solvind.skycams.app.core.UseCase
import com.solvind.skycams.app.di.IoDispatcher
import com.solvind.skycams.app.domain.repo.IAlarmRepo
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

open class DeactivateAlarmUseCase @Inject constructor(
    private val repo: IAlarmRepo,
    private val getAlarmUseCase: GetAlarmUseCase,
    @IoDispatcher dispatcher: CoroutineDispatcher
) : UseCase<Unit, DeactivateAlarmUseCase.Params>(dispatcher) {

    data class Params(
        val skycamKey: String
    )

    override suspend fun run(params: Params): Resource<Unit> {
        if (params.skycamKey.isEmpty()) return Resource.Error(Failure.EmptySkycamKeyFailure)
        return when(val currentAlarm = getAlarmUseCase.run(GetAlarmUseCase.Params(params.skycamKey))) {
            is Resource.Error -> Resource.Error(currentAlarm.failure)
            is Resource.Success -> repo.setAlarm(params.skycamKey, currentAlarm.value.alarmAvailableUntilEpochSeconds, false)
        }
    }

}