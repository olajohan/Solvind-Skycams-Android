package com.solvind.skycams.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import coil.request.ErrorResult
import coil.request.SuccessResult
import com.google.firebase.storage.FirebaseStorage
import com.solvind.skycams.app.R
import com.solvind.skycams.app.core.*
import com.solvind.skycams.app.di.AuroraAlarmAppspot
import com.solvind.skycams.app.di.SkycamImages
import com.solvind.skycams.app.domain.enums.AuroraPrediction
import com.solvind.skycams.app.domain.model.Skycam
import com.solvind.skycams.app.presentation.MainActivity
import com.solvind.skycams.app.presentation.ads.RewardedAdActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import io.github.rosariopfernandes.firecoil.FireCoil
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

/**
 * Responsible for creating a foreground notification displaying one active alarm at a time.
 *
 * It will keep a map of all the users active alarms. The foreground notification will have buttons
 * for the user to navigate next/previous in the map of active alarms. When the user navigates through the
 * active alarms we change the foreground notification to show the user the next/previous alarm in the map.
 *
 * It depends on the client to forward alarm and skycam updates to update the current foreground notification.
 *
 * If there are no active alarms the foreground notification [StateFlow] should be set to null
 *
 * */
@ExperimentalCoroutinesApi
@ServiceScoped
class ForegroundNotificationHandler @Inject constructor(
    @ApplicationContext private val context: Context,
    @AuroraAlarmAppspot private val mAppStorage: FirebaseStorage,
    @SkycamImages private val mSkycamImagesStorage: FirebaseStorage
) {

    private val mForegroundNotification = MutableStateFlow<Notification?>(null)
    val foregroundNotification = mForegroundNotification as StateFlow<Notification?>

    private val mMutex = Mutex()

    /**
     * Map of all the users active alarms.
     *
     * @MapKey unique skycamKey per skycam
     * @MapValue [AlarmStatusUpdate.Activated]
     * */
    private val mActivatedAlarmsMap = mutableMapOf<String, AlarmStatusUpdate.Activated>()

    private var mSelectedKey: String? = null
    private var mLiveView = false
    private var mLiveViewTimer: Job? = null

    init {
        createForegroundNotificationChannel()
    }

    suspend fun updateAlarmStatus(alarmStatusUpdate: AlarmStatusUpdate) {
        when (alarmStatusUpdate) {
            is AlarmStatusUpdate.Activated -> {
                addOrUpdateList(alarmStatusUpdate)
            }
            else -> removeFromList(alarmStatusUpdate)
        }
        Timber.i("Alarm update received: $alarmStatusUpdate")
        updateForegroundNotification()
    }

    /**
     * Updates a single skycam in the map access by a unique skycamKey
     *
     * @param predictedSkycamUpdate
     * */
    suspend fun updateActiveAlarm(predictedSkycamUpdate: PredictedSkycamUpdate) {
        val skycam = predictedSkycamUpdate.skycam
        val alarmConfig = predictedSkycamUpdate.alarmConfig
        addOrUpdateList(AlarmStatusUpdate.Activated(skycam, alarmConfig))
        if (skycam.skycamKey == mSelectedKey) {
            updateForegroundNotification()
        }
    }

    private suspend fun addOrUpdateList(alarmStatusUpdate: AlarmStatusUpdate.Activated) = mMutex.withLock {
        mActivatedAlarmsMap[alarmStatusUpdate.alarmConfig.skycamKey] = alarmStatusUpdate
    }

    private suspend fun removeFromList(alarmStatusUpdate: AlarmStatusUpdate) = mMutex.withLock {
        mActivatedAlarmsMap.remove(alarmStatusUpdate.alarmConfig.skycamKey)
    }

    /**
     * Changes the foreground notification to display the next activated skycam alarm
     * */
    suspend fun selectNextFromListAndUpdateForegroundNotification() = mMutex.withLock {
        val nextIndex = (mActivatedAlarmsMap.keys.indexOf(mSelectedKey) + 1).let {
            if (it >= mActivatedAlarmsMap.size) { 0 } else { it }
        }
        mSelectedKey = mActivatedAlarmsMap.keys.toList().getOrNull(nextIndex)
        Timber.i("Selected key: $mSelectedKey Index: $nextIndex")
        updateForegroundNotification()
    }

    /**
     * Changes the foreground notification to display the previous activated skycam alarm
     * */
    suspend fun selectPreviousFromListAndUpdateForegroundNotification() = mMutex.withLock {
        val previousIndex = (mActivatedAlarmsMap.keys.indexOf(mSelectedKey) -1).let {
            if (it < 0) { mActivatedAlarmsMap.keys.size - 1 } else { it }
        }
        mSelectedKey = mActivatedAlarmsMap.keys.toList().getOrNull(previousIndex)
        Timber.i("Selected key: $mSelectedKey Index: $previousIndex")
        updateForegroundNotification()
    }

    /**
     * Toggle if the foreground notification's image should display the main image of the skycam
     * or live updates.
     * */
    suspend fun toggleLiveView() {
        mLiveView = !mLiveView
        updateForegroundNotification()
        mLiveViewTimer?.cancelAndJoin()
        if (mLiveView) {
            mLiveViewTimer = CoroutineScope(coroutineContext).launch {
                Toast.makeText(context, "Live view activated for 2 minutes", Toast.LENGTH_SHORT).show()
                delay(120_000)
                toggleLiveView()
            }
        }
    }

    private suspend fun updateForegroundNotification() {

        if (mActivatedAlarmsMap.isEmpty()) {
            mForegroundNotification.value = null
            return
        }

        if (mSelectedKey == null) {
            mSelectedKey = mActivatedAlarmsMap.keys.first()
        }

        val activeAlarm = mActivatedAlarmsMap[mSelectedKey] ?: mActivatedAlarmsMap.values.first()
        val notificationBuilder = NotificationCompat.Builder(context, SERVICE_FOREGROUND_NOTIFICATIONS_CHANNEl_ID)
        val skycam = activeAlarm.skycam
        val alarmConfig = activeAlarm.alarmConfig

        /*
        * Used as seed for the chronometer countdown timer indicating how long the alarm will be active for
        * */
        val timeoutTimeStamp = TimeUnit.SECONDS.toMillis(alarmConfig.alarmAvailableUntilEpochSeconds)


        Timber.i("Most recent prediction ${skycam.mostRecentImage.prediction}")

        /*
        * The text that will be displayed beneath the title of the skycam
        * */
        val contentText = when(val prediction = skycam.mostRecentImage.prediction) {

            is AuroraPrediction.NotPredicted -> prediction.name
            is AuroraPrediction.NotAurora -> {
                String.format("%s: %.1f%s", prediction.name, prediction.confidence*100, "%")
            }
            is AuroraPrediction.VisibleAurora -> {
                String.format("%s: %.1f%s", prediction.name, prediction.confidence*100, "%")
            }

        }

        notificationBuilder.apply {
            setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)
            )
            setWhen(timeoutTimeStamp)
            setUsesChronometer(true)
            setContentTitle(skycam.location.name)
            setContentText(contentText)
            setSmallIcon(R.drawable.ic_solvindhvit)
            setContentIntent(getContentPendingIntent(skycam))
            setOngoing(true)
            setProgress(0, 0, true)
            addAction(
                R.drawable.ic_baseline_navigate_before_24,
                "Previous",
                getSelectPreviousPendingIntent()
            )
            addAction(
                R.drawable.ic_baseline_stop_24,
                "Stop",
                getStopSingleAlarmPendingIntent(skycam.skycamKey)
            )
            addAction(
                R.drawable.ic_baseline_navigate_next_24,
                "Next",
                getSelectNextPendingIntent()
            )
            addAction(
                R.drawable.ic_baseline_more_time_24,
                "Watch ad",
                getWatchAdPendingIntent(skycam.skycamKey)
            )
            addAction(
                R.drawable.ic_baseline_preview_24,
                "Live",
                getToggleLiveViewPendingIntent()
            )
        }

        val mainImageRef = if (mLiveView) {
            mSkycamImagesStorage.getReferenceFromUrl(skycam.mostRecentImage.storageLocation)
        } else {
            mAppStorage.getReferenceFromUrl(skycam.mainImage)
        }

        when (val mainImageLoadingResult = FireCoil.get(context, mainImageRef)) {
            is SuccessResult -> {
                notificationBuilder.setLargeIcon(mainImageLoadingResult.drawable.toBitmap())
            }
            is ErrorResult -> Timber.i("Error while loading main image: ${mainImageLoadingResult.throwable.message}")
        }

        mForegroundNotification.value = notificationBuilder.build()
    }

    private fun getContentPendingIntent(skycam: Skycam) =
        Intent(context, MainActivity::class.java).let { contentIntent ->
            contentIntent.action = ACTIVITY_OPEN_SINGLE_SKYCAM_ACTION
            contentIntent.putExtra(INTENT_EXTRA_SKYCAMKEY, skycam.skycamKey)
            contentIntent.putExtra(INTENT_EXTRA_SKYCAM_NAME, skycam.location.name)
            contentIntent.putExtra(INTENT_EXTRA_SKYCAM_MAIN_IMAGE, skycam.mainImage)

            contentIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            PendingIntent.getActivity(
                context,
                ACTIVITY_OPEN_SINGLE_SKYCAM_REQUEST_CODE,
                contentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
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

    private fun getWatchAdPendingIntent(skycamKey: String) =
        Intent(context, RewardedAdActivity::class.java).let { watchAdIntent ->

            watchAdIntent.apply {
                action = ACTIVITY_WATCH_AD_INTENT_ACTION
                putExtra(INTENT_EXTRA_SKYCAMKEY, skycamKey)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            PendingIntent.getActivity(
                context,
                ACTIVITY_WATCH_AD_REQUEST_CODE,
                watchAdIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

    private fun getToggleLiveViewPendingIntent() =
        Intent(context, AlarmServiceImpl::class.java).let { toggleLiveViewIntent ->
            toggleLiveViewIntent.action = SERVICE_TOGGLE_LIVE_VIEW_ACTION

            PendingIntent.getService(
                context,
                SERVICE_TOGGLE_LIVE_VIEW_REQUEST_CODE,
                toggleLiveViewIntent,
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

