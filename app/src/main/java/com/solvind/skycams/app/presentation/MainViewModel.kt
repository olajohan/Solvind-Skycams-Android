package com.solvind.skycams.app.presentation

import android.app.Activity
import android.app.Application
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.solvind.skycams.app.core.IExposeNetworkStateLiveData
import com.solvind.skycams.app.core.InternetConnection
import com.solvind.skycams.app.domain.usecases.UpdateAlarmTimeUseCase
import kotlinx.coroutines.launch

/**
 * Responsible for:
 * - loading ads
 * - Rewarding user after showing a rewarded ad
 * - Exposing network state as livedata (should not be used for single events)
 * */
class MainViewModel @ViewModelInject constructor(
    private val app: Application,
    private val updateAlarmTimeUseCase: UpdateAlarmTimeUseCase
) : AndroidViewModel(app), IProvideRewardedAds, IExposeNetworkStateLiveData {

    private lateinit var mRewardedAd: RewardedAd

    /**
     * The connectivity manager will be used to register a network callback.
     * */
    private val mConnectivityManager = app.getSystemService(ConnectivityManager::class.java)

    /**
     * Keeps track of the loading state of the rewarded ad.
     * */
    private val mRewardedAdState = MutableLiveData<RewardedAdState>()
    override val rewardedAdState = mRewardedAdState as LiveData<RewardedAdState>

    /**
     * Exposes the devices connection type as a livedata.
     * */
    private val mInternetConnectionType = MutableLiveData<InternetConnection>()
    override val internetConnectionType = mInternetConnectionType as LiveData<InternetConnection>

    /**
     * Registers to the activities onResume/onPause methods and loads a new ad as soon as the network is available.
     * It will only load the ad if it hasn't already been loaded.
     * The actual action of loading the ad is performed in the loadNewRewarded at method.
     *
     * IMPORTANT!
     * We need the user to first consent to be shown ads. This is happening in the init activity at boot. The user should
     * not be able to open the main activity before we have the consent.
     * */
    private val mNetworkCallback = object : ConnectivityManager.NetworkCallback() {

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)

            /**
             * Determine if we have an active internet connection. We need to do the check on the
             * network capabilities instead of onAvailable since the latter will fire regardless
             * of if the network is connected to the internet or not.
             * */
            if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {

                /**
                 * set connection to connected
                 * */
                viewModelScope.launch { mInternetConnectionType.postValue(InternetConnection.Connected) }


                /**
                 * Once we know for sure we have a working internet connection, download a fresh ad.
                 * This method is safe to call since it will not load a new ad if it already has a
                 * ad loaded.
                 * */
                loadNewRewardedAd()

            } else {
                viewModelScope.launch { mInternetConnectionType.value = InternetConnection.NotConnected }
            }
        }
    }

    /**
     * Registers the mNetwork callback to onPause/ onResume
     * */
    private val mActivityLifeCycleCallback = object:  Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
        override fun onActivityStarted(activity: Activity) {}
        override fun onActivityResumed(activity: Activity) = mConnectivityManager.registerDefaultNetworkCallback(mNetworkCallback)
        override fun onActivityPaused(activity: Activity) = mConnectivityManager.unregisterNetworkCallback(mNetworkCallback)
        override fun onActivityStopped(activity: Activity) {}
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        override fun onActivityDestroyed(activity: Activity) {}
    }

    /**
     * Registers the activity lifecycle callbacks
     * */
    init {
        app.registerActivityLifecycleCallbacks(mActivityLifeCycleCallback)
    }

    /**
     * Loads a new ad if mRewardedAd has not been initialized or the mRewardedAd is not in a loaded state
     * */
    private fun loadNewRewardedAd() = viewModelScope.launch {
        if (!this@MainViewModel::mRewardedAd.isInitialized || !mRewardedAd.isLoaded) {
            mRewardedAdState.value = RewardedAdState.Loading
            mRewardedAd = RewardedAd(app, "ca-app-pub-8129908873162978/5890143437")

            val adLoadCallback = object : RewardedAdLoadCallback() {
                override fun onRewardedAdLoaded() {
                    mRewardedAdState.value = RewardedAdState.Ready(mRewardedAd)
                }

                override fun onRewardedAdFailedToLoad(adError: LoadAdError) {
                    mRewardedAdState.value = RewardedAdState.Failure(adError)
                }
            }
            mRewardedAd.loadAd(AdRequest.Builder().build(), adLoadCallback)
        }
    }

    /**
     * Rewards the user with the alarmtime
     * */
    override fun rewardUserAlarmTime(skycamKey: String, plusEpochSeconds: Long) = viewModelScope.launch {
        updateAlarmTimeUseCase(this, UpdateAlarmTimeUseCase.Params(skycamKey, plusEpochSeconds))
        loadNewRewardedAd()
    }
}

sealed class RewardedAdState() {
    object Loading : RewardedAdState()
    data class Ready(val ad: RewardedAd) : RewardedAdState()
    data class Failure(val adError: LoadAdError) : RewardedAdState()
}