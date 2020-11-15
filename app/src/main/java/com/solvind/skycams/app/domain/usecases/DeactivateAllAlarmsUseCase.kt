package com.solvind.skycams.app.domain.usecases

import com.solvind.skycams.app.core.Failure
import com.solvind.skycams.app.core.Resource
import com.solvind.skycams.app.core.UseCase
import com.solvind.skycams.app.di.IoDispatcher
import com.solvind.skycams.app.di.MainDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class DeactivateAllAlarmsUseCase @Inject constructor(
    private val getAllAlarmsUseCase: GetAllAlarmsUseCase,
    private val deactivateAlarmUseCase: DeactivateAlarmConfigUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher
) : UseCase<Unit, UseCase.None>(ioDispatcher, mainDispatcher) {

    override suspend fun run(params: None): Resource<Unit> {
        val userAlarms = getAllAlarmsUseCase.run(None())

        if (userAlarms is Resource.Success) {
            val deactivationResultList = userAlarms.value.mapTo(mutableListOf()) {
                deactivateAlarmUseCase.run(DeactivateAlarmConfigUseCase.Params(it.skycamKey))
            }
            return if (deactivationResultList.any { it is Resource.Error }) Resource.Error(Failure.OneOrMoreAlarmsFailedToDeactivateFailure)
            else Resource.Success(Unit)
        } else if (userAlarms is Resource.Error) return Resource.Error(userAlarms.failure)
        return Resource.Error(Failure.DeactivateAllAlarmsUnknownFailure)
    }
}