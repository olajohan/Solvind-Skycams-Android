package com.solvind.skycams.app.presentation.home

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.solvind.skycams.app.core.Failure
import com.solvind.skycams.app.core.Resource
import com.solvind.skycams.app.core.UseCase
import com.solvind.skycams.app.domain.model.Skycam
import com.solvind.skycams.app.domain.usecases.*
import com.solvind.skycams.app.presentation.IHandleAlarm
import com.solvind.skycams.app.presentation.IProvideSkycamLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * - Loads a list of skycams to be used in the homefragments recyclerview.
 * - Offers skycam livedata which
 * */
class HomeViewModel @ViewModelInject constructor(
    private val getAllSkycamsUseCase: GetAllSkycamsUseCase,
    private val getSkycamFlowUseCase: GetSkycamFlowUseCase,
    private val getAlarmFlowUseCase: GetAlarmFlowUseCase,
    private val activateAlarmUseCase: ActivateAlarmUseCase,
    private val deactivateAlarmUseCase: DeactivateAlarmUseCase
) : ViewModel(), IProvideSkycamLiveData, IHandleAlarm {

    private val mainViewState = MutableLiveData<MainViewState>()
    val mainViewStateReadOnly = mainViewState as LiveData<MainViewState>

    fun refreshSkycamList() {
        mainViewState.value = MainViewState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            getAllSkycamsUseCase(this, UseCase.None()) { result ->
                when(result) {
                    is Resource.Success -> mainViewState.value = MainViewState.Success(result.value)
                    is Resource.Error -> mainViewState.value = MainViewState.Failed(result.failure)
                }
            }
        }
    }

    override fun getSkycamLiveData(skycamKey: String) =
        getSkycamFlowUseCase.run(GetSkycamFlowUseCase.Params(skycamKey))
            .flowOn(Dispatchers.IO)
            .catch { exception ->
                Timber.i("""
                    Error while receiving skycam flow: ${exception.message} 

                    ${exception.printStackTrace()}
                """.trimIndent())
            }
            .asLiveData()

    override fun getAlarmLiveData(skycamKey: String)= getAlarmFlowUseCase.run(GetAlarmFlowUseCase.Params(skycamKey))
        .flowOn(Dispatchers.IO)
        .catch { exception ->
            Timber.i("""
                    Error while receiving skycam flow: ${exception.message} 

                    ${exception.printStackTrace()}
                """.trimIndent())
        }
        .asLiveData()

    override fun activateAlarm(skycamKey: String) = viewModelScope.launch {
        activateAlarmUseCase(this, ActivateAlarmUseCase.Params(skycamKey))
        Timber.i("Activate alarm called!")
    }

    override fun deactivateAlarm(skycamKey: String) = viewModelScope.launch {
        deactivateAlarmUseCase(this, DeactivateAlarmUseCase.Params(skycamKey))
    }

    sealed class MainViewState() {
        object Loading : MainViewState()
        data class Success(val skycamList: List<Skycam>) : MainViewState()
        data class Failed(val failure: Failure) : MainViewState()
    }
}