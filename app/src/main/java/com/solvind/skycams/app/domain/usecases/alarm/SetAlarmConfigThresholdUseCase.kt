package com.solvind.skycams.app.domain.usecases.alarm

import com.solvind.skycams.app.core.Failure
import com.solvind.skycams.app.core.Resource
import com.solvind.skycams.app.core.UseCase
import com.solvind.skycams.app.domain.model.AlarmConfig
import com.solvind.skycams.app.domain.repo.IAlarmConfigRepo
import javax.inject.Inject

class SetAlarmConfigThresholdUseCase @Inject constructor(
    private val mAlarmConfigRepo: IAlarmConfigRepo,
    private val mGetAlarmConfigUseCase: GetAlarmConfigUseCase
) :
    UseCase<AlarmConfig, SetAlarmConfigThresholdUseCase.Params>() {
    data class Params(
        val skycamKey: String,
        val newThreshold: Int
    )

    override suspend fun run(params: Params): Resource<AlarmConfig> {

        return  when (val alarmConfigResource = mGetAlarmConfigUseCase.run(GetAlarmConfigUseCase.Params(params.skycamKey))) {
            is Resource.Error -> {
                Resource.Error(alarmConfigResource.failure)
            }
            is Resource.Success -> {
                val newThreshold = params.newThreshold
                val alarmConfig = alarmConfigResource.value
                when {
                    newThreshold < 51 -> Resource.Error(Failure.AlarmThresholdToLowFailure)
                    newThreshold > 100 -> Resource.Error(Failure.AlarmThresholdToHighFailure)
                    else -> {

                        when (val repoResult = mAlarmConfigRepo.setAlarmConfig(alarmConfig.skycamKey, alarmConfig.alarmAvailableUntilEpochSeconds, alarmConfig.isActive, newThreshold)) {
                            is Resource.Success -> Resource.Success(repoResult.value)
                            is Resource.Error -> Resource.Error(repoResult.failure)
                        }
                    }
                }
            }

        }
    }
}