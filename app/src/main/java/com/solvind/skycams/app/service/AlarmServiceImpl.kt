package com.solvind.skycams.app.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.solvind.skycams.app.FOREGROUND_NOTIFICATIONS_CHANNEl_ID
import com.solvind.skycams.app.R
import com.solvind.skycams.app.core.UseCase
import com.solvind.skycams.app.core.UseCaseFlow
import com.solvind.skycams.app.domain.enums.AuroraPredictionLabel
import com.solvind.skycams.app.domain.model.Alarm
import com.solvind.skycams.app.domain.model.Skycam
import com.solvind.skycams.app.domain.usecases.DeactivateAlarmUseCase
import com.solvind.skycams.app.domain.usecases.DeactivateAllAlarmsUseCase
import com.solvind.skycams.app.domain.usecases.GetSkycamFlowUseCase
import com.solvind.skycams.app.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.time.Instant
import java.util.*
import javax.inject.Inject

private const val FOREGROUND_NOTIFICATION_ID = 1
private const val ALARM_INTERACTION_INTENT_EXTRA_KEY = "alarm_interaction"
private const val CLEAR_ALL_ALARMS_REQUEST_CODE = 99

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AlarmServiceImpl : LifecycleService() {

    @Inject lateinit var mGetAllAlarmsFlowUseCase: com.solvind.skycams.app.domain.usecases.GetAllAlarmsFlowUseCase
    @Inject lateinit var mGetSkycamFlowUseCase: GetSkycamFlowUseCase
    @Inject lateinit var mDeactivateAlarmUseCase: DeactivateAlarmUseCase
    @Inject lateinit var mDeactivateAllAlarmsUseCase: DeactivateAllAlarmsUseCase
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

        intent?.extras?.get(ALARM_INTERACTION_INTENT_EXTRA_KEY).let {
            if (it == AlarmUserInteractions.CLEAR_ALL_ALARMS) deactivateAllAlarms()
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
        while(true) {
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
        stopSkycamUpdateListener()

        val validAlarmsList = mutableListOf<Alarm>()
        synchronized(mCurrentAlarmsList) {

            validAlarmsList.addAll(
                mCurrentAlarmsList.filter { it.isActive && it.alarmAvailableUntilEpochSeconds > Instant.now().epochSecond }
            )
        }

        /**
         * If the user don't have any valid alarms, then remove the foreground notification and return,
         * */
        if (validAlarmsList.isEmpty()) {
            stopForeground(true)
            return
        }

        /**
         * Get a flow (cold stream) for all the users valid alarms
         * */
        val skycamFlowList = validAlarmsList.mapTo(mutableListOf()) {
            mGetSkycamFlowUseCase.run(GetSkycamFlowUseCase.Params(it.skycamKey))
        }

        Timber.i("Flow list size: ${skycamFlowList.size}")
        startSkycamUpdateListener(skycamFlowList)
        showForegroundNotification()
    }

    /**
     * Updates or removes the foreground notification depending on if the provided filtered list
     * is empty or contains active alarms.
     * */
    private fun showForegroundNotification() {

        val openActivityPendingIntent = getForegroundServiceContentPendingIntent()
        val clearAllAlarmsPendingIntent = getClearAllAlarmsPendingIntent()

        val notification = NotificationCompat.Builder(this, FOREGROUND_NOTIFICATIONS_CHANNEl_ID)
            .setContentTitle("Aurora Alarm")
            .setContentText("Keeping an eye up for northern lights.")
            .setSmallIcon(R.drawable.ic_solvind)
            .setContentIntent(openActivityPendingIntent)
            .addAction(
                NotificationCompat.Action.Builder(
                    R.drawable.ic_baseline_cancel_24, getString(R.string.clear_all_alarms),
                    clearAllAlarmsPendingIntent
                ).build()
            )
            .build()

        startForeground(FOREGROUND_NOTIFICATION_ID, notification)
    }

    private fun getForegroundServiceContentPendingIntent(): PendingIntent? {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(this, 0, intent, 0)
    }

    private fun getClearAllAlarmsPendingIntent(): PendingIntent? {
        val intent = Intent(this, AlarmServiceImpl::class.java).apply {
            putExtra(ALARM_INTERACTION_INTENT_EXTRA_KEY, AlarmUserInteractions.CLEAR_ALL_ALARMS)
        }
        return PendingIntent.getService(
            this,
            CLEAR_ALL_ALARMS_REQUEST_CODE,
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
                    when(it.mostRecentImage.predictionLabel) {
                        AuroraPredictionLabel.VISIBLE_AURORA -> {

                        }
                        AuroraPredictionLabel.NOT_AURORA -> {}
                        AuroraPredictionLabel.NOT_PREDICTED -> {}                    }
                }
        }
    }

    private fun stopSkycamUpdateListener() {
        if (this::mSkycamMergedFlowJob.isInitialized) mSkycamMergedFlowJob.cancel()
    }

    enum class AlarmUserInteractions {
        CLEAR_ALL_ALARMS
    }

    inner class LocalBinder : Binder() {
        fun getService(): AlarmServiceImpl = this@AlarmServiceImpl
    }
}