package com.solvind.skycams.app

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.solvind.skycams.app.service.AlarmServiceImpl
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private lateinit var mService : AlarmServiceImpl


    /**
     * We are binding to the service just so we can quickly shut it down
     * when the activity is no longer in the foreground. If the service is monitoring
     * skycam flows, it will stay running in he foreground
     * */
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AlarmServiceImpl.LocalBinder
            mService = binder.getService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {}

    }

    /**
     * Setup nav controller
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        findViewById<BottomNavigationView>(R.id.bottom_nav).setupWithNavController(navController)

    }

    /**
     * Start alarm service
     * */
    override fun onStart() {
        super.onStart()
        Intent(this, AlarmServiceImpl::class.java).also {
            startService(it)
            bindService(it, connection, Context.BIND_AUTO_CREATE)
        }
    }

    /**
     * Unbind from the alarm service
     * */
    override fun onStop() {
        super.onStop()
        unbindService(connection)
    }
}