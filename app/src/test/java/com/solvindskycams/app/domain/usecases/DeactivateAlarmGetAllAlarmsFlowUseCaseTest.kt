package com.solvindskycams.app.domain.usecases

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.eq
import com.solvind.skycams.app.core.Failure
import com.solvind.skycams.app.core.Resource
import com.solvind.skycams.app.domain.model.Alarm
import com.solvind.skycams.app.domain.repo.IAlarmRepo
import com.solvind.skycams.app.domain.usecases.DeactivateAlarmUseCase
import com.solvind.skycams.app.domain.usecases.GetAlarmUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import java.time.Instant
import java.util.concurrent.TimeUnit

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class DeactivateAlarmGetAllAlarmsFlowUseCaseTest {

    @Mock
    private lateinit var repo: IAlarmRepo

    private lateinit var activateAlarmUseCase: DeactivateAlarmUseCase
    private lateinit var getAlarmUseCase: GetAlarmUseCase

    private val testDispatcher = TestCoroutineDispatcher()

    @After
    fun cleanup() {
        testDispatcher.cleanupTestCoroutines()
    }

    @Before
    fun setup() {
        getAlarmUseCase = GetAlarmUseCase(repo, testDispatcher)
        activateAlarmUseCase = DeactivateAlarmUseCase(repo, getAlarmUseCase, testDispatcher)
    }

    @Test
    fun activateAlarm_emptySkycamKey_returnsResourceError() = runBlockingTest {
        val TEST_SKYCAM_KEY = ""
        val params = DeactivateAlarmUseCase.Params(TEST_SKYCAM_KEY)
        val result = activateAlarmUseCase.run(params)
        assertThat((result as Resource.Error).failure).isEqualTo(Failure.EmptySkycamKeyFailure)
    }

    @Test
    fun setAlarmStatus_isActiveTrue_keepOtherParamsTheSameAndIsActiveTrue() = runBlockingTest {
        val TEST_SKYCAM_KEY = "lyngennorth"
        val TEST_CURRENT_ALARM = Alarm(TEST_SKYCAM_KEY, Instant.now().epochSecond + TimeUnit.MINUTES.toSeconds(30L), true)
        val setAlarmParams = DeactivateAlarmUseCase.Params(TEST_SKYCAM_KEY)
        `when`(repo.getAlarm(TEST_SKYCAM_KEY)).thenReturn(Resource.Success(TEST_CURRENT_ALARM))
        activateAlarmUseCase.run(setAlarmParams)

        verify(repo).setAlarm(eq(TEST_SKYCAM_KEY), eq(TEST_CURRENT_ALARM.alarmAvailableUntilEpochSeconds), eq(false))
    }
    
}
