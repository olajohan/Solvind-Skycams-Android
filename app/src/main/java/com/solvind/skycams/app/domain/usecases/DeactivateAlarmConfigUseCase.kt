package com.solvind.skycams.app.domain.usecases

import com.solvind.skycams.app.core.Resource
import com.solvind.skycams.app.core.UseCase
import com.solvind.skycams.app.di.IoDispatcher
import com.solvind.skycams.app.di.MainDispatcher
import com.solvind.skycams.app.domain.repo.IAlarmConfigRepo
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class DeactivateAlarmConfigUseCase @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher,
    private val getAlarmConfigUseCase: GetAlarmConfigUseCase,
    private val alarmConfigRepo: IAlarmConfigRepo
) : UseCase<Unit, DeactivateAlarmConfigUseCase.Params>(ioDispatcher, mainDispatcher) {

    data class Params(val skycamKey: String)

    override suspend fun run(params: Params): Resource<Unit> {

        return when (val getAlarmConfigResult =
            getAlarmConfigUseCase.run(GetAlarmConfigUseCase.Params(params.skycamKey))) {
            is Resource.Error -> Resource.Error(getAlarmConfigResult.failure)
            is Resource.Success -> {
                val currentAlarmConfig = getAlarmConfigResult.value

                when (val setAlarmConfigResult = alarmConfigRepo.setAlarmConfig(params.skycamKey, currentAlarmConfig.alarmAvailableUntilEpochSeconds, false)) {
                    is Resource.Error -> Resource.Error(setAlarmConfigResult.failure)
                    is Resource.Success -> Resource.Success(Unit)
                }
            }
        }
    }
}