package com.solvind.skycams.app.presentation.ads

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.solvind.skycams.app.core.Failure
import com.solvind.skycams.app.core.Resource
import com.solvind.skycams.app.domain.usecases.alarm.RewardUserAlarmTimeUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


@ExperimentalCoroutinesApi
class RewardedAdViewModel @ViewModelInject constructor(
    private val mRewardedAdLoader: RewardedAdLoader,
    private val mRewardUserAlarmTimeUseCase: RewardUserAlarmTimeUseCase
) : ViewModel() {

    val rewardedAdStatus = mRewardedAdLoader.getRewardedAdStatusFlow().asLiveData()

    private val mUserRewardEvent = MutableStateFlow<UserRewardEvent>(UserRewardEvent.Waiting)
    val userRewardEvent = mUserRewardEvent.asLiveData()

    /**
     * Rewarded amount is in minutes while the update to the database is in seconds.
     * */
    fun rewardUser(skycamKey: String, rewardedMinutes: Int) = viewModelScope.launch {
        val rewardedSeconds = TimeUnit.MINUTES.toSeconds(rewardedMinutes.toLong())
        mRewardUserAlarmTimeUseCase(this, RewardUserAlarmTimeUseCase.Params(skycamKey, rewardedSeconds)) {
            when(it) {
                is Resource.Success -> mUserRewardEvent.value = UserRewardEvent.UserRewardedSuccesfully(rewardedMinutes)
                is Resource.Error -> {
                    mUserRewardEvent.value = UserRewardEvent.UserNotRewarded(it.failure)
                }
            }
        }

        // Download the next ad, so the user won't have to wait on the loading screen.
        mRewardedAdLoader.loadRewardedAd()
    }
}

sealed class UserRewardEvent {
    object Waiting : UserRewardEvent()
    data class UserRewardedSuccesfully(val alarmMinutesReward: Int) : UserRewardEvent()
    data class UserNotRewarded(val failure: Failure) : UserRewardEvent()
}