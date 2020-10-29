package com.solvind.skycams.app.domain.usecases

import com.solvind.skycams.app.core.Resource
import com.solvind.skycams.app.core.UseCase
import com.solvind.skycams.app.di.IoDispatcher
import com.solvind.skycams.app.domain.model.Alarm
import com.solvind.skycams.app.domain.repo.IAlarmRepo
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class GetAllAlarmsUseCase @Inject constructor(
    private val alarmRepo: IAlarmRepo,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : UseCase<List<Alarm>, UseCase.None>(dispatcher) {

    override suspend fun run(params: None): Resource<List<Alarm>> = alarmRepo.getAllAlarms()

}