package com.solvind.skycams.app.presentation.ads

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.solvind.skycams.app.BuildConfig
import com.solvind.skycams.app.core.EspressoIdlingResource
import com.solvind.skycams.app.core.Failure
import com.solvind.skycams.app.core.Resource
import com.solvind.skycams.app.domain.usecases.UpdateAlarmConfigTimeUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@ExperimentalCoroutinesApi
@Singleton
class AdProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val updateAlarmConfigTimeUseCase: UpdateAlarmConfigTimeUseCase
) {

    private val mRewarded_ad_unit_id =
        if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/5224354917" else "ca-app-pub-8129908873162978/5890143437"
    private lateinit var mRewardedAd: RewardedAd

    private val mAdState = MutableStateFlow<AdState>(AdState.Loading)
    val adState = mAdState.asLiveData()

    fun showRewardedAd(activity: AppCompatActivity, skycamKey: String) {
        EspressoIdlingResource.increment()
        if (this::mRewardedAd.isInitialized && mRewardedAd.isLoaded) {
            mRewardedAd.show(activity, DefaultRewardedAdCallback(activity, skycamKey))
        } else {
            activity.lifecycleScope.launch {
                when (loadNewRewardedAd()) {
                    is Resource.Success -> {
                        showRewardedAd(activity, skycamKey)
                        EspressoIdlingResource.decrement()
                    }
                    is Resource.Error -> {
                        Toast.makeText(
                            activity,
                            "ERROR LOADING AD",
                            Toast.LENGTH_SHORT
                        ).show()
                        mAdState.value = AdState.Failed
                    }
                }
            }
        }
    }

    suspend fun loadNewRewardedAd(): Resource<RewardedAd> =
        suspendCoroutine { continuation ->

            if (!this::mRewardedAd.isInitialized || !mRewardedAd.isLoaded) {
                mAdState.value = AdState.Loading
                mRewardedAd = RewardedAd(context, mRewarded_ad_unit_id)

                val adLoadCallback = object : RewardedAdLoadCallback() {

                    override fun onRewardedAdLoaded() {
                        mAdState.value = AdState.Ready
                        EspressoIdlingResource.decrement()
                        continuation.resume(Resource.Success(mRewardedAd))
                    }

                    override fun onRewardedAdFailedToLoad(adError: LoadAdError) {
                        continuation.resume(Resource.Error(Failure.FailedToLoadAd))
                        mAdState.value = AdState.Failed
                    }
                }

                mRewardedAd.loadAd(AdRequest.Builder().build(), adLoadCallback)
                EspressoIdlingResource.increment()
            } else {
                continuation.resume(Resource.Success(mRewardedAd))
            }
        }

    private inner class DefaultRewardedAdCallback(
        private val activity: AppCompatActivity,
        private val skycamKey: String
    ) : RewardedAdCallback() {
        override fun onUserEarnedReward(reward: RewardItem) {
            val plusEpochSeconds = TimeUnit.MINUTES.toSeconds(reward.amount.toLong())

            activity.lifecycleScope.launch {
                updateAlarmConfigTimeUseCase(
                    this,
                    UpdateAlarmConfigTimeUseCase.Params(skycamKey, plusEpochSeconds)
                ) {
                    when (it) {
                        is Resource.Success -> {
                            Toast.makeText(
                                activity,
                                "Gratulations! You earned ${reward.amount} alarm minutes",
                                Toast.LENGTH_LONG
                            ).show()

                            EspressoIdlingResource.decrement()
                        }
                        is Resource.Error -> Toast.makeText(
                            activity,
                            "Failed to update alarm time... Please try again",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                loadNewRewardedAd()
            }

        }
    }
}

sealed class AdState() {
    object Loading : AdState()
    object Ready : AdState()
    object Failed : AdState()
}