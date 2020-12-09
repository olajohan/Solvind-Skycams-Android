package com.solvind.skycams.app.presentation.ads

import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.solvind.skycams.app.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@ExperimentalCoroutinesApi
@Singleton
class RewardedAdLoader @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val mAdUnitId = if (BuildConfig.DEBUG) {
        "ca-app-pub-3940256099942544/5224354917"
    } else {
        "ca-app-pub-8129908873162978/5890143437"
    }

    private lateinit var mRewardedAd: RewardedAd

    fun getRewardedAdStatusFlow() = flow {
        if (this@RewardedAdLoader::mRewardedAd.isInitialized && mRewardedAd.isLoaded) {
            emit(RewardedAdStatus.Ready(mRewardedAd))
        } else {
            emit(RewardedAdStatus.Loading)
            emit(loadRewardedAd())
        }
    }

    suspend fun loadRewardedAd() = suspendCoroutine<RewardedAdStatus> { continuation ->
        mRewardedAd = RewardedAd(context, mAdUnitId)
        mRewardedAd.loadAd(AdRequest.Builder().build(), object : RewardedAdLoadCallback() {
            override fun onRewardedAdLoaded() {
                continuation.resume(RewardedAdStatus.Ready(mRewardedAd))
            }

            override fun onRewardedAdFailedToLoad(loadError: LoadAdError?) {
                continuation.resume(RewardedAdStatus.Error(loadError))
            }
        })
    }

    fun isAdLoaded() : Boolean = mRewardedAd.isLoaded
}

sealed class RewardedAdStatus {
    object Loading : RewardedAdStatus()
    data class Ready(val rewardedAd: RewardedAd) : RewardedAdStatus()
    data class Error(val loadError: LoadAdError?) : RewardedAdStatus()
}