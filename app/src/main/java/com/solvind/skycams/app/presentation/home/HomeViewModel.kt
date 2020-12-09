package com.solvind.skycams.app.presentation.home

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.solvind.skycams.app.core.Resource
import com.solvind.skycams.app.core.UseCase
import com.solvind.skycams.app.domain.model.AlarmConfig
import com.solvind.skycams.app.domain.model.Skycam
import com.solvind.skycams.app.domain.usecases.*
import com.solvind.skycams.app.presentation.ads.RewardedAdLoader
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

@FlowPreview
@ExperimentalCoroutinesApi
class HomeViewModel @ViewModelInject constructor(
    private val mGetAllSkycamsUSeCase: GetAllSkycamsUseCase,
    private val mGetSkycamFlowUseCase: GetSkycamFlowUseCase,
    private val mGetAlarmConfigFlowUseCase: GetAlarmConfigFlowUseCase,
    private val mActivateAlarmConfigUseCase: ActivateAlarmConfigUseCase,
    private val mDeactivateAlarmConfigUseCase: DeactivateAlarmConfigUseCase,
    private val mRewardedAdLoader: RewardedAdLoader
) : ViewModel() {

    private val mSkycamList = MutableLiveData<List<Skycam>>()
    val skycamList = mSkycamList as LiveData<List<Skycam>>

    private val mBottomSheetState = MutableStateFlow<BottomSheetViewState>(BottomSheetViewState.Hidden)
    val bottomSheetState = mBottomSheetState.asStateFlow().asLiveData()

    val selectedSkycam = bottomSheetState.switchMap {
        when (it) {

            is BottomSheetViewState.VisibleWithSkycam -> {
                mGetSkycamFlowUseCase.run(GetSkycamFlowUseCase.Params(it.skycam.skycamKey)).asLiveData()
            }

           else -> MutableLiveData(null)
        }
    }

    val fabState = mBottomSheetState.filterIsInstance<BottomSheetViewState.VisibleWithSkycam>()
        .flatMapLatest {
            mGetAlarmConfigFlowUseCase.run(GetAlarmConfigFlowUseCase.Params(it.skycam.skycamKey))
        }
        .map {
            when {
                it.hasTimedOut() -> FabState.AlarmTimedOut(it)
                it.isActiveAndHasNotTimedOut() -> FabState.AlarmActivated(it)
                else -> FabState.AlarmDeactivated(it)
            }

        }
        .asLiveData()

    init {
        refreshSkycams()

        /*
        * Load an ad, so the user won't be waiting for the first ad request. This ad will not
        * be shown before the user clicks to get more ad minutes.
        * */
        viewModelScope.launch {
            mRewardedAdLoader.loadRewardedAd()
        }
    }

    private fun refreshSkycams() = viewModelScope.launch {
        mGetAllSkycamsUSeCase(this, UseCase.None()) {
            when (it) {
                is Resource.Success -> mSkycamList.postValue(it.value)
                is Resource.Error -> Timber.i("Error fetching skycams")
            }
        }
    }

    fun hideBottomSheet() {
        mBottomSheetState.value = BottomSheetViewState.Hidden
    }

    fun selectSkycam(skycam: Skycam) {
        mBottomSheetState.value = BottomSheetViewState.VisibleWithSkycam(skycam)
    }

    fun activateAlarm(skycamKey: String) = viewModelScope.launch {
        mActivateAlarmConfigUseCase(this, ActivateAlarmConfigUseCase.Params(skycamKey)) {
            // TODO Handle error
        }
    }

    fun deactivateAlarm(skycamKey: String) = viewModelScope.launch {
        mDeactivateAlarmConfigUseCase(this, DeactivateAlarmConfigUseCase.Params(skycamKey)) {
            // TODO Handle error
        }
    }
}

sealed class BottomSheetViewState(val state: Int) {
    object Hidden : BottomSheetViewState(BottomSheetBehavior.STATE_HIDDEN)
    data class VisibleWithSkycam(val skycam: Skycam) : BottomSheetViewState(BottomSheetBehavior.STATE_EXPANDED)
}

sealed class FabState {
    object Hidden : FabState()
    data class AlarmActivated(val alarmConfig: AlarmConfig) : FabState()
    data class AlarmDeactivated(val alarmConfig: AlarmConfig) : FabState()
    data class AlarmTimedOut(val alarmConfig: AlarmConfig) : FabState()
}