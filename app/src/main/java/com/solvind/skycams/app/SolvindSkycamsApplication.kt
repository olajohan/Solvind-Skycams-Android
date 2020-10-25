package com.solvind.skycams.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class SolvindSkycamsApplication : Application() {

    override fun onCreate() {
        Timber.plant(Timber.DebugTree())
        super.onCreate()

        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)

    }


}