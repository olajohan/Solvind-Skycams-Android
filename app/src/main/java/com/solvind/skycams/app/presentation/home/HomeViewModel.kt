package com.solvind.skycams.app.presentation.home

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.solvind.skycams.app.core.Failure
import com.solvind.skycams.app.core.Resource
import com.solvind.skycams.app.core.SingleEvent
import com.solvind.skycams.app.core.UseCase
import com.solvind.skycams.app.domain.model.AlarmConfig
import com.solvind.skycams.app.domain.model.Skycam
import com.solvind.skycams.app.domain.usecases.alarm.*
import com.solvind.skycams.app.domain.usecases.skycam.GetAllSkycamsUseCase
import com.solvind.skycams.app.domain.usecases.skycam.GetSkycamFlowUseCase
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
    private val mSetAlarmConfigThresholdUseCase: SetAlarmConfigThresholdUseCase,
    private val mRewardUserAlarmTimeUseCase: RewardUserAlarmTimeUseCase,
    private val mRewardedAdLoader: RewardedAdLoader
) : ViewModel() {

    private val mSkycamList = MutableLiveData<List<Skycam>>()
    val skycamList = mSkycamList as LiveData<List<Skycam>>

    private val mShowFirstTimeActivationDialog = MutableLiveData<SingleEvent<String>>()
    val showFirstTimeActivationDialog = mShowFirstTimeActivationDialog as LiveData<SingleEvent<String>>

    private val mShowAlarmHasTimedOutWhenActivatingDialog = MutableLiveData<SingleEvent<AlarmConfig>>()
    val showAlarmHasTimedOutWhenActivatingDialog = mShowAlarmHasTimedOutWhenActivatingDialog as LiveData<SingleEvent<AlarmConfig>>

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

    val selectedAlarmConfig = mBottomSheetState
        .filterIsInstance<BottomSheetViewState.VisibleWithSkycam>()
        .flatMapLatest {
            mGetAlarmConfigFlowUseCase.run(GetAlarmConfigFlowUseCase.Params(it.skycam.skycamKey))

                // Clear any previous selected alarm config
                .onStart {
                    emit(
                        AlarmConfig("", -1L, false)
                    )
                }
        }.asLiveData()

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
            when (it) {

                is Resource.Success -> {
                    // Show dialog if the user activates a timed out alarm
                    if (it.value.hasTimedOut()) {
                        mShowAlarmHasTimedOutWhenActivatingDialog.value = SingleEvent(it.value)
                    }

                }

                is Resource.Error -> {
                    when (it.failure) {

                        /*
                        * If the user never have activated the alarm before, show a dialog and reward the user 1h of free alarm time.
                        * */
                        is Failure.AlarmNotFoundFailure -> {
                            mShowFirstTimeActivationDialog.value = SingleEvent(skycamKey)
                        }
                        else -> Timber.i("Failed to activate alarm: ${it.failure}")
                    }
                }
            }
        }
    }

    fun deactivateAlarm(skycamKey: String) = viewModelScope.launch {
        mDeactivateAlarmConfigUseCase(this, DeactivateAlarmConfigUseCase.Params(skycamKey)) {
            // TODO Handle error
        }
    }

    fun setNewAlarmConfigThreshold(skycamKey: String, newThreshold: Int) = viewModelScope.launch {
        mSetAlarmConfigThresholdUseCase(this, SetAlarmConfigThresholdUseCase.Params(skycamKey, newThreshold)) {
            when (it) {
                is Resource.Success -> Resource.Success(it.value)
                is Resource.Error -> Timber.i("Error while setting new alarm threshold ${it.failure}")
            }
        }
    }

    fun rewardUserAlarmTimeFirstActivation(skycamKey: String, rewardedSeconds: Long) = viewModelScope.launch {
        launch {
            mRewardUserAlarmTimeUseCase(this, RewardUserAlarmTimeUseCase.Params(skycamKey, rewardedSeconds)) {
                when (it) {
                    is Resource.Success -> Timber.i("User rewarded alarmtime: $rewardedSeconds")
                    is Resource.Error -> Timber.i("Error when rewarding user alarmtime ${it.failure}")
                }
            }
        }.join()

        mActivateAlarmConfigUseCase(this, ActivateAlarmConfigUseCase.Params(skycamKey)) {
            when (it) {
                is Resource.Success -> Timber.i("Skycam alarm activated for $skycamKey")
                is Resource.Error -> Timber.i("Error when activating alarm for skycam $skycamKey ${it.failure}")
            }
        }
    }
}

sealed class BottomSheetViewState(val state: Int) {
    object Hidden : BottomSheetViewState(BottomSheetBehavior.STATE_HIDDEN)
    data class VisibleWithSkycam(val skycam: Skycam) : BottomSheetViewState(BottomSheetBehavior.STATE_EXPANDED)
}