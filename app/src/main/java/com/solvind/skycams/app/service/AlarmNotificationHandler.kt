package com.solvind.skycams.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.graphics.Color
import android.media.AudioAttributes
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import coil.request.ErrorResult
import coil.request.SuccessResult
import com.google.firebase.storage.FirebaseStorage
import com.solvind.skycams.app.R
import com.solvind.skycams.app.core.SERVICE_ALARM_NOTIFICATIONS_CHANNEL_ID
import com.solvind.skycams.app.di.SkycamImages
import com.solvind.skycams.app.domain.model.Skycam
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import io.github.rosariopfernandes.firecoil.FireCoil
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@ServiceScoped
class AlarmNotificationHandler @Inject constructor(
    @ApplicationContext private val context: Context,
    @SkycamImages private val mSkycamImagesStorage: FirebaseStorage
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
    suspend fun showAlarmNotification(skycam: Skycam) {

        val notificationBuilder = NotificationCompat.Builder(
            context,
            SERVICE_ALARM_NOTIFICATIONS_CHANNEL_ID
        )
        val timestampMilli = TimeUnit.SECONDS.toMillis(skycam.mostRecentImage.timestamp)
        notificationBuilder.apply {
            setSmallIcon(R.drawable.ic_solvindhvit)
            setContentTitle("Aurora detected at ${skycam.location.name}!")
            setWhen(timestampMilli)
            setOnlyAlertOnce(true)

        }

        val detectedAuroraImageStorageRef = mSkycamImagesStorage.getReferenceFromUrl(skycam.mostRecentImage.storageLocation)

        when (val detectedImageLoadingResult = FireCoil.get(context, detectedAuroraImageStorageRef)) {
            is SuccessResult -> {
                val image = detectedImageLoadingResult.drawable.toBitmap()
                notificationBuilder.setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(image)
                        .bigLargeIcon(null)
                )
            }
            is ErrorResult -> Timber.i("Error while loading main image: ${detectedImageLoadingResult.throwable.message}")
        }

        context.getSystemService(NotificationManager::class.java).notify(
            skycam.skycamKey.hashCode(),
            notificationBuilder.build()
        )
    }

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
                Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.applicationContext.packageName + "/" + R.raw.number_2),
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            enableVibration(true)
            vibrationPattern = longArrayOf(100, 500, 100, 500, 100, 500, 100 ,500, 100, 500)
            enableLights(true)
            lightColor = Color.GREEN
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}