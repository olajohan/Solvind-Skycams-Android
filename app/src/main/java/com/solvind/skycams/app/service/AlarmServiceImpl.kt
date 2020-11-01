package com.solvind.skycams.app.service

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import coil.request.ErrorResult
import coil.request.SuccessResult
import com.google.firebase.storage.FirebaseStorage
import com.solvind.skycams.app.ALARM_NOTIFICATIONS_CHANNEL_ID
import com.solvind.skycams.app.FOREGROUND_NOTIFICATIONS_CHANNEl_ID
import com.solvind.skycams.app.R
import com.solvind.skycams.app.core.UseCase
import com.solvind.skycams.app.core.UseCaseFlow
import com.solvind.skycams.app.di.AuroraAlarmAppspot
import com.solvind.skycams.app.di.SkycamImages
import com.solvind.skycams.app.domain.enums.AuroraPredictionLabel
import com.solvind.skycams.app.domain.model.Alarm
import com.solvind.skycams.app.domain.model.Skycam
import com.solvind.skycams.app.domain.usecases.*
import com.solvind.skycams.app.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import io.github.rosariopfernandes.firecoil.FireCoil
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val FOREGROUND_NOTIFICATION_ID = 1
private const val SERVICE_CANCEL_ALL_ALARM_LISTENERS_EXTRA_KEY = "clear_all_alarms"
private const val SERVICE_REACTIVATE_ALARM_EXTRA_KEY = "reactivate_alarm"
private const val SERVICE_CLEAR_ALL_ALARMS_REQUEST_CODE = 1
private const val SERVICE_REACTIVATE_ALARM_REQUEST_CODE = 2
private const val ACTIVITY_NO_EXTRAS_REQUEST_CODE = 3
private const val ACTIVITY_WITH_EXTRAS_KEY_STORAGE_LOCATION_REQUEST_CODE = 4

/**
 * Will also be used by the activity to retrieve the intent extras
 * */
