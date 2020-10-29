package com.solvindskycams.app.domain.usecases

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.verify
import com.solvind.skycams.app.core.Failure
import com.solvind.skycams.app.core.Failure.UpdateAlarmTimeLessThanZeroFailure
import com.solvind.skycams.app.core.Resource
import com.solvind.skycams.app.domain.model.Alarm
import com.solvind.skycams.app.domain.repo.IAlarmRepo
import com.solvind.skycams.app.domain.usecases.GetAlarmUseCase
import com.solvind.skycams.app.domain.usecases.UpdateAlarmTimeUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import java.time.Instant
import java.util.concurrent.TimeUnit

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class UpdateAlarmTimeGetAllAlarmsFlowUseCaseTest {

    @Mock
    lateinit var mockRepo: IAlarmRepo

    lateinit var getAlarmUseCase: GetAlarmUseCase
    lateinit var useCase: UpdateAlarmTimeUseCase

    private val testDispatcher = TestCoroutineDispatcher()

    @After
    fun cleanup() {
        testDispatcher.cleanupTestCoroutines()
    }

    @Before
    fun setupUseCase() {
        getAlarmUseCase = GetAlarmUseCase(mockRepo, testDispatcher)
        useCase = UpdateAlarmTimeUseCase(mockRepo, getAlarmUseCase, testDispatcher)
    }

    @Test
    fun updateAlarmTime_lessThanZero_returnsResourceError() = runBlocking {
        val TEST_SKYCAM_KEY = "lyngennorth"
        val TEST_PARAMS = UpdateAlarmTimeUseCase.Params(TEST_SKYCAM_KEY, -200L)

        val result = useCase.run(TEST_PARAMS)
        assertThat(result is Resource.Error).isTrue()
        assertThat((result as Resource.Error).failure).isEqualTo(UpdateAlarmTimeLessThanZeroFailure)
    }

    @Test
    fun updateAlarmTime_repoGetAlarmReturnsAlarmNotFound_returnsResourceError() =
        runBlockingTest {
            val TEST_SKYCAM_KEY = "SKYCAM_NOT_IN_LIST"
            val TEST_PARAMS = UpdateAlarmTimeUseCase.Params(TEST_SKYCAM_KEY, 0L)
            `when`(mockRepo.getAlarm(TEST_SKYCAM_KEY)).thenReturn(
                Resource.Error(Failure.AlarmNotFoundFailure)
            )
            val result = useCase.run(TEST_PARAMS)
            assertThat(result is Resource.Error).isTrue()
            assertThat((result as Resource.Error).failure).isEqualTo(Failure.AlarmNotFoundFailure)
        }

    @Test
    fun updateAlarmTime_repoGetAlarmUidIsNull_returnsResourceError() = runBlockingTest {
        val TEST_SKYCAM_KEY = "SKYCAM_NOT_IN_LIST"
        val TEST_PARAMS = UpdateAlarmTimeUseCase.Params(TEST_SKYCAM_KEY, 0L)
        `when`(mockRepo.getAlarm(TEST_SKYCAM_KEY)).thenReturn(Resource.Error(Failure.UserIdNullFailure))
        val result = useCase.run(TEST_PARAMS)
        assertThat(result is Resource.Error).isTrue()
        assertThat((result as Resource.Error).failure).isEqualTo(Failure.UserIdNullFailure)
    }

    @Test
    fun updateAlarmTime_repoGetAlarmSuccessAndSetAlarmUserIdIsNull_returnsResourceError() = runBlockingTest {
        val TEST_SKYCAM_KEY = "lyngennorth"
        val TEST_PARAMS = UpdateAlarmTimeUseCase.Params(TEST_SKYCAM_KEY, 60L)
        val TEST_GET_ALARM = Alarm(TEST_SKYCAM_KEY, 200L, true)
        val CURRENT_TIME = Instant.now().epochSecond
        `when`(mockRepo.getAlarm(TEST_SKYCAM_KEY)).thenReturn(Resource.Success(TEST_GET_ALARM))

        `when`(mockRepo.setAlarm(
            TEST_SKYCAM_KEY,
            CURRENT_TIME + TEST_PARAMS.plusEpochSeconds, TEST_GET_ALARM.isActive)
        ).thenReturn(Resource.Error(Failure.UserIdNullFailure))

        val result = useCase.run(TEST_PARAMS)

        assertThat(result is Resource.Error).isTrue()
        assertThat((result as Resource.Error).failure).isEqualTo(Failure.UserIdNullFailure)
    }

    @Test
    fun updateAlarm_currentAlarmIsInThePast_returnsSuccessAlarmInstantNowPlusParameterValue() = runBlockingTest {
        val TEST_SKYCAM_KEY = "lyngennorth"
        val TEST_PARAMS = UpdateAlarmTimeUseCase.Params(TEST_SKYCAM_KEY, TimeUnit.MINUTES.toSeconds(30L))
        val GET_TEST_ALARM = Alarm(TEST_SKYCAM_KEY, 200L, true)
        val CURRENT_TIME = Instant.now().epochSecond
        `when`(mockRepo.getAlarm(TEST_SKYCAM_KEY)).thenReturn(Resource.Success(GET_TEST_ALARM))

        useCase.run(TEST_PARAMS)
        verify(mockRepo).setAlarm(eq(TEST_SKYCAM_KEY), eq(CURRENT_TIME + TEST_PARAMS.plusEpochSeconds), eq(GET_TEST_ALARM.isActive))
    }

    @Test
    fun updateAlarm_currentAlarmInTheFuture_returnsSuccessAlarmCurrentAlarmPlusParameterValue() = runBlockingTest {
        val TEST_SKYCAM_KEY = "lyngennorth"
        val TEST_PARAMS = UpdateAlarmTimeUseCase.Params(TEST_SKYCAM_KEY, TimeUnit.MINUTES.toSeconds(30L))
        val GET_TEST_ALARM = Alarm(TEST_SKYCAM_KEY, Instant.now().epochSecond + TimeUnit.MINUTES.toSeconds(240L), true)

        `when`(mockRepo.getAlarm(TEST_SKYCAM_KEY)).thenReturn(Resource.Success(GET_TEST_ALARM))
        useCase.run(TEST_PARAMS)
        verify(mockRepo).setAlarm(eq(TEST_SKYCAM_KEY), eq(GET_TEST_ALARM.alarmAvailableUntilEpochSeconds + TEST_PARAMS.plusEpochSeconds), eq(GET_TEST_ALARM.isActive))
    }
}