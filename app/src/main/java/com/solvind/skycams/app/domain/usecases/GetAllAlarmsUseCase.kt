package com.solvind.skycams.app.domain.usecases

import com.solvind.skycams.app.core.Resource
import com.solvind.skycams.app.core.UseCase
import com.solvind.skycams.app.di.IoDispatcher
import com.solvind.skycams.app.di.MainDispatcher
import com.solvind.skycams.app.domain.model.AlarmConfig
import com.solvind.skycams.app.domain.repo.IAlarmConfigRepo
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class GetAllAlarmsUseCase @Inject constructor(
    private val alarmConfigRepo: IAlarmConfigRepo,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher
) : UseCase<List<AlarmConfig>, UseCase.None>(ioDispatcher, mainDispatcher) {

    override suspend fun run(params: None): Resource<List<AlarmConfig>> = alarmConfigRepo.getAllAlarmConfigs()

}