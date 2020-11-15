package com.solvind.skycams.app.domain.usecases

import com.solvind.skycams.app.core.Failure
import com.solvind.skycams.app.core.Resource
import com.solvind.skycams.app.core.UseCase
import com.solvind.skycams.app.di.IoDispatcher
import com.solvind.skycams.app.di.MainDispatcher
import com.solvind.skycams.app.domain.model.AlarmConfig
import com.solvind.skycams.app.domain.repo.IAlarmConfigRepo
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

open class GetAlarmConfigUseCase @Inject constructor(
    private val configRepo: IAlarmConfigRepo,
    @IoDispatcher ioDispatcher: CoroutineDispatcher,
    @MainDispatcher mainDispatcher: CoroutineDispatcher
) : UseCase<AlarmConfig, GetAlarmConfigUseCase.Params>(ioDispatcher, mainDispatcher) {

    data class Params(val skycamKey: String)

    override suspend fun run(params: Params): Resource<AlarmConfig> {
        if (params.skycamKey.isEmpty()) return Resource.Error(Failure.EmptySkycamKeyFailure)
        return configRepo.getAlarmConfig(params.skycamKey)
    }
}