package com.solvind.skycams.app.presentation.ads

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.solvind.skycams.app.R
import com.solvind.skycams.app.core.ACTIVITY_WATCH_AD_INTENT_ACTION
import com.solvind.skycams.app.core.INTENT_EXTRA_SKYCAMKEY
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber

/**
 * 1. Displays a rewarded ad to the user
 * 2.
 * */
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class RewardedAdActivity : AppCompatActivity(R.layout.activity_ad) {

    private val mViewModel by viewModels<RewardedAdViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mViewModel.userRewardEvent.observe(this) { userRewardEvent ->

            when (userRewardEvent) {
                UserRewardEvent.Waiting -> Timber.i("Waiting while ad is showing")

                is UserRewardEvent.UserRewardedSuccesfully -> Toast.makeText(
                    this,
                    "Congratulations! You have received ${userRewardEvent.alarmMinutesReward} minutes of alarm time!",
                    Toast.LENGTH_LONG).show()

                is UserRewardEvent.UserNotRewarded -> Toast.makeText(
                    this,
                    "Something wen't wrong while updating alarm: ${userRewardEvent.failure}",
                    Toast.LENGTH_LONG
                ).show()
            }

        }

        mViewModel.rewardedAdStatus.observe(this) { rewardedAdStatus ->
            when (rewardedAdStatus) {

                RewardedAdStatus.Loading -> Timber.i("Ad is loading")
                is RewardedAdStatus.Error -> Timber.i("Error loading ad. ${rewardedAdStatus.loadError}")

                is RewardedAdStatus.Ready -> {
                    val rewardedAd = rewardedAdStatus.rewardedAd

                    rewardedAd.show(this, object : RewardedAdCallback() {
                        override fun onUserEarnedReward(rewardedItem: RewardItem) {
                            when (intent.action) {
                               ACTIVITY_WATCH_AD_INTENT_ACTION -> {
                                   intent?.getStringExtra(INTENT_EXTRA_SKYCAMKEY)?.let { skycamKey ->
                                       mViewModel.rewardUser(skycamKey, rewardedItem.amount)
                                   }
                               }
                            }
                        }

                        /*
                        * Close the activity when the user has clicked the close button on the rewarded ad
                        * */
                        override fun onRewardedAdClosed() {
                            super.onRewardedAdClosed()
                            finish()
                        }

                        override fun onRewardedAdFailedToShow(p0: AdError?) {
                            super.onRewardedAdFailedToShow(p0)

                        }
                    })
                }
            }
        }
    }
}