package com.solvind.skycams.app.domain.usecases

import com.solvind.skycams.app.core.Failure
import com.solvind.skycams.app.core.Resource
import com.solvind.skycams.app.core.UseCase
import com.solvind.skycams.app.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class DeactivateAllAlarmsUseCase @Inject constructor(
    private val getAllAlarmsUseCase: GetAllAlarmsUseCase,
    private val deactivateAlarmUseCase: DeactivateAlarmUseCase,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : UseCase<Unit, UseCase.None>(dispatcher) {

    override suspend fun run(params: None): Resource<Unit> {
        val userAlarms = getAllAlarmsUseCase.run(None())

        if (userAlarms is Resource.Success) {
            val deactivationResultList = userAlarms.value.mapTo(mutableListOf()) {
                deactivateAlarmUseCase.run(DeactivateAlarmUseCase.Params(it.skycamKey))
            }
            return if (deactivationResultList.any { it is Resource.Error }) Resource.Error(Failure.OneOrMoreAlarmsFailedToDeactivateFailure)
            else Resource.Success(Unit)
        } else if (userAlarms is Resource.Error) return Resource.Error(userAlarms.failure)
        return Resource.Error(Failure.DeactivateAllAlarmsUnknownFailure)
    }
}