package com.solvind.skycams.app.service

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.solvind.skycams.app.core.*
import com.solvind.skycams.app.domain.enums.AuroraPrediction
import com.solvind.skycams.app.domain.model.AlarmConfig
import com.solvind.skycams.app.domain.usecases.DeactivateAlarmConfigUseCase
import com.solvind.skycams.app.domain.usecases.GetAllAlarmsFlowUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AlarmServiceImpl : LifecycleService() {

    @Inject
    lateinit var mGetAllAlarmsFlowUseCase: GetAllAlarmsFlowUseCase

    @Inject
    lateinit var mDeactivateAlarmConfigUseCase: DeactivateAlarmConfigUseCase

    @Inject
    lateinit var mSkycamListenerHandler: SkycamListenerHandler

    @Inject
    lateinit var mForegroundNotificationHandler: ForegroundNotificationHandler

    @Inject
    lateinit var mAlarmNotificationHandler: AlarmNotificationHandler

    @Inject
    lateinit var mAlarmTimeoutNotificationHandler: AlarmTimedoutNotificationHandler

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
        collectUserAlarmConfigFlow()
        startAlarmStatusUpdateListener()
        startSkycamUpdateListener()
        startForegroundNotificationUpdateListener()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {

            lifecycleScope.launchWhenCreated {
                when (it.action) {

                    /*
                    * This intent action is sent when the user click's the next button on the foreground notification
                    * */
                    SERVICE_SELECT_PREVIOUS_ALARM_ACTION -> mForegroundNotificationHandler.selectPreviousFromListAndUpdateForegroundNotification()

                    /*
                    * This intent action is sent when the user click's the cancel/stop button on the foreground notification
                    *
                    * The intent must contain the extras key/value pair of [INTENT_EXTRA_SKYCAMKEY] = e.g "lyngennorth"
                    * */
                    SERVICE_CANCEL_SINGLE_ALARM_ACTION -> {
                        it.getStringExtra(INTENT_EXTRA_SKYCAMKEY)?.let { skycamKey ->
                            mDeactivateAlarmConfigUseCase(this, DeactivateAlarmConfigUseCase.Params(skycamKey)) {
                                // TODO Handle succss/error
                            }
                        }
                    }

                    /*
                    * This intent action is sent when the user click's the next button on the foreground notification
                    * */
                    SERVICE_SELECT_NEXT_ALARM_ACTION -> mForegroundNotificationHandler.selectNextFromListAndUpdateForegroundNotification()

                    /*
                    * This intent action is sent when the user click's the live view button on the foreground notification
                    * */
                    SERVICE_TOGGLE_LIVE_VIEW_ACTION -> mForegroundNotificationHandler.toggleLiveView()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }


    /**
     * Forwards the skycam update to the foregroundservice handler whenever any of the
     * skycams being listened to emits and update inside the skycam listener.
     * */
    private fun startSkycamUpdateListener() = lifecycleScope.launch {
        for (predictedSkycamUpdate in mSkycamListenerHandler.skycamUpdateChannel) {
            val auroraPrediction = predictedSkycamUpdate.skycam.mostRecentImage.prediction
            if (auroraPrediction is AuroraPrediction.VisibleAurora && auroraPrediction.confidence > 0.75) {
                mAlarmNotificationHandler.showAlarmNotification(predictedSkycamUpdate.skycam)
            }
            mForegroundNotificationHandler.updateActiveAlarm(predictedSkycamUpdate)
        }
    }

    /**
     * Listens for alarm updates in the [SkycamListenerHandler] alarm update channel and
     * forwards all updates to the [ForegroundNotificationHandler] except for [AlarmStatusUpdate.DeactivatedDueToReset]
     * since we wan't to handle that update inside the service.
     * */
    private fun startAlarmStatusUpdateListener() = lifecycleScope.launch {

        for (alarmStatusUpdate in mSkycamListenerHandler.alarmStatusUpdateChannel) {
            Timber.i("Alarm status update: $alarmStatusUpdate")
            when (alarmStatusUpdate) {

                // Alarm reset is only consumed by the service and not forwarded to the foreground notification handler
                is AlarmStatusUpdate.DeactivatedDueToReset -> { }

                is AlarmStatusUpdate.Timeout -> {
                    mAlarmTimeoutNotificationHandler.showTimeoutNotification(alarmStatusUpdate.alarmConfig)
                    Timber.i("Timeout")
                    mForegroundNotificationHandler.updateAlarmStatus(alarmStatusUpdate)
                }

                // Activated, deactivated and timeout is forwarded to the foreground notification handler
                else -> {
                    mForegroundNotificationHandler.updateAlarmStatus(alarmStatusUpdate)
                }
            }

        }
    }

    /**
     * Displays a foreground notification whenever the [StateFlow] inside the [ForegroundNotificationHandler]
     * is set to a notification. If the notification is set to null, we remove the foreground notification.
     * */
    private fun startForegroundNotificationUpdateListener() = lifecycleScope.launch {
        mForegroundNotificationHandler.foregroundNotification.collect {
            if (it != null) {
                startForeground(SERVICE_FOREGROUND_NOTIFICATION_ID, it)
            } else {
                stopForeground(true)
            }
        }
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
            mSkycamListenerHandler.stopListeningToSkycam(
                lifecycleScope,
                alarmConfig.skycamKey,
                CanceledByUser
            )
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return binder
    }

    inner class LocalBinder : Binder() {
        fun getService(): AlarmServiceImpl = this@AlarmServiceImpl
    }

}