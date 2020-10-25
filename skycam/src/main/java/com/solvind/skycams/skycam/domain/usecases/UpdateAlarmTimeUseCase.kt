package com.solvind.skycams.skycam.domain.usecases

import com.solvind.skycams.skycam.Failure
import com.solvind.skycams.skycam.Result
import com.solvind.skycams.skycam.Result.Error
import com.solvind.skycams.skycam.Result.Success
import com.solvind.skycams.skycam.UseCase
import com.solvind.skycams.skycam.domain.repo.IAlarmRepo
import java.time.Instant
import javax.inject.Inject

/**
 * 1. Get the current alarm available until for a single skycam
 * 2. If the alarmtime is in the past take the present epochseconds and add the extra seconds
 *  - If the alarmtime is in the future, take the future epochseoconds and add the extra seconds
 * */
class UpdateAlarmTimeUseCase @Inject constructor(
    private val repo: IAlarmRepo,
    private val getAlarmUseCase: GetAlarmUseCase
) : UseCase<Unit, UpdateAlarmTimeUseCase.Params>() {

    data class Params(
        val skycamKey: String,
        val plusEpochSeconds: Long
    )

    override suspend fun run(params: Params): Result<Unit> {
        if (params.plusEpochSeconds < 0) return Error(Failure.UpdateAlarmTimeLessThanZeroFailure)
        val currentAlarm = getAlarmUseCase.run(GetAlarmUseCase.Params(params.skycamKey))
        if (currentAlarm is Success) {

            val now = Instant.now().epochSecond
            val currentAlarmTime = currentAlarm.value.alarmAvailableUntilEpochSeconds

            val newTime = if (currentAlarmTime < now) now + params.plusEpochSeconds
            else currentAlarmTime + params.plusEpochSeconds

            return repo.setAlarm(params.skycamKey, newTime)

        } else if (currentAlarm is Error) return Error(currentAlarm.failure)
        return Error(Failure.UpdateAlarmTimeUnknownFailure)
    }
}