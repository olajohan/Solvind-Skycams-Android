package com.solvind.skycams.app.service

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.solvind.skycams.app.core.*
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

        lifecycleScope.launch {
            for (skycamUpdate in mSkycamListenerHandler.skycamUpdateChannel) {
                mForegroundNotificationHandler.updateActiveAlarm(skycamUpdate)
            }
        }

        lifecycleScope.launch {
            for (alarmStatusUpdate in mSkycamListenerHandler.alarmStatusUpdateChannel) {
                when(alarmStatusUpdate) {

                    // Alarm reset is only consumed by the service and not forwarded to the foreground notification handler
                    is AlarmStatus.DeactivatedDueToReset -> {}

                    // Activated, deactivated and timeout is forwarded to the foreground notification handler
                    else -> {
                        mForegroundNotificationHandler.updateAlarmStatus(alarmStatusUpdate)
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

                    SERVICE_SELECT_PREVIOUS_ALARM_ACTION -> mForegroundNotificationHandler.selectPreviousFromList()

                    SERVICE_CANCEL_SINGLE_ALARM_ACTION -> {
                        it.extras?.let {
                            it.getString(INTENT_EXTRA_SKYCAMKEY)?.let { skycamKey ->
                                mDeactivateAlarmConfigUseCase(this, DeactivateAlarmConfigUseCase.Params(skycamKey)) {
                                    // TODO Handle succss/error
                                }
                            }
                        }
                    }

                    SERVICE_SELECT_NEXT_ALARM_ACTION -> mForegroundNotificationHandler.selectNextFromList()

                    SERVICE_TOGGLE_LIVE_VIEW_ACTION -> mForegroundNotificationHandler.toggleLiveView()
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

    inner class LocalBinder : Binder() {
        fun getService(): AlarmServiceImpl = this@AlarmServiceImpl
    }

}