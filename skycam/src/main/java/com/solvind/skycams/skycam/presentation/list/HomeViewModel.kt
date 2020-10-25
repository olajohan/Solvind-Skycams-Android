package com.solvind.skycams.skycam.presentation.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.solvind.skycams.skycam.domain.model.Skycam
import com.solvind.skycams.skycam.domain.usecases.GetAllSkycamsUseCase
import com.solvind.skycams.skycam.domain.usecases.GetSkycamFlowUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn

class HomeViewModel(
    private val getAllSkycamsUseCase: GetAllSkycamsUseCase,
    private val getSkycamFlowUseCase: GetSkycamFlowUseCase
) : ViewModel() {

    private val _skycams = MutableLiveData<List<Skycam>>()
    val skycams: LiveData<List<Skycam>>
        get() = _skycams

    fun getSkycamUpdates(skycamKey: String) = getSkycamFlowUseCase(GetSkycamFlowUseCase.Params(skycamKey))
            .flowOn(Dispatchers.IO)
            .asLiveData()
}