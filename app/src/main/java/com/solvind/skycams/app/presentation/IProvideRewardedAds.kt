package com.solvind.skycams.app.presentation

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Job

interface IProvideRewardedAds {

    val rewardedAdState: LiveData<RewardedAdState>
    fun rewardUserAlarmTime(skycamKey: String, plusEpochSeconds: Long) : Job
}