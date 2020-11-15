package com.solvind.skycams.app.domain.usecases

import com.solvind.skycams.app.core.Resource
import com.solvind.skycams.app.core.UseCase
import com.solvind.skycams.app.di.IoDispatcher
import com.solvind.skycams.app.di.MainDispatcher
import com.solvind.skycams.app.domain.model.AlarmConfig
import com.solvind.skycams.app.domain.model.Skycam
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


/**
 * 1. Get a list of all skycams
 * 2. Get a flow for each of the skycams
 * 3. Get a flow for each of the skycams alarms
 * 4. Return the two flows in a list of pairs
 * */
open class GetListOfAllSkycamFlowsPairedWithAlarmFlows @Inject constructor(
    private val getAllSkycamsUseCase: GetAllSkycamsUseCase,
    private val getSkycamFlowUseCase: GetSkycamFlowUseCase,
    private val getAlarmConfigFlowUseCase: GetAlarmConfigFlowUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher
) : UseCase<List<Pair<Flow<Skycam>, Flow<AlarmConfig>>>, UseCase.None>(ioDispatcher, mainDispatcher) {

    override suspend fun run(params: None): Resource<List<Pair<Flow<Skycam>, Flow<AlarmConfig>>>>  {

        return when (val getAllSkycamResult = getAllSkycamsUseCase.run(None())) {
            is Resource.Error -> Resource.Error(getAllSkycamResult.failure)
            is Resource.Success -> {
                val resultList = getAllSkycamResult.value.mapTo(mutableListOf()) {
                    val skycamFlow = getSkycamFlowUseCase.run(GetSkycamFlowUseCase.Params(it.skycamKey))
                    val alarmFlow = getAlarmConfigFlowUseCase.run(GetAlarmConfigFlowUseCase.Params(it.skycamKey))
                    Pair(skycamFlow, alarmFlow)
                }
                Resource.Success(resultList)
            }
        }
    }
}