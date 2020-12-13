package com.solvind.skycams.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import coil.request.ErrorResult
import coil.request.SuccessResult
import com.google.firebase.storage.FirebaseStorage
import com.solvind.skycams.app.R
import com.solvind.skycams.app.core.*
import com.solvind.skycams.app.di.AuroraAlarmAppspot
import com.solvind.skycams.app.domain.model.AlarmConfig
import com.solvind.skycams.app.domain.usecases.skycam.GetSkycamUseCase
import com.solvind.skycams.app.presentation.ads.RewardedAdActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import io.github.rosariopfernandes.firecoil.FireCoil
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@ExperimentalCoroutinesApi
@ServiceScoped
class AlarmTimedoutNotificationHandler @Inject constructor(
    @ApplicationContext private val mContext: Context,
    @AuroraAlarmAppspot private val mMainStorage: FirebaseStorage,
    private val mGetSkycamUseCase: GetSkycamUseCase
) {


    init {
        createTimedoutNotificationChannel()
    }

    suspend fun showTimeoutNotification(alarmConfig: AlarmConfig) {
        val notificationBuilder = NotificationCompat.Builder(
            mContext,
            SERVICE_ALARM_NOTIFICATIONS_CHANNEL_ID
        )

        notificationBuilder.apply {
            setSmallIcon(R.drawable.ic_solvindhvit)
            setContentTitle("Alarm timeout!")
            setAutoCancel(true)
            setWhen(TimeUnit.SECONDS.toMillis(alarmConfig.alarmAvailableUntilEpochSeconds))
            setOnlyAlertOnce(true)
            setContentIntent(getWatchAdPendingIntent(alarmConfig.skycamKey))
        }

        when (val result = mGetSkycamUseCase.run(GetSkycamUseCase.Params(alarmConfig.skycamKey))) {
            is Resource.Error -> {
                Timber.i("Failed to fetch skycam")
                Timber.e(result.failure.toString())
            }
            is Resource.Success -> {
                val skycam = result.value

                val mainImageRef = mMainStorage.getReferenceFromUrl(skycam.mainImage)
                when (val detectedImageLoadingResult = FireCoil.get(mContext, mainImageRef)) {
                    is SuccessResult -> {
                        val image = detectedImageLoadingResult.drawable.toBitmap()
                        notificationBuilder.setStyle(
                            NotificationCompat.BigTextStyle()
                                .bigText("The alarm at ${skycam.location.name} has timedout. Click this notification to watch an ad and gain more alarm minutes.")
                        )
                        notificationBuilder.setLargeIcon(image)
                    }
                    is ErrorResult -> Timber.i("Error while loading main image: ${detectedImageLoadingResult.throwable.message}")
                }

                mContext.getSystemService(NotificationManager::class.java).notify(
                    skycam.skycamKey.hashCode(),
                    notificationBuilder.build()
                )
            }
        }
    }

    private fun getWatchAdPendingIntent(skycamKey: String) =

        Intent(mContext, RewardedAdActivity::class.java).let { watchAdIntent ->

            watchAdIntent.apply {
                action = ACTIVITY_WATCH_AD_INTENT_ACTION
                putExtra(INTENT_EXTRA_SKYCAMKEY, skycamKey)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            PendingIntent.getActivity(
                mContext,
                ACTIVITY_WATCH_AD_REQUEST_CODE,
                watchAdIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

    private fun createTimedoutNotificationChannel() {
        val name = mContext.getString(R.string.timeout_notification_title)
        val descriptionText = mContext.getString(R.string.timeout_notification_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT

        val channel = NotificationChannel(SERVICE_ALARM_TIMEOUT_NOTIFICATION_CHANNEL_ID, name, importance).apply {
            description = descriptionText
            setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
                    .build()
                )
            enableVibration(true)
            vibrationPattern = longArrayOf(500)
            enableLights(true)
            lightColor = Color.RED
        }

        val notificationManager = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}