package com.solvindskycams.skycam.presentation.list

import androidx.lifecycle.*
import com.solvindskycams.common.Failure
import com.solvindskycams.common.usecases.UseCase
import com.solvindskycams.skycam.domain.model.Skycam
import com.solvindskycams.skycam.domain.usecases.GetAllSkycamsUseCase
import com.solvindskycams.skycam.domain.usecases.GetSkycamFlowUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import timber.log.Timber

class HomeViewModel(
    private val getAllSkycamsUseCase: GetAllSkycamsUseCase,
    private val getSkycamFlowUseCase: GetSkycamFlowUseCase
) : ViewModel() {

    private val _skycams = MutableLiveData<List<Skycam>>()
    val skycams: LiveData<List<Skycam>>
        get() = _skycams

    fun getSkycamUpdates(skycamKey: String) = getSkycamFlowUseCase(GetSkycamFlowUseCase.Params(skycamKey))
            .flowOn(Dispatchers.IO)
            .catch {e ->
                Timber.i(e)
            }
            .asLiveData()


    fun loadSkycams() = viewModelScope.launch {

        getAllSkycamsUseCase(this, UseCase.None()) {
            it.either(::handleGetAllSkycamsFailure, ::handleGetAllSkycamsSuccess)
        }

    }

    private fun handleGetAllSkycamsFailure(failure: Failure) {
        Timber.i("Failure: $failure while loading skycams")
    }

    private fun handleGetAllSkycamsSuccess(list: List<Skycam>) {
        _skycams.value = list
    }
}