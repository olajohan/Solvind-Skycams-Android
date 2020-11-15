package com.solvind.skycams.app.domain.usecases

import com.solvind.skycams.app.core.Failure
import com.solvind.skycams.app.core.Resource
import com.solvind.skycams.app.core.UseCase
import com.solvind.skycams.app.di.IoDispatcher
import com.solvind.skycams.app.di.MainDispatcher
import com.solvind.skycams.app.domain.repo.IAlarmConfigRepo
import kotlinx.coroutines.CoroutineDispatcher
import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Activates alarm for a single skycam.
 *
 * 1. Get the current alarm
 * - If there are is no record in the database for the current alarm, then insert a new record with
 *   with the current time + 30 minutes and return success.
 * 2. Update the alarm with the isActive field set to true
 * */
class ActivateAlarmConfigUseCase @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher,
    private val getAlarmConfigUseCase: GetAlarmConfigUseCase,
    private val alarmConfigRepo: IAlarmConfigRepo
): UseCase<Unit, ActivateAlarmConfigUseCase.Params>(ioDispatcher, mainDispatcher) {

    data class Params(val skycamKey: String)

    override suspend fun run(params: Params): Resource<Unit> {

        return when (val getAlarmConfigResult = getAlarmConfigUseCase.run(GetAlarmConfigUseCase.Params(params.skycamKey))) {
            is Resource.Error -> if (getAlarmConfigResult.failure is Failure.AlarmNotFoundFailure) {
                when (val setAlarmConfigResult = alarmConfigRepo.setAlarmConfig(params.skycamKey, Instant.now().epochSecond + TimeUnit.MINUTES.toSeconds(30L), true)) {
                    is Resource.Success -> Resource.Success(Unit)
                    is Resource.Error -> Resource.Error(setAlarmConfigResult.failure)
                }
            } else {
                Resource.Error(getAlarmConfigResult.failure)
            }
            is Resource.Success -> {
                val currentAlarmConfig = getAlarmConfigResult.value

                if (currentAlarmConfig.alarmAvailableUntilEpochSeconds < Instant.now().epochSecond) {
                    Resource.Error(Failure.AlarmTimedOutFailure)
                } else {
                    when (val setAlarmConfigResult = alarmConfigRepo.setAlarmConfig(params.skycamKey, currentAlarmConfig.alarmAvailableUntilEpochSeconds, true)) {
                        is Resource.Error -> Resource.Error(setAlarmConfigResult.failure)
                        is Resource.Success -> Resource.Success(Unit)
                    }
                }
            }
        }
    }
}