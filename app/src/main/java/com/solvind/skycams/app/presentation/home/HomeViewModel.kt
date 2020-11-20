package com.solvind.skycams.app.presentation.home

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.solvind.skycams.app.core.Resource
import com.solvind.skycams.app.core.UseCase
import com.solvind.skycams.app.domain.model.Skycam
import com.solvind.skycams.app.domain.usecases.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

@ExperimentalCoroutinesApi
class HomeViewModel @ViewModelInject constructor(
    private val getAllSkycamsUseCase: GetAllSkycamsUseCase,
    private val getSkycamFlowUseCase: GetSkycamFlowUseCase,
    private val getAlarmConfigUseCase: GetAlarmConfigUseCase,
    private val getAlarmConfigFlowUseCase: GetAlarmConfigFlowUseCase,
    private val activateAlarmConfigUseCase: ActivateAlarmConfigUseCase,
    private val deactivateAlarmConfigUseCase: DeactivateAlarmConfigUseCase,
    private val updateAlarmConfigConfigUseCase: UpdateAlarmConfigTimeUseCase
) : ViewModel() {

    private val mSkycamBottomSheetViewState = MutableStateFlow<SkycamBottomSheetViewState>(SkycamBottomSheetViewState.Hidden)
    val skycamBottomSheetViewState = mSkycamBottomSheetViewState.asStateFlow().asLiveData()


    /**
     * 1. Only emit skycam updates when the skycam bottom sheet is in the visible state.
     * */
    val skycamStream = mSkycamBottomSheetViewState.filter {
        it is SkycamBottomSheetViewState.Visible
    }.flatMapLatest {
        val selectedSkycam = (it as SkycamBottomSheetViewState.Visible).skycam
        getSkycamFlowUseCase.run(GetSkycamFlowUseCase.Params(selectedSkycam.skycamKey))
    }.conflate().asLiveData()

    private val mSkycams = MutableLiveData<List<Skycam>>()
    val skycams = mSkycams as LiveData<List<Skycam>>

    init { refreshSkycamList() }

    private fun refreshSkycamList() = viewModelScope.launch {
        getAllSkycamsUseCase(this, UseCase.None()) {
            when (it) {
                is Resource.Success -> mSkycams.postValue(it.value)
                is Resource.Error -> Timber.i("Failure: ${it.failure}")
            }
        }
    }

    fun selectMapObject(mapObject: Any?)  {
        when(mapObject) {
            is Skycam -> mSkycamBottomSheetViewState.value = SkycamBottomSheetViewState.Visible(mapObject)
        }
    }

    fun clearMapObjectSelection() {
        mSkycamBottomSheetViewState.value = SkycamBottomSheetViewState.Hidden
    }

    fun activateAlarm(skycamKey: String) = viewModelScope.launch {
        activateAlarmConfigUseCase(this, ActivateAlarmConfigUseCase.Params(skycamKey)) {
            when(it) {
                is Resource.Success -> { Timber.i("Alarm activated for $skycamKey") }
                is Resource.Error -> { Timber.i("Error activating alarm: ${it.failure}") }
            }
        }
    }

    fun deactivateAlarm(skycamKey: String) = viewModelScope.launch {
        deactivateAlarmConfigUseCase(this, DeactivateAlarmConfigUseCase.Params(skycamKey)) {
            when (it) {
                is Resource.Success -> Timber.i("Alarm deactivated for $skycamKey")
                is Resource.Error -> Timber.i("Error deactivating alarm: ${it.failure}")
            }
        }
    }

    sealed class SkycamBottomSheetViewState(val state: Int) {
        object Hidden : SkycamBottomSheetViewState(BottomSheetBehavior.STATE_HIDDEN)
        data class Visible (val skycam: Skycam) : SkycamBottomSheetViewState(BottomSheetBehavior.STATE_HALF_EXPANDED)
    }
}