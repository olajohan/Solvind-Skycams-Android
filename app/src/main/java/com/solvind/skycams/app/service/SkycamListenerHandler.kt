package com.solvind.skycams.app.service

import androidx.lifecycle.LifecycleCoroutineScope
import com.solvind.skycams.app.di.IoDispatcher
import com.solvind.skycams.app.domain.enums.AuroraPredictionLabel
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

@ServiceScoped
class SkycamListenerHandler @Inject constructor(
    private val mGetSkycamFlowUseCase: GetSkycamFlowUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    private val mMutex = Mutex()
    private val mSkycamListenerJobs = mutableMapOf<String, Job>()

    private val mAlarmStatusUpdateChannel = Channel<AlarmStatus>()
    val alarmStatusUpdateChannel = mAlarmStatusUpdateChannel as ReceiveChannel<AlarmStatus>

    private val mSkycamUpdateChannel = Channel<PredictedSkycamUpdate>()
    val skycamUpdateChannel = mSkycamUpdateChannel as ReceiveChannel<PredictedSkycamUpdate>

    fun startListeningToSkycam(scope: LifecycleCoroutineScope, alarmConfig: AlarmConfig) =
        scope.launch {

            stopListeningToSkycam(scope, alarmConfig.skycamKey, CanceledDueToReset).join()

            // mSkycamListenerJobs is shared mutable state
            mMutex.withLock {

                val listenerJob = scope.launch {

                    withTimeout(timeMillis = alarmConfig.timeLeftMilli()) {

                        mGetSkycamFlowUseCase.run(GetSkycamFlowUseCase.Params(alarmConfig.skycamKey))
                            .flowOn(ioDispatcher)
                            .collectIndexed { index, skycam ->

                                Timber.i("Emission #$index from ${skycam.location.name} ${skycam.mostRecentImage.timestamp}")

                                if (index == 0) {
                                    mAlarmStatusUpdateChannel.send(AlarmStatus.Activated(skycam, alarmConfig))
                                } else {
                                    when (skycam.mostRecentImage.predictionLabel) {
                                        AuroraPredictionLabel.VISIBLE_AURORA -> mSkycamUpdateChannel.send(
                                            PredictedSkycamUpdate.VisibleAurora(skycam, alarmConfig)
                                        )
                                        AuroraPredictionLabel.NOT_AURORA -> mSkycamUpdateChannel.send(
                                            PredictedSkycamUpdate.NotAurora(skycam, alarmConfig)
                                        )
                                        AuroraPredictionLabel.NOT_PREDICTED -> mSkycamUpdateChannel.send(
                                            PredictedSkycamUpdate.NotPredicted(skycam, alarmConfig)
                                        )
                                    }
                                }
                            }
                    }
                }

                listenerJob.invokeOnCompletion {
                    scope.launch {
                        when (it) {
                            is TimeoutCancellationException -> mAlarmStatusUpdateChannel.send(AlarmStatus.Timeout(alarmConfig))
                            is CanceledDueToReset -> mAlarmStatusUpdateChannel.send(AlarmStatus.DeactivatedDueToReset(alarmConfig))
                            is CanceledByUser -> mAlarmStatusUpdateChannel.send(AlarmStatus.Deactivated(alarmConfig))
                        }

                    }
                }

                mSkycamListenerJobs[alarmConfig.skycamKey] = listenerJob
            }
        }

    fun stopListeningToSkycam(scope: LifecycleCoroutineScope, skycamKey: String, cause: CancellationException? = null) = scope.launch {
        mMutex.withLock {
            mSkycamListenerJobs[skycamKey]?.cancel(cause)
            mSkycamListenerJobs[skycamKey]?.join()
            mSkycamListenerJobs.remove(skycamKey)
        }
    }
}

sealed class PredictedSkycamUpdate {
    data class VisibleAurora(val skycam: Skycam, val alarmConfig: AlarmConfig) : PredictedSkycamUpdate()
    data class NotAurora(val skycam: Skycam, val alarmConfig: AlarmConfig) : PredictedSkycamUpdate()
    data class NotPredicted(val skycam: Skycam, val alarmConfig: AlarmConfig) : PredictedSkycamUpdate()
}

sealed class AlarmStatus(val alarmConfig: AlarmConfig) {
    data class Activated(val skycam: Skycam, val ac: AlarmConfig) : AlarmStatus(ac)
    data class Deactivated(val ac: AlarmConfig) : AlarmStatus(ac)
    data class DeactivatedDueToReset(val ac: AlarmConfig) : AlarmStatus(ac)
    data class Timeout(val ac: AlarmConfig) : AlarmStatus(ac)
}

object CanceledByUser : CancellationException()
object CanceledDueToReset : CancellationException()