const val ACTIVITY_INTENT_EXTRAS_STORAGE_LOCATION_KEY = "storage_location"
const val ACTIVITY_INTENT_EXTRAS_SKYCAMKEY_KEY = "skycamKey"

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AlarmServiceImpl : LifecycleService() {

    @Inject lateinit var mGetAllAlarmsFlowUseCase: com.solvind.skycams.app.domain.usecases.GetAllAlarmsFlowUseCase
    @Inject lateinit var mGetSkycamFlowUseCase: GetSkycamFlowUseCase
    @Inject lateinit var mActivateAlarmUseCase: ActivateAlarmUseCase
    @Inject lateinit var mDeactivateAlarmUseCase: DeactivateAlarmUseCase
    @Inject lateinit var mDeactivateAllAlarmsUseCase: DeactivateAllAlarmsUseCase
    @Inject lateinit var mGetSkycamUseCase: GetSkycamUseCase
    @Inject @SkycamImages lateinit var mSkycamImageStorage: FirebaseStorage
    @Inject @AuroraAlarmAppspot lateinit var mAppStorage: FirebaseStorage

    private lateinit var mUserAlarmsFlowsJob: Job
    private lateinit var mSkycamMergedFlowJob: Job

    /**
     * Synchronized list that holds all the users alarms. The user alarm listener is updating this list
     * eveverytime there is an update in the database. The list contains both inactive and active alarms.
     *
     *  The number of alarms in this list will not by default be the same as the number of skycam entries in the
     * database. Only the alarms the user has previously activated will be in this list.
     *
     * IMPORTANT!
     * This list must always be referenced from a synchronized(mCurrentAlarmList) {} block. The synchronized
     * block will use the list object itself as a lock.
     * */
    private val mCurrentAlarmsList = Collections.synchronizedList(mutableListOf<Alarm>())

    /**
     * Interface for communicating with bound components. This service will not make use of the binding
     * other than calling internal methods in onbBind/onUnbind
     * */
    private val binder = LocalBinder()

    /**
     * Starts the handler responsible for checking the alarmlist every second and deactivating the
     * users alarm if the alarm's availableUntilEpochSeconds in the past.
     *
     * Also starts the user alarm listener responsible for updating the alarm list whenever an
     * update happens in the database.
     * */
    override fun onCreate() {
        super.onCreate()
        startUserAlarmsListener()
        startAlarmValidatorHandler()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        /**
         * This intent is sent when the user clicks the CANCEL button in the foreground notification.
         * We the extra value to the key for SERVICE_CLEAR_ALL_ALARMS_EXTRA_KEY will always be true,
         * so we don't bother with any further checks than that the key exists in the Intent extras
         * before deactivating all alarms
         */
        intent?.extras?.get(SERVICE_CANCEL_ALL_ALARM_LISTENERS_EXTRA_KEY).let {
            if (it is Boolean && it) deactivateAllAlarms()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {

        synchronized(mCurrentAlarmsList) {
            /* If there are no active alarms when all components has unbound, stop the service */
            if (mCurrentAlarmsList.none { it.isActive }) stopSelf()
        }
        return true
    }

    /**
     * Starts/restarts listening for updates (inserts, modifications and deletions) on the users alarms.
     * The listener is attached/canceled in the services started/destroyed
     *
     * */
    private fun startUserAlarmsListener() {
        mUserAlarmsFlowsJob = lifecycleScope.launch {
            mGetAllAlarmsFlowUseCase.run(UseCaseFlow.None())
                .flowOn(Dispatchers.IO)
                .catch { exception ->
                    Timber.i(
                        """
                         ${exception.message}
                         ${exception.printStackTrace()}
                     """.trimIndent()
                    )
                }
                .onEmpty {
                    synchronized(mCurrentAlarmsList) { mCurrentAlarmsList.clear() }
                    handleAlarmListsUpdate()
                }
                .collect {
                    synchronized(mCurrentAlarmsList) {
                        mCurrentAlarmsList.clear()
                        mCurrentAlarmsList.addAll(it)
                    }
                    handleAlarmListsUpdate()
                }
        }
    }

    /**
     * Checks the alarm list every second and calls deactivateAlarmUseCase if the alarm is active
     * and has alarmAvailableUntilEpochSeconds in the past.
     * */
    private fun startAlarmValidatorHandler() = lifecycleScope.launch(Dispatchers.Default) {
        while (true) {
            synchronized(mCurrentAlarmsList) {
                mCurrentAlarmsList.forEach {

                    /**
                     * Sets the alarm to a deactivated state if the value of available until is in the past
                     * */
                    if (it.isActive && it.alarmAvailableUntilEpochSeconds < Instant.now().epochSecond)
                        mDeactivateAlarmUseCase(this, DeactivateAlarmUseCase.Params(it.skycamKey))
                }
            }
            delay(1000)
        }
    }


    /**
     * Sets a single alarms isActive to false in the database. This will fire of the user alarm listener
     * and adjust the list accordingly.
     * */
    private fun deactivateSingleAlarm(skycamKey: String) = lifecycleScope.launch(Dispatchers.IO) {
        mDeactivateAlarmUseCase(this, DeactivateAlarmUseCase.Params(skycamKey))
    }

    private fun deactivateAllAlarms() = lifecycleScope.launch(Dispatchers.IO) {
        Timber.i("DeactivateAllAlarms")
        mDeactivateAllAlarmsUseCase(this, UseCase.None()) {
            Timber.i("Result=$it")
        }

    }

    /** Called everytime the user makes an updates to h*'s alarms. We reset the alarm listening job
     * on each call. If the user's alarm list is empty we return before calling startSkycamUpdateListener.
     * */
    private fun handleAlarmListsUpdate() {

        /**
         * Whenever the alarm list changes we always cancels the current skycam listener job.
         * It will be reinitialized in the call to startSkycamUpdateListener if the alarm list is
         * not empty.
         *
         * */
        cancelSkycamUpdateListenerJob()

        val validAlarmsList = mutableListOf<Alarm>()
        synchronized(mCurrentAlarmsList) {

            validAlarmsList.addAll(
                mCurrentAlarmsList.filter { it.isActive && it.alarmAvailableUntilEpochSeconds > Instant.now().epochSecond }
            )
        }

        /**
         * If the user don't have any valid alarms, then remove the foreground notification and return
         * The alarms are deactivated by the alarmValidatorHandler who periodically checks for alarms
         * that has timed out.
         * */
        if (validAlarmsList.isEmpty()) {
            stopForeground(true)
            return
        }

        /**
         * Get a flow for all the users valid alarms
         * */
        val skycamFlowList = validAlarmsList.mapTo(mutableListOf()) {
            mGetSkycamFlowUseCase.run(GetSkycamFlowUseCase.Params(it.skycamKey))
        }

        Timber.i("Flow list size: ${skycamFlowList.size}")
        startSkycamUpdateListener(skycamFlowList)



        showForegroundNotification(validAlarmsList)



    }

    /**
     * Updates or removes the foreground notification depending on if the provided filtered list
     * is empty or contains active alarms.
     * */
    private fun showForegroundNotification(validAlarmsList: List<Alarm>) {

        val openActivityPendingIntent = getForegroundNotificationContentPendingIntent()
        val clearAllAlarmsPendingIntent = getClearAllAlarmsPendingIntent()

        /**
         * Get the alarm which will be the last to terminate due to timeout
         * */
        var longestAvailableAlarm = validAlarmsList.first()
        validAlarmsList.forEach {
            if (it.alarmAvailableUntilEpochSeconds > longestAvailableAlarm.alarmAvailableUntilEpochSeconds)
                longestAvailableAlarm = it
        }

        val now = Instant.now().epochSecond
        val timeout = (longestAvailableAlarm.alarmAvailableUntilEpochSeconds - now) + now
        Timber.i("Timeout: $timeout")

        val notification = NotificationCompat.Builder(this, FOREGROUND_NOTIFICATIONS_CHANNEl_ID)
            .setContentTitle("Aurora Alarm")
            .setContentText("${validAlarmsList.size} ${if (validAlarmsList.size == 1) "Skycam" else "Skycams"} Activated")
            .setUsesChronometer(true)
            .setWhen(TimeUnit.SECONDS.toMillis(timeout))
            .setSmallIcon(R.drawable.ic_solvind)
            .setContentIntent(openActivityPendingIntent)
            .setColorized(true)
            .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
            .addAction(
                NotificationCompat.Action.Builder(
                    R.drawable.ic_baseline_cancel_24,
                    getString(R.string.cancel_all_alarm_listeners_text),
                    clearAllAlarmsPendingIntent
                ).build()
            )
            .build()

        startForeground(FOREGROUND_NOTIFICATION_ID, notification)
    }

    private fun getForegroundNotificationContentPendingIntent(): PendingIntent? {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(
            this,
            ACTIVITY_NO_EXTRAS_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_ONE_SHOT
        )
    }

    private fun getClearAllAlarmsPendingIntent(): PendingIntent? {
        val intent = Intent(this, AlarmServiceImpl::class.java).apply {
            putExtra(SERVICE_CANCEL_ALL_ALARM_LISTENERS_EXTRA_KEY, true)
        }
        return PendingIntent.getService(
            this,
            SERVICE_CLEAR_ALL_ALARMS_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_NO_CREATE
        )
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
    private fun showAlarmNotification(skycam: Skycam) = lifecycleScope.launch {

        val openActivityPendingIntent = getAlarmNotificationContentPendingIntent(
            skycam.mostRecentImage.storageLocation,
            skycam.skycamKey
        )
        val reactivateAlarmPendingIntent = getReactivateAlarmPendingIntent(skycam.skycamKey)
        val soundUri = Uri.parse("android.resource://${packageName}/${R.raw.number_2}")
        val notificationBuilder =
            NotificationCompat.Builder(this@AlarmServiceImpl, ALARM_NOTIFICATIONS_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_solvind)
                .setContentTitle("Aurora at ${skycam.location.name}")
                .setContentText("Confidence: ${skycam.mostRecentImage.predictionConfidence}")
                .setContentIntent(openActivityPendingIntent)
                .setOnlyAlertOnce(true)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setVibrate(longArrayOf(500L, 500L, 500L, 500L, 500L, 500L, 500L, 500L, 500L))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setSound(soundUri)
                .setWhen(TimeUnit.SECONDS.toMillis(skycam.mostRecentImage.timestamp))
                .setAutoCancel(true)
                .addAction(
                    NotificationCompat.Action.Builder(
                        R.drawable.ic_baseline_cancel_24,
                        getString(R.string.reactivate_alarm_pending_intent_text),
                        reactivateAlarmPendingIntent
                    ).build()
                )

        val style = NotificationCompat.BigPictureStyle()

        /**
         * Try downloading the main image from the skycam location
         * */
        val mainImageRef = mAppStorage.getReferenceFromUrl(skycam.mainImage)
        val mainImageLoadingResult = FireCoil.get(this@AlarmServiceImpl, mainImageRef)

        when (mainImageLoadingResult) {
            is SuccessResult -> style.bigLargeIcon(mainImageLoadingResult.drawable.toBitmap())
            is ErrorResult -> Timber.i("Error while loading main image: ${mainImageLoadingResult.throwable.message}")
        }

        /**
         * Try to download the image that set of the alarm
         */
        val skycamImageStorageRef = mSkycamImageStorage.getReferenceFromUrl(skycam.mostRecentImage.storageLocation)
        val skycamImageLoadingResult = FireCoil.get(this@AlarmServiceImpl, skycamImageStorageRef)

        when (skycamImageLoadingResult) {
            is SuccessResult -> style.bigPicture(skycamImageLoadingResult.drawable.toBitmap())
            is ErrorResult -> Timber.i("Error loading picture: ${skycamImageLoadingResult.throwable.message}")
        }

        notificationBuilder.setStyle(style)

        with(NotificationManagerCompat.from(this@AlarmServiceImpl)) {
            notify(skycam.skycamKey.hashCode(), notificationBuilder.build())
        }

        Timber.i("Sound: $soundUri")
    }

    private fun getAlarmNotificationContentPendingIntent(
        storageLocation: String,
        skycamKey: String
    ): PendingIntent? {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(ACTIVITY_INTENT_EXTRAS_STORAGE_LOCATION_KEY, storageLocation)
            putExtra(ACTIVITY_INTENT_EXTRAS_SKYCAMKEY_KEY, skycamKey)
        }
        return PendingIntent.getActivity(
            this,
            ACTIVITY_WITH_EXTRAS_KEY_STORAGE_LOCATION_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_ONE_SHOT
        )
    }

    private fun getReactivateAlarmPendingIntent(skycamKey: String): PendingIntent? {
        val intent = Intent(this, AlarmServiceImpl::class.java).apply {
            putExtra(SERVICE_REACTIVATE_ALARM_EXTRA_KEY, skycamKey)
        }
        return PendingIntent.getService(
            this,
            SERVICE_REACTIVATE_ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_ONE_SHOT
        )
    }

    /**
     * Takes a list of Flow<Skycam> and merges it into a single flow which we will use to act upon
     * updates from the skycams. We drop the same number of updates as the size of the skycamFlowList
     * to avoid receiving the first updates. The first updates are just the current value in db
     * and does not reflect a realtime update from the skycam.
     *
     * We are cancelling and restarting the mSkycamFlowsJob everytime there is a change to the
     * users alarm settings
     * */
    private fun startSkycamUpdateListener(skycamFlowList: List<Flow<Skycam>>) {
        mSkycamMergedFlowJob = lifecycleScope.launch {
            skycamFlowList
                .merge()
                .flowOn(Dispatchers.IO)
                .drop(skycamFlowList.size)
                .collect {
                    when (it.mostRecentImage.predictionLabel) {
                        AuroraPredictionLabel.VISIBLE_AURORA -> showAlarmNotification(it)
                        AuroraPredictionLabel.NOT_AURORA -> {
                        }
                        AuroraPredictionLabel.NOT_PREDICTED -> {
                        }
                    }
                    Timber.i("New update from ${it.location.name}. Prediction: ${it.mostRecentImage.predictionLabel}")
                }
        }
    }

    private fun cancelSkycamUpdateListenerJob() {
        if (this::mSkycamMergedFlowJob.isInitialized) mSkycamMergedFlowJob.cancel()
    }

    inner class LocalBinder : Binder() {
        fun getService(): AlarmServiceImpl = this@AlarmServiceImpl
    }
}