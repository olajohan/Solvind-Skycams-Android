package com.solvind.skycams.app.domain.usecases.alarm

import com.solvind.skycams.app.core.Failure
import com.solvind.skycams.app.core.Resource
import com.solvind.skycams.app.core.UseCase
import com.solvind.skycams.app.domain.model.AlarmConfig
import com.solvind.skycams.app.domain.repo.IAlarmConfigRepo
import java.time.Instant
import javax.inject.Inject

/**
 * 1. Get the current alarm available until for a single skycam
 * 2. If the alarmtime is in the past take the present epochseconds and add the extra seconds
 *  - If the alarmtime is in the future, take the future epochseoconds and add the extra seconds
 *
 *  If the current alarm is not found, we insert a new record with a new alarm time of
 *  the the current instant plus the provided seconds.
 * */
open class RewardUserAlarmTimeUseCase @Inject constructor(
    private val configRepo: IAlarmConfigRepo,
    private val getAlarmConfigUseCase: GetAlarmConfigUseCase
) : UseCase<AlarmConfig, RewardUserAlarmTimeUseCase.Params>() {

    data class Params(
        val skycamKey: String,
        val plusEpochSeconds: Long
    )

    override suspend fun run(params: Params): Resource<AlarmConfig> {
        if (params.plusEpochSeconds < 0) return Resource.Error(Failure.UpdateAlarmTimeLessThanZeroFailure)

        val currentAlarm = getAlarmConfigUseCase.run(GetAlarmConfigUseCase.Params(params.skycamKey))
        if (currentAlarm is Resource.Success) {

            val now = Instant.now().epochSecond
            val currentAlarmTime = currentAlarm.value.alarmAvailableUntilEpochSeconds

            val newTime = if (currentAlarmTime < now) now + params.plusEpochSeconds
            else currentAlarmTime + params.plusEpochSeconds

            return configRepo.setAlarmConfig(params.skycamKey, newTime, currentAlarm.value.isActive, currentAlarm.value.threshold)

        } else if (currentAlarm is Resource.Error && currentAlarm.failure == Failure.AlarmNotFoundFailure) {
            return configRepo.setAlarmConfig(params.skycamKey, Instant.now().epochSecond + params.plusEpochSeconds, false)
        }
        return Resource.Error(Failure.UpdateAlarmTimeUnknownFailure)
    }
}