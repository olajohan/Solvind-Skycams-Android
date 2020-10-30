package com.solvind.skycams.app.presentation.ads

import android.app.Activity
import androidx.lifecycle.LifecycleCoroutineScope
import com.solvind.skycams.app.domain.model.Skycam

interface IAdsProvider {
    fun showNextRewardedAd(activity: Activity, lifecycleScope: LifecycleCoroutineScope, skycam: Skycam)
}