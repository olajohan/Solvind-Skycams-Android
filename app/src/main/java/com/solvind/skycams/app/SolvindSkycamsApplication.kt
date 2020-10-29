package com.solvind.skycams.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

const val FOREGROUND_NOTIFICATIONS_CHANNEl_ID = "foreground_notification"

@HiltAndroidApp
class SolvindSkycamsApplication : Application() {

    override fun onCreate() {
        Timber.plant(Timber.DebugTree())
        createForegroundNotificationChannel()
        super.onCreate()
    }

    /**
     * Creates the notification channel used by the foreground service that listens to skycam updates.
     * We will use a different channel for alerting the user about northern lights.
     *
     * This channel should not be used to disturb the user and thereby have the priority set to LOW
     * */
    private fun createForegroundNotificationChannel() {
        val name = getString(R.string.foreground_notification_channel_name)
        val descriptionText = getString(R.string.foreground_notification_channel_description)
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(FOREGROUND_NOTIFICATIONS_CHANNEl_ID, name, importance).apply { description = descriptionText }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }


}