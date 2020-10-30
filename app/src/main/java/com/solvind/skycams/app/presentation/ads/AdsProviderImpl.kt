package com.solvind.skycams.app.presentation.ads

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LifecycleCoroutineScope
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.solvind.skycams.app.core.Resource
import com.solvind.skycams.app.domain.model.Skycam
import com.solvind.skycams.app.domain.usecases.UpdateAlarmTimeUseCase
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
private const val PRODUCTION_AD_UNIT_ID = "ca-app-pub-8129908873162978/5890143437"

@ActivityScoped
class AdsProviderImpl @Inject constructor(
    @ActivityContext private val context: Context,
    private val updateAlarmTimeUseCase: UpdateAlarmTimeUseCase
): IAdsProvider {

    private var mDefaultRewardedAd: RewardedAd

    init {
        mDefaultRewardedAd = createAndLoadRewardedAd()
    }

    private fun createAndLoadRewardedAd(): RewardedAd {
        val rewardedAd = RewardedAd(context, PRODUCTION_AD_UNIT_ID)
        rewardedAd.loadAd(AdRequest.Builder().build(), object : RewardedAdLoadCallback() {
            override fun onRewardedAdLoaded() {
                Timber.i("Rewarded ad loaded")
                super.onRewardedAdLoaded()
            }

            override fun onRewardedAdFailedToLoad(p0: LoadAdError?) {
                Timber.i("Error: $p0")
            }
        })
        return rewardedAd
    }

    override fun showNextRewardedAd(activity: Activity, lifecycleScope: LifecycleCoroutineScope, skycam: Skycam) {
        if (!mDefaultRewardedAd.isLoaded) return
        mDefaultRewardedAd.show(activity, object : RewardedAdCallback() {
            override fun onUserEarnedReward(rewardItem: RewardItem) {
                Timber.i("Reward ${rewardItem.type} Amount: ${rewardItem.amount.toLong()}")
                lifecycleScope.launch(Dispatchers.IO) {
                    updateAlarmTimeUseCase(
                        this,
                        UpdateAlarmTimeUseCase.Params(
                            skycam.skycamKey,
                            TimeUnit.MINUTES.toSeconds(rewardItem.amount.toLong())
                        )
                    ) {
                        when (it) {
                            is Resource.Success -> {
                            }
                            is Resource.Error -> {
                                Toast.makeText(
                                    activity,
                                    "Error: ${it.failure}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }

            override fun onRewardedAdClosed() {
                mDefaultRewardedAd = createAndLoadRewardedAd()
            }
        })
    }

}