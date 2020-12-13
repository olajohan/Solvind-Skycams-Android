package com.solvind.skycams.app.presentation

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.solvind.skycams.app.R
import com.solvind.skycams.app.core.*
import com.solvind.skycams.app.domain.model.Skycam
import com.solvind.skycams.app.presentation.ads.RewardedAdActivity
import com.solvind.skycams.app.presentation.single.SingleSkycamFragmentDirections
import com.solvind.skycams.app.service.AlarmServiceImpl
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Responsibilities:
 * - Start and bind/unbind from alarm service
 * - Setup navigation (navcontroller)
 * - Set content
 * */
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private lateinit var mService: AlarmServiceImpl

    /**
     * We are binding to the service just so we can quickly shut it down
     * when the activity is no longer in the foreground. If the service is monitoring
     * skycam flows, it will stay running in he foreground
     * */
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AlarmServiceImpl.LocalBinder
            mService = binder.getService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {}
    }


    /**
     * - Setup navigation
     * - Initialize the connectivity manager
     * - Initialize the connection error snackbar
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        bottom_nav.setupWithNavController(navController)
    }

    /**
     * Start and bind to the alarm service. We only bind to the service in order to notify it
     * when the activity is started and stopped (bound and unbound). The service shall otherwise
     * be decoupled from android components.
     * */
    override fun onStart() {
        super.onStart()
        Intent(this, AlarmServiceImpl::class.java).also {
            startService(it)
            bindService(it, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        intent?.let {

            when (it.action) {
                ACTIVITY_OPEN_SINGLE_SKYCAM_ACTION -> {
                    val skycamKey = it.extras?.getString(INTENT_EXTRA_SKYCAMKEY)
                    val skycamName = it.extras?.getString(INTENT_EXTRA_SKYCAM_NAME)
                    val skycamMainImage = it.extras?.getString(INTENT_EXTRA_SKYCAM_MAIN_IMAGE)

                    if (skycamKey != null && skycamName != null && skycamMainImage != null) {
                        navigateToSingleSkycam(skycamKey, skycamName, skycamMainImage)
                    }
                }
            }
        }
    }

    fun onClickWatchAdButton(view: View) {
        view.tag?.let { tag ->
            if (tag is String) {
                watchAdForReward(tag)
            }
        }
    }

    fun watchAdForReward(skycamKey: String) =
        Intent(this, RewardedAdActivity::class.java).apply {
            action = ACTIVITY_WATCH_AD_INTENT_ACTION
            putExtra(INTENT_EXTRA_SKYCAMKEY, skycamKey)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(this)


        }


    fun onClickNavigateToSingleSkycam(view: View) {
        view.tag.let { tag ->
            when (tag) {
                is Skycam -> {
                    navigateToSingleSkycam(tag.skycamKey, tag.location.name, tag.mainImage)
                }
            }
        }
    }

    private fun navigateToSingleSkycam(
        skycamKey: String,
        skycamName: String,
        skycamMainImage: String
    ) {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navHostFragment.navController.navigate(
            SingleSkycamFragmentDirections.actionNavigateToSingle(
                skycamKey,
                skycamName,
                skycamMainImage
            )
        )
    }

    /**
     * Unbind from the alarm service
     * */
    override fun onStop() {
        super.onStop()
        unbindService(serviceConnection)
    }
}