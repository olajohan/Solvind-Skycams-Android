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
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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
import com.solvind.skycams.app.domain.usecases.DeactivateAlarmConfigUseCase
import com.solvind.skycams.app.domain.usecases.GetAllAlarmsFlowUseCase
import com.solvind.skycams.app.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import io.github.rosariopfernandes.firecoil.FireCoil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AlarmServiceImpl : LifecycleService() {

    @Inject
    lateinit var mGetAllAlarmsFlowUseCase: GetAllAlarmsFlowUseCase

    @Inject
    lateinit var mDeactivateAlarmConfigUseCase: DeactivateAlarmConfigUseCase

    @Inject
    @SkycamImages
    lateinit var mSkycamImageStorage: FirebaseStorage

    @Inject
    @AuroraAlarmAppspot
    lateinit var mAppStorage: FirebaseStorage

    @Inject
    lateinit var mSkycamListenerHandler: SkycamListenerHandler

    @Inject
    lateinit var mForegroundNotificationHandler: ForegroundNotificationHandler

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
        collectUserAlarmConfigFlow()

        lifecycleScope.launch {

            for (skycamUpdate in mSkycamListenerHandler.skycamUpdateChannel) {
                when (skycamUpdate) {
                    is PredictedSkycamUpdate.VisibleAurora -> {}
                    is PredictedSkycamUpdate.NotAurora -> {}
                    is PredictedSkycamUpdate.NotPredicted -> {
                        Timber.i("Received update on channel! ${skycamUpdate.skycam.location.name} ${skycamUpdate.skycam.mostRecentImage.timestamp}")
                    }
                }
            }
        }

        lifecycleScope.launch {
            for (alarmStatusUpdate in mSkycamListenerHandler.alarmStatusUpdateChannel) {
                when(alarmStatusUpdate) {

                    // Alarm reset is only consumed by the service and not forwarded to the foreground notification handler
                    is AlarmStatus.DeactivatedDueToReset -> {}

                    // Activated, deactivated and timeout is forwarded to the foreground notification handler
                    else -> {
                        mForegroundNotificationHandler.updateAlarmList(alarmStatusUpdate)
                    }
                }
            }
        }

        lifecycleScope.launch {
            mForegroundNotificationHandler.foregroundNotification.collect {
                if (it != null) {
                    startForeground(SERVICE_FOREGROUND_NOTIFICATION_ID, it)
                } else {
                    stopForeground(true)
                }
                Timber.i("Foreground notification = $it")
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let {
            lifecycleScope.launchWhenCreated {
                when (it.action) {
                    SERVICE_SELECT_NEXT_ALARM_ACTION -> mForegroundNotificationHandler.selectNextFromList()

                    SERVICE_SELECT_PREVIOUS_ALARM_ACTION -> mForegroundNotificationHandler.selectPreviousFromList()

                    SERVICE_CANCEL_SINGLE_ALARM_ACTION -> {
                        it.extras?.let {
                            it.getString(INTENT_EXTRA_SKYCAMKEY)?.let { skycamKey ->
                                mDeactivateAlarmConfigUseCase(this, DeactivateAlarmConfigUseCase.Params(skycamKey)) {

                                }
                            }
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

    /**
     * Starts/restarts listening for updates (inserts, modifications and deletions) on the users alarms.
     * The listener is attached/canceled in the service's started/destroyed
     *
     * */
    private fun collectUserAlarmConfigFlow() = mGetAllAlarmsFlowUseCase.run(UseCaseFlow.None())
        .flowOn(Dispatchers.IO)
        .onEach { handleAlarmUpdate(it) }
        .launchIn(lifecycleScope)

    private fun handleAlarmUpdate(alarmConfig: AlarmConfig) {
        if (alarmConfig.isActiveAndHasNotTimedOut()) {
            mSkycamListenerHandler.startListeningToSkycam(lifecycleScope, alarmConfig)
        } else {
            mSkycamListenerHandler.stopListeningToSkycam(lifecycleScope, alarmConfig.skycamKey, CanceledByUser)
        }
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
        val soundUri = Uri.parse("android.resource://${packageName}/${R.raw.number_2}")
        val notificationBuilder =
            NotificationCompat.Builder(this@AlarmServiceImpl, SERVICE_ALARM_NOTIFICATIONS_CHANNEL_ID)
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

        val style = NotificationCompat.BigPictureStyle()

        /**
         * Try downloading the main image from the skycam location
         * */
        val mainImageRef = mAppStorage.getReferenceFromUrl(skycam.mainImage)

        when (val mainImageLoadingResult = FireCoil.get(this@AlarmServiceImpl, mainImageRef)) {
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



    /**
     * Creates the notification channel used to alarm the user when northern lights appears.
     *
     * This channel should get the users attention asap.
     * */
    private fun createAlarmNotificationChannel() {
        val name = getString(R.string.alarm_notifications_name)
        val descriptionText = getString(R.string.alarm_notifications_description)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(SERVICE_ALARM_NOTIFICATIONS_CHANNEL_ID, name, importance).apply {
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