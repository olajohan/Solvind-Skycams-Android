package com.solvind.skycams.app.presentation.ads

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.solvind.skycams.app.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class RewardedAdLoaderTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Test
    fun flow_emits_loading_status() = runBlockingTest {
        val adLoader = RewardedAdLoader(ApplicationProvider.getApplicationContext())
        val rewardedAdStatus = adLoader.getRewardedAdStatusFlow().take(1).first()
        assertThat(rewardedAdStatus is RewardedAdStatus.Loading)
    }

    @Test
    fun load_ad_success() = runBlockingTest {
        val adLoader = RewardedAdLoader(ApplicationProvider.getApplicationContext())
        adLoader.loadRewardedAd()
        assertThat(adLoader.isAdLoaded())
    }

}