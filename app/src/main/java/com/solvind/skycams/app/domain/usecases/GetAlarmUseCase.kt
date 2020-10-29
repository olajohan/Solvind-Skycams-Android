package com.solvind.skycams.app.domain.usecases

import com.solvind.skycams.app.core.Failure
import com.solvind.skycams.app.core.Resource
import com.solvind.skycams.app.core.UseCase
import com.solvind.skycams.app.di.IoDispatcher
import com.solvind.skycams.app.domain.model.Alarm
import com.solvind.skycams.app.domain.repo.IAlarmRepo
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

open class GetAlarmUseCase @Inject constructor(
    private val repo: IAlarmRepo,
    @IoDispatcher dispatcher: CoroutineDispatcher
) : UseCase<Alarm, GetAlarmUseCase.Params>(dispatcher) {
    data class Params(val skycamKey: String)

    override suspend fun run(params: Params): Resource<Alarm> {
        if (params.skycamKey.isEmpty()) return Resource.Error(Failure.EmptySkycamKeyFailure)
        return repo.getAlarm(params.skycamKey)
    }
}