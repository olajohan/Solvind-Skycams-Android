package com.solvind.skycams.app.service

import androidx.lifecycle.LifecycleCoroutineScope
import com.solvind.skycams.app.di.IoDispatcher
import com.solvind.skycams.app.domain.enums.AuroraPrediction
import com.solvind.skycams.app.domain.model.AlarmConfig
import com.solvind.skycams.app.domain.model.Skycam
import com.solvind.skycams.app.domain.usecases.GetSkycamFlowUseCase
import dagger.hilt.android.scopes.ServiceScoped
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject

/**
 * Responsible for keeping track of active listeners and starting/stopping to receive updates from skycams.
 *
 * It is the clients responsibility to call the start and stop listening functions of this class.
 *
 * As this class will be responsible for setting the timeout for the job, it will also be the one cancelling the job
 * when the given timeout has been reached. TODO look for a solution to honor better separation of concerns
 *
 * */
@ServiceScoped
class SkycamListenerHandler @Inject constructor(
    private val mGetSkycamFlowUseCase: GetSkycamFlowUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    private val mMutex = Mutex()

    /**
     * Map of the currently active skycam listening jobs. The jobs flow collectors emits values
     * on each update from the active skycams.
     *
     * This map represents shared mutable state and will only be accessed using a [Mutex]
     *
     * @MapKey skycamKey
     * @MapValue active skycam listening job
     * */
    private val mSkycamListenerJobs = mutableMapOf<String, Job>()

    /**
     * Used to send [AlarmStatusUpdate] updates about changes to the user's alarms.
     * */
    private val mAlarmStatusUpdateChannel = Channel<AlarmStatusUpdate>()
    val alarmStatusUpdateChannel = mAlarmStatusUpdateChannel as ReceiveChannel<AlarmStatusUpdate>

    /**
     * Used to send [PredictedSkycamUpdate] for the user's active alarms.
     * When a alarm becomes deactivated there should be no more updates sent to this channel.
     * */
    private val mSkycamUpdateChannel = Channel<PredictedSkycamUpdate>()
    val skycamUpdateChannel = mSkycamUpdateChannel as ReceiveChannel<PredictedSkycamUpdate>

    /**
     * Adds a new job to the map of skycam lister jobs. The job will stay active until the timeout
     * given in the alarm config has been reached.
     *
     * The job is a indexed flow collecter. On first emit we send a [AlarmStatusUpdate.Activated] update to the alarm status update channel.
     *
     * On each emit after that we send [PredictedSkycamUpdate] to the skycam update channel.
     *
     * After the collection job has been launched we add it to the active skycam alarms map.
     *
     * We also attach a invokeOnCompletion callback to the job which will send an update to the alarm status update channel
     * with the cause of the deactivation representet as a [AlarmStatusUpdate]
     *
     *
     * @param scope the [LifecycleCoroutineScope] in which the listening job should exsist
     * @param alarmConfig the [AlarmConfig] of the skycam that we should start listening for updates
     * */
    fun startListeningToSkycam(scope: LifecycleCoroutineScope, alarmConfig: AlarmConfig) =
        scope.launch {

            stopListeningToSkycam(scope, alarmConfig.skycamKey, CanceledDueToReset).join()

            mMutex.withLock {

                val listenerJob = scope.launch {
                    withTimeout(timeMillis = alarmConfig.timeLeftMilli()) {

                        mGetSkycamFlowUseCase.run(GetSkycamFlowUseCase.Params(alarmConfig.skycamKey))
                            .flowOn(ioDispatcher)
                            .collectIndexed { index, skycam ->

                                Timber.i("Emission #$index from ${skycam.location.name} ${skycam.mostRecentImage.timestamp}")

                                if (index == 0) {
                                    mAlarmStatusUpdateChannel.send(AlarmStatusUpdate.Activated(skycam, alarmConfig))
                                } else {
                                    when (skycam.mostRecentImage.prediction) {
                                        is AuroraPrediction.VisibleAurora -> mSkycamUpdateChannel.send(
                                            PredictedSkycamUpdate.VisibleAurora(skycam, alarmConfig)
                                        )
                                        is AuroraPrediction.NotAurora -> mSkycamUpdateChannel.send(
                                            PredictedSkycamUpdate.NotAurora(skycam, alarmConfig)
                                        )
                                        is AuroraPrediction.NotPredicted -> mSkycamUpdateChannel.send(
                                            PredictedSkycamUpdate.NotPredicted(skycam, alarmConfig)
                                        )
                                    }
                                }
                            }
                    }
                }

                listenerJob.invokeOnCompletion {
                    scope.launch {
                        Timber.i("Job cancelled ${alarmConfig.skycamKey}")
                        when (it) {
                            is TimeoutCancellationException -> mAlarmStatusUpdateChannel.send(AlarmStatusUpdate.Timeout(alarmConfig))
                            is CanceledDueToReset -> mAlarmStatusUpdateChannel.send(AlarmStatusUpdate.DeactivatedDueToReset(alarmConfig))
                            is CanceledByUser -> mAlarmStatusUpdateChannel.send(AlarmStatusUpdate.Deactivated(alarmConfig))
                        }
                    }
                }

                // Already inside Mutex lock
                mSkycamListenerJobs[alarmConfig.skycamKey] = listenerJob

            }
        }

    /**
     * Cancels a job in the map of active skycam listening jobs referenced by a unique skycam key.
     * It is safe to call this method if the skycam job has already been removed from the list and/or already been canceled.
     *
     * @param cause coroutine cancellation throwable or null used for looking up what caused the job to cancel
     * @param skycamKey unique key for a given skycam
     * @param scope the lifecycleCoroutineScope in which the the job should be launched
     * */
    fun stopListeningToSkycam(scope: LifecycleCoroutineScope, skycamKey: String, cause: CancellationException? = null) = scope.launch {
        mMutex.withLock {
            mSkycamListenerJobs[skycamKey]?.cancel(cause)
            mSkycamListenerJobs[skycamKey]?.join()
            mSkycamListenerJobs.remove(skycamKey)
        }
    }
}

sealed class PredictedSkycamUpdate(open val skycam: Skycam, open val alarmConfig: AlarmConfig) {
    data class VisibleAurora(override val skycam: Skycam, override val alarmConfig: AlarmConfig) : PredictedSkycamUpdate(skycam, alarmConfig)
    data class NotAurora(override val skycam: Skycam, override val alarmConfig: AlarmConfig) : PredictedSkycamUpdate(skycam, alarmConfig)
    data class NotPredicted(override val skycam: Skycam, override val alarmConfig: AlarmConfig) : PredictedSkycamUpdate(skycam, alarmConfig)
}

sealed class AlarmStatusUpdate(open val alarmConfig: AlarmConfig) {
    data class Activated(val skycam: Skycam, override val alarmConfig: AlarmConfig) : AlarmStatusUpdate(alarmConfig)
    data class Deactivated(override val alarmConfig: AlarmConfig) : AlarmStatusUpdate(alarmConfig)
    data class DeactivatedDueToReset(override val alarmConfig: AlarmConfig) : AlarmStatusUpdate(alarmConfig)
    data class Timeout(override val alarmConfig: AlarmConfig) : AlarmStatusUpdate(alarmConfig)
}

object CanceledByUser : CancellationException()
object CanceledDueToReset : CancellationException()