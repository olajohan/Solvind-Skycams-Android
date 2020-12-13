package com.solvind.skycams.app.domain.usecases.alarm

import com.solvind.skycams.app.core.Resource
import com.solvind.skycams.app.core.UseCase
import com.solvind.skycams.app.domain.model.AlarmConfig
import com.solvind.skycams.app.domain.repo.IAlarmConfigRepo
import javax.inject.Inject

/**
 * Activates alarm for a single skycam.
 *
 * 1. Check if alarm already exsits
 *  - If not return Resource.Error with alarmNotFoundFailure
 * 2. Check if alarm is not timed out
 *  - If yes return Resource.Error with alarmHasTimedoutFailure
 * 3. Update alarm to activated
 *  - On failure to activate return Resource.Error alarmFailedToActivateFailure
 *
 * */
class ActivateAlarmConfigUseCase @Inject constructor(
    private val getAlarmConfigUseCase: GetAlarmConfigUseCase,
    private val alarmConfigRepo: IAlarmConfigRepo
) : UseCase<AlarmConfig, ActivateAlarmConfigUseCase.Params>() {

    data class Params(val skycamKey: String)

    override suspend fun run(params: Params): Resource<AlarmConfig> {

        when (val getAlarmConfigResource = getAlarmConfigUseCase.run(GetAlarmConfigUseCase.Params(params.skycamKey))) {

            is Resource.Success -> {
                val alarmConfig = getAlarmConfigResource.value

                return when (val setAlarmConfigResource = alarmConfigRepo.setAlarmConfig(alarmConfig.skycamKey, alarmConfig.alarmAvailableUntilEpochSeconds, true, alarmConfig.threshold)) {
                    is Resource.Success -> Resource.Success(setAlarmConfigResource.value)
                    is Resource.Error -> Resource.Error(setAlarmConfigResource.failure)
                }

            }

            is Resource.Error -> {
                return Resource.Error(getAlarmConfigResource.failure)
            }
        }
    }
}