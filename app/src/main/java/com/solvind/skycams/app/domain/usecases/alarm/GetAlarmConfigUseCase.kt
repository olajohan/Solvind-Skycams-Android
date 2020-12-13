package com.solvind.skycams.app.domain.usecases.alarm

import com.solvind.skycams.app.core.Failure
import com.solvind.skycams.app.core.Resource
import com.solvind.skycams.app.core.UseCase
import com.solvind.skycams.app.domain.model.AlarmConfig
import com.solvind.skycams.app.domain.repo.IAlarmConfigRepo
import javax.inject.Inject

open class GetAlarmConfigUseCase @Inject constructor(
    private val configRepo: IAlarmConfigRepo
) : UseCase<AlarmConfig, GetAlarmConfigUseCase.Params>() {

    data class Params(val skycamKey: String)

    override suspend fun run(params: Params): Resource<AlarmConfig> {
        if (params.skycamKey.isEmpty()) return Resource.Error(Failure.EmptySkycamKeyFailure)
        return configRepo.getAlarmConfig(params.skycamKey)
    }
}