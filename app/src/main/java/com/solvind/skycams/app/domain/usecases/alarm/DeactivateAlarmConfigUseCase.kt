package com.solvind.skycams.app.domain.usecases.alarm

import com.solvind.skycams.app.core.Resource
import com.solvind.skycams.app.core.UseCase
import com.solvind.skycams.app.domain.repo.IAlarmConfigRepo
import javax.inject.Inject

class DeactivateAlarmConfigUseCase @Inject constructor(
    private val getAlarmConfigUseCase: GetAlarmConfigUseCase,
    private val alarmConfigRepo: IAlarmConfigRepo
) : UseCase<Unit, DeactivateAlarmConfigUseCase.Params>() {

    data class Params(val skycamKey: String)

    override suspend fun run(params: Params): Resource<Unit> {

        return when (val alarmConfigResult = getAlarmConfigUseCase.run(GetAlarmConfigUseCase.Params(params.skycamKey))) {

            is Resource.Success -> {
                val alarmConfig = alarmConfigResult.value

                when(val setAlarmConfigResource = alarmConfigRepo.setAlarmConfig(alarmConfig.skycamKey, alarmConfig.alarmAvailableUntilEpochSeconds, false, alarmConfig.threshold)) {
                    is Resource.Success -> Resource.Success(Unit)

                    is Resource.Error -> Resource.Error(setAlarmConfigResource.failure)
                }
            }

            is Resource.Error -> Resource.Error(alarmConfigResult.failure)

        }
    }
}