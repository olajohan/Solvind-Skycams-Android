package com.solvind.skycams.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import coil.request.ErrorResult
import coil.request.SuccessResult
import com.google.firebase.storage.FirebaseStorage
import com.solvind.skycams.app.R
import com.solvind.skycams.app.core.*
import com.solvind.skycams.app.di.AuroraAlarmAppspot
import com.solvind.skycams.app.di.SkycamImages
import com.solvind.skycams.app.domain.model.AlarmConfig
import com.solvind.skycams.app.domain.model.Skycam
import com.solvind.skycams.app.domain.usecases.*
import com.solvind.skycams.app.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import io.github.rosariopfernandes.firecoil.FireCoil
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AlarmServiceImpl : LifecycleService() {

    @Inject
    lateinit var mGetAllAlarmsFlowUseCase: GetAllAlarmsFlowUseCase

    @Inject
    lateinit var mGetSkycamFlowUseCase: GetSkycamFlowUseCase

    @Inject
    lateinit var mActivateAlarmUseCase: ActivateAlarmConfigUseCase

    @Inject
    lateinit var mDeactivateAlarmUseCase: DeactivateAlarmConfigUseCase

    @Inject
    lateinit var mDeactivateAllAlarmsUseCase: DeactivateAllAlarmsUseCase

    @Inject
    lateinit var mGetSkycamUseCase: GetSkycamUseCase

    @Inject
    @SkycamImages
    lateinit var mSkycamImageStorage: FirebaseStorage

    @Inject
    @AuroraAlarmAppspot
    lateinit var mAppStorage: FirebaseStorage

    /**
     * The list contains both inactive and active alarms.
     *
     *  The number of alarms in this list will not by default be the same as the number of skycam entries in the
     * database. Only the alarms the user has previously activated will be in this list.
     *
     * Stateflows are thread safe by default
     * */
    companion object {
        private val mAlarms = MutableStateFlow(mutableListOf<AlarmConfig>())
        val alarms = mAlarms.asStateFlow()
    }

    private lateinit var mSkycamFlowsJob: Job

    /**
     * Interface for communicating with bound components. This service will not make use of the binding
     * other than calling internal methods in onbBind/onUnbind
     * */
    private val binder = LocalBinder()

    /**
     * Starts the handler responsiblev for checking the alarmlist every second and deactiating the
     * users alarm if the alarm's availableUntilEpochSeconds in the past.
     *
     * Also starts the user alarm listener responsible for updating the alarm list whenever an
     * update happens in the database.
     * */
    override fun onCreate() {
        super.onCreate()
        createAlarmNotificationChannel()
        createForegroundNotificationChannel()
        startUserAlarmsListener()
        startAlarmValidator()
        resetSkycamCollectingJob()
    }

    private fun resetSkycamCollectingJob() {
        if (this::mSkycamFlowsJob.isInitialized) {
            mSkycamFlowsJob.cancel()
        }

        mSkycamFlowsJob = lifecycleScope.launchWhenCreated {
            mAlarms.transform<List<AlarmConfig>, AlarmConfig> {
                val validAlarms = mAlarms.value.filter { it.isActiveAndNotTimeout() }
                if (validAlarms.isNotEmpty()) {
                    showForegroundNotification(validAlarms)
                } else {
                    stopForeground(true)
                }
                validAlarms.forEach {
                    emit(it)
                }

            }.flatMapMerge {
                    mGetSkycamFlowUseCase.run(GetSkycamFlowUseCase.Params(it.skycamKey))
            }.drop(
                mAlarms.value.count { it.isActiveAndNotTimeout() }
            ).collect {
                Timber.i("New update from ${it.location.name} Time: ${it.mostRecentImage.timestamp}")
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        /**
         * This intent is sent when the user clicks the CANCEL button in the foreground notification.
         * We the extra value to the key for SERVICE_CLEAR_ALL_ALARMS_EXTRA_KEY will always be true,
         * so we don't bother with any further checks than that the key exists in the Intent extras
         * before deactivating all alarms
         */
        when (intent?.action) {
            SERVICE_CANCEL_ALL_ALARM_INTENT_ACTION -> deactivateAllAlarms()

            SERVICE_REACTIVATE_ALARM_INTENT_ACTION -> intent.extras?.getString(
                INTENT_EXTRA_SKYCAMKEY
            )?.let { skycamKey ->
                lifecycleScope.launch {
                    mActivateAlarmUseCase(this, ActivateAlarmConfigUseCase.Params(skycamKey)) {
                        when (it) {
                            is Resource.Success -> Toast.makeText(
                                this@AlarmServiceImpl,
                                "Alarm reactivated",
                                Toast.LENGTH_LONG
                            ).show()
                            is Resource.Error -> Toast.makeText(
                                this@AlarmServiceImpl,
                                "Error while trying to reactivate",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        if (mAlarms.value.none { it.isActive }) stopSelf()
        return true
    }

    /**
     * Deactivates alarms that has timed out
     * */
    private fun startAlarmValidator() = lifecycleScope.launch {
        while (true) {
            mAlarms.value.forEach {
                if (it.hasTimedoutAndIsStillActive()) {
                    deactivateSingleAlarm(it.skycamKey)
                }
            }
            delay(1000L)
        }
    }

    /**
     * Starts/restarts listening for updates (inserts, modifications and deletions) on the users alarms.
     * The listener is attached/canceled in the service's started/destroyed
     *
     * */
    private fun startUserAlarmsListener() {
        lifecycleScope.launch {
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
                .collect {
                    mAlarms.value = it.toMutableList()
                    resetSkycamCollectingJob()
                }
        }
    }

    /**
     * Sets a single alarms isActive to false in the database. This will fire of the user alarm listener
     * and adjust the list accordingly.
     * */
    private fun deactivateSingleAlarm(skycamKey: String) = lifecycleScope.launch(Dispatchers.IO) {
        mDeactivateAlarmUseCase(this, DeactivateAlarmConfigUseCase.Params(skycamKey))
    }

    private fun deactivateAllAlarms() = lifecycleScope.launch(Dispatchers.IO) {
        Timber.i("DeactivateAllAlarms")
        mDeactivateAllAlarmsUseCase(this, UseCase.None()) {
            Timber.i("Result=$it")
        }

    }

    /**
     * Updates or removes the foreground notification depending on if the provided filtered list
     * is empty or contains active alarms.
     * */
    private fun showForegroundNotification(validAlarmsList: List<AlarmConfig>) {

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
            ACTIVITY_OPEN_DESTINATION_HOME,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun getClearAllAlarmsPendingIntent(): PendingIntent? {
        val intent = Intent(this, AlarmServiceImpl::class.java).apply {
            action = SERVICE_CANCEL_ALL_ALARM_INTENT_ACTION
        }
        return PendingIntent.getService(
            this,
            SERVICE_CANCEL_ALL_ALARMS_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
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
        val skycamImageStorageRef =
            mSkycamImageStorage.getReferenceFromUrl(skycam.mostRecentImage.storageLocation)
        val skycamImageLoadingResult = FireCoil.get(this@AlarmServiceImpl, skycamImageStorageRef)

        when (skycamImageLoadingResult) {
            is SuccessResult -> style.bigPicture(skycamImageLoadingResult.drawable.toBitmap())
            is ErrorResult -> Timber.i("Error loading picture: ${skycamImageLoadingResult.throwable.message}")
        }

        notificationBuilder.setStyle(style)

        with(NotificationManagerCompat.from(this@AlarmServiceImpl)) {
            notify(skycam.skycamKey.hashCode(), notificationBuilder.build())
        }

        /**
         * Deactivate the alarm for the skycam, so the user will not keep being disturbed
         * */
        deactivateSingleAlarm(skycam.skycamKey)
    }

    private fun getAlarmNotificationContentPendingIntent(
        storageLocation: String,
        skycamKey: String
    ): PendingIntent? {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(ACTIVITY_INTENT_EXTRAS_STORAGE_LOCATION_KEY, storageLocation)
            putExtra(INTENT_EXTRA_SKYCAMKEY, skycamKey)
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
            action = SERVICE_REACTIVATE_ALARM_INTENT_ACTION
            putExtra(INTENT_EXTRA_SKYCAMKEY, skycamKey)
        }
        return PendingIntent.getService(
            this,
            SERVICE_REACTIVATE_ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_ONE_SHOT
        )
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
        val channel = NotificationChannel(
            FOREGROUND_NOTIFICATIONS_CHANNEl_ID,
            name,
            importance
        ).apply { description = descriptionText }
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    inner class LocalBinder : Binder() {
        fun getService(): AlarmServiceImpl = this@AlarmServiceImpl
    }
}