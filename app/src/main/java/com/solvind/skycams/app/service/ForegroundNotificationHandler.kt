package com.solvind.skycams.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import coil.request.ErrorResult
import coil.request.SuccessResult
import com.google.firebase.storage.FirebaseStorage
import com.solvind.skycams.app.R
import com.solvind.skycams.app.core.*
import com.solvind.skycams.app.di.AuroraAlarmAppspot
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import io.github.rosariopfernandes.firecoil.FireCoil
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import javax.inject.Inject

@ExperimentalCoroutinesApi
@ServiceScoped
class ForegroundNotificationHandler @Inject constructor(
    @ApplicationContext private val context: Context,
    @AuroraAlarmAppspot private val mAppStorage: FirebaseStorage
) {

    private val mForegroundNotification = MutableStateFlow<Notification?>(null)
    val foregroundNotification = mForegroundNotification as StateFlow<Notification?>

    private val mActivatedAlarmsMap = mutableMapOf<String, AlarmStatus.Activated>()
    private var mSelectedKey: String? = null

    init {
        createForegroundNotificationChannel()
    }

    suspend fun updateAlarmList(alarmStatus: AlarmStatus) {
        when (alarmStatus) {
            is AlarmStatus.Activated -> addOrUpdateList(alarmStatus)
            else -> removeFromList(alarmStatus)
        }
        updateForegroundNotification()
    }

    private fun addOrUpdateList(alarmStatus: AlarmStatus.Activated) {
        mActivatedAlarmsMap[alarmStatus.ac.skycamKey] = alarmStatus
    }

    private fun removeFromList(alarmStatus: AlarmStatus) {
        mActivatedAlarmsMap.remove(alarmStatus.alarmConfig.skycamKey)
    }

    suspend fun selectNextFromList() {
        val nextIndex = mActivatedAlarmsMap.keys.indexOf(mSelectedKey) + 1
        mSelectedKey = mActivatedAlarmsMap.keys.toList().getOrNull(nextIndex)
        updateForegroundNotification()
    }

    suspend fun selectPreviousFromList() {
        val nextIndex = mActivatedAlarmsMap.keys.indexOf(mSelectedKey) - -1
        mSelectedKey = mActivatedAlarmsMap.keys.toList().getOrNull(nextIndex)
        updateForegroundNotification()
    }

    private suspend fun updateForegroundNotification() {

        if (mActivatedAlarmsMap.isEmpty()) {
            mForegroundNotification.value = null
            return
        }

        val activeAlarm = mActivatedAlarmsMap[mSelectedKey] ?: mActivatedAlarmsMap.values.first()

        val notificationBuilder = NotificationCompat.Builder(context, SERVICE_FOREGROUND_NOTIFICATIONS_CHANNEl_ID)

        val skycam = activeAlarm.skycam
        notificationBuilder.apply {
            setStyle(androidx.media.app.NotificationCompat.MediaStyle())
            setContentTitle("Searching for northern lights")
            setContentText("Skycam: ${skycam.location.name}")
            setSmallIcon(R.drawable.ic_solvindhvit)
            addAction(R.drawable.ic_baseline_navigate_before_24, "Previous", getSelectPreviousPendingIntent())
            addAction(R.drawable.ic_baseline_stop_24, "Stop", getStopSingleAlarmPendingIntent(skycam.skycamKey))
            addAction(R.drawable.ic_baseline_navigate_next_24, "Next", getSelectNextPendingIntent())
        }

        /**
         * Try downloading the main image from the skycam location
         * */
        val mainImageRef = mAppStorage.getReferenceFromUrl(skycam.mainImage)

        when (val mainImageLoadingResult = FireCoil.get(context, mainImageRef)) {
            is SuccessResult -> notificationBuilder.setLargeIcon(mainImageLoadingResult.drawable.toBitmap())
            is ErrorResult -> Timber.i("Error while loading main image: ${mainImageLoadingResult.throwable.message}")
        }

        mForegroundNotification.value = notificationBuilder.build()
    }


    private fun getSelectNextPendingIntent() =
        Intent(context, AlarmServiceImpl::class.java).let { selectNextIntent ->
            selectNextIntent.action = SERVICE_SELECT_NEXT_ALARM_ACTION
            PendingIntent.getService(
                context,
                SERVICE_SELECT_NEXT_ALARM_REQUEST_CODE,
                selectNextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

    private fun getSelectPreviousPendingIntent() =
        Intent(context, AlarmServiceImpl::class.java).let { selectPreviousIntent ->
            selectPreviousIntent.action = SERVICE_SELECT_PREVIOUS_ALARM_ACTION
            PendingIntent.getService(
                context,
                SERVICE_SELECT_PREVIOUS_ALARM_REQUEST_CODE,
                selectPreviousIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

    private fun getStopSingleAlarmPendingIntent(skycamKey: String) =
        Intent(context, AlarmServiceImpl::class.java).let { cancelSingleAlarmIntent ->
            cancelSingleAlarmIntent.action = SERVICE_CANCEL_SINGLE_ALARM_ACTION
            cancelSingleAlarmIntent.putExtra(INTENT_EXTRA_SKYCAMKEY, skycamKey)

            PendingIntent.getService(
                context,
                SERVICE_CANCEL_SINGLE_ALARM_REQUEST_CODE,
                cancelSingleAlarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

    /**
     * Creates the notification channel used by the foreground service that listens to skycam updates.
     * We will use a different channel for alerting the user about northern lights.
     *
     * This channel should not be used to disturb the user and thereby have the priority set to LOW
     * */
    private fun createForegroundNotificationChannel() {
        val name = context.getString(R.string.foreground_notification_channel_name)
        val descriptionText =
            context.getString(R.string.foreground_notification_channel_description)
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(
            SERVICE_FOREGROUND_NOTIFICATIONS_CHANNEl_ID,
            name,
            importance
        ).apply { description = descriptionText }
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

