package com.solvind.skycams.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.media.AudioAttributes
import android.net.Uri
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

const val FOREGROUND_NOTIFICATIONS_CHANNEl_ID = "foreground_notification"
const val ALARM_NOTIFICATIONS_CHANNEL_ID = "alarm_notification"

@HiltAndroidApp
class SolvindSkycamsApplication : Application() {

    override fun onCreate() {
        Timber.plant(Timber.DebugTree())
        createForegroundNotificationChannel()
        createAlarmNotificationChannel()
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

    /**
     * Creates the notification channel used to alarm the user when northern lights appears.
     *
     * This channel should get the users attention asap.
     * */
    private fun createAlarmNotificationChannel() {
        val name = getString(R.string.alarm_notifications_name)
        val descriptionText = getString(R.string.alarm_notifications_description)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(ALARM_NOTIFICATIONS_CHANNEL_ID, name, importance).apply {
            description = descriptionText
            setSound(
                Uri.parse("android.resource://${packageName}/${R.raw.number_2}"),
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            enableVibration(true)
            enableLights(true)
            lightColor = Color.GREEN
        }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

}