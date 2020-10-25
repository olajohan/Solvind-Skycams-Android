package com.solvind.skycams.skycam.domain.usecases

import com.solvind.skycams.skycam.Failure
import com.solvind.skycams.skycam.Result
import com.solvind.skycams.skycam.UseCase
import com.solvind.skycams.skycam.domain.model.Alarm
import com.solvind.skycams.skycam.domain.repo.IAlarmRepo
import javax.inject.Inject

class GetAlarmUseCase @Inject constructor(private val repo: IAlarmRepo) : UseCase<Alarm, GetAlarmUseCase.Params>() {
    data class Params(val skycamKey: String)

    override suspend fun run(params: Params): Result<Alarm> {
        if (params.skycamKey.isEmpty()) return Result.Error(Failure.EmptySkycamKeyFailure)
        return repo.getAlarm(params.skycamKey)
    }
}