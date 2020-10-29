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

class HomeViewModel @ViewModelInject constructor(
    private val getAllSkycamsUseCase: GetAllSkycamsUseCase,
    private val getSkycamFlowUseCase: GetSkycamFlowUseCase,
    private val getAlarmFlowUseCase: GetAlarmFlowUseCase,
    private val activateAlarmUseCase: ActivateAlarmUseCase,
    private val deactivateAlarmUseCase: DeactivateAlarmUseCase
) : ViewModel(), IProvideSkycamLiveData, IHandleAlarm {

    private val viewState = MutableLiveData<ViewState>()
    val viewStateReadOnly = viewState as LiveData<ViewState>

    init { refreshSkycamList() }

    fun refreshSkycamList() {
        viewState.value = ViewState.Loading

        viewModelScope.launch {
            getAllSkycamsUseCase(this, UseCase.None()) { result ->
                when(result) {
                    is Resource.Success -> viewState.value = ViewState.Success(result.value)
                    is Resource.Error -> viewState.value = ViewState.Failed(result.failure)
                }
            }
        }
    }

    sealed class ViewState() {
        object Loading : ViewState()
        data class Success(val skycamList: List<Skycam>) : ViewState()
        data class Failed(val failure: Failure) : ViewState()
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


}