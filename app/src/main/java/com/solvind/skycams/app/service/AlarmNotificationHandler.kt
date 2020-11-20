package com.solvind.skycams.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.media.AudioAttributes
import android.net.Uri
import com.solvind.skycams.app.R
import com.solvind.skycams.app.core.SERVICE_ALARM_NOTIFICATIONS_CHANNEL_ID
import com.solvind.skycams.app.domain.model.Skycam
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AlarmNotificationHandler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    init {
        createAlarmNotificationChannel()
    }

    /**
     * Sends a alarm notification to the users notification tray and plays the users preferred alarm
     * sound. After the notification has been sent we cancel the user's alarm.
     *
     * The notification features:
     * - Name of the skycam who triggered the alarm
     * - Image from the skycam
     * - Alarm sound
     * - Time
     * - Action button to reactivate the alarm from the notification
     *
     * When the user clicks the notification he/she will be taken to a alarm fragment showing information about the image
     *
     * */
    private fun showAlarmNotification(skycam: Skycam) {}

    private fun getAlarmNotificationContentPendingIntent() {}

    /**
     * Creates the notification channel used to alarm the user when northern lights appears.
     *
     * This channel should get the users attention asap.
     * */
    private fun createAlarmNotificationChannel() {
        val name = context.getString(R.string.alarm_notifications_name)
        val descriptionText = context.getString(R.string.alarm_notifications_description)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(SERVICE_ALARM_NOTIFICATIONS_CHANNEL_ID, name, importance).apply {
            description = descriptionText
            setSound(
                Uri.parse(""),
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            enableVibration(true)
            enableLights(true)
            lightColor = Color.GREEN
        }
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}