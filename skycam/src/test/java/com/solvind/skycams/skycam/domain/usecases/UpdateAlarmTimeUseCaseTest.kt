package com.solvind.skycams.skycam.domain.usecases

import com.google.common.truth.Truth.assertThat
import com.solvind.skycams.skycam.Failure
import com.solvind.skycams.skycam.Failure.UpdateAlarmTimeLessThanZeroFailure
import com.solvind.skycams.skycam.Result
import com.solvind.skycams.skycam.domain.model.Alarm
import com.solvind.skycams.skycam.domain.repo.IAlarmRepo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import java.time.Instant
import java.util.concurrent.TimeUnit

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class UpdateAlarmTimeUseCaseTest {

    @Mock
    lateinit var mockRepo: IAlarmRepo

    lateinit var getAlarmUseCase: GetAlarmUseCase
    lateinit var useCase: UpdateAlarmTimeUseCase

    @Before
    fun setupUseCase() {
        getAlarmUseCase = GetAlarmUseCase(mockRepo)
        useCase = UpdateAlarmTimeUseCase(mockRepo, getAlarmUseCase)
    }

    @Test
    fun updateAlarmTime_lessThanZero_returnsResourceError() = runBlocking {
        val TEST_SKYCAM_KEY = "lyngennorth"
        val TEST_PARAMS = UpdateAlarmTimeUseCase.Params(TEST_SKYCAM_KEY, -200L)

        val result = useCase.run(TEST_PARAMS)
        assertThat(result is Result.Error).isTrue()
        assertThat((result as Result.Error).failure).isEqualTo(UpdateAlarmTimeLessThanZeroFailure)
    }

    @Test
    fun updateAlarmTime_repoGetAlarmReturnsAlarmNotFound_returnsResourceError() =
        runBlockingTest {
            val TEST_SKYCAM_KEY = "SKYCAM_NOT_IN_LIST"
            val TEST_PARAMS = UpdateAlarmTimeUseCase.Params(TEST_SKYCAM_KEY, 0L)
            `when`(mockRepo.getAlarm(TEST_SKYCAM_KEY)).thenReturn(
                Result.Error(Failure.AlarmNotFoundFailure)
            )
            val result = useCase.run(TEST_PARAMS)
            assertThat(result is Result.Error).isTrue()
            assertThat((result as Result.Error).failure).isEqualTo(Failure.AlarmNotFoundFailure)
        }

    @Test
    fun updateAlarmTime_repoGetAlarmUidIsNull_returnsResourceError() = runBlockingTest {
        val TEST_SKYCAM_KEY = "SKYCAM_NOT_IN_LIST"
        val TEST_PARAMS = UpdateAlarmTimeUseCase.Params(TEST_SKYCAM_KEY, 0L)
        `when`(mockRepo.getAlarm(TEST_SKYCAM_KEY)).thenReturn(Result.Error(Failure.UserIdNullFailure))
        val result = useCase.run(TEST_PARAMS)
        assertThat(result is Result.Error).isTrue()
        assertThat((result as Result.Error).failure).isEqualTo(Failure.UserIdNullFailure)
    }

    @Test
    fun updateAlarmTime_repoGetAlarmSuccessAndSetAlarmUserIdIsNull_returnsResourceError() = runBlockingTest {
        val TEST_SKYCAM_KEY = "lyngennorth"
        val TEST_PARAMS = UpdateAlarmTimeUseCase.Params(TEST_SKYCAM_KEY, 60L)
        val TEST_GET_ALARM = Alarm(TEST_SKYCAM_KEY, 200L, true)
        val CURRENT_TIME = Instant.now().epochSecond
        `when`(mockRepo.getAlarm(TEST_SKYCAM_KEY)).thenReturn(Result.Success(TEST_GET_ALARM))

        `when`(mockRepo.setAlarm(
            TEST_SKYCAM_KEY,
            CURRENT_TIME + TEST_PARAMS.plusEpochSeconds)
        ).thenReturn(Result.Error(Failure.UserIdNullFailure))

        val result = useCase.run(TEST_PARAMS)

        assertThat(result is Result.Error).isTrue()
        assertThat((result as Result.Error).failure).isEqualTo(Failure.UserIdNullFailure)
    }

    @Test
    fun updateAlarm_currentAlarmIsInThePast_returnsSuccessAlarmInstantNowPlusParameterValue() = runBlockingTest {
        val TEST_SKYCAM_KEY = "lyngennorth"
        val TEST_PARAMS = UpdateAlarmTimeUseCase.Params(TEST_SKYCAM_KEY, TimeUnit.MINUTES.toSeconds(30L))
        val GET_TEST_ALARM = Alarm(TEST_SKYCAM_KEY, 200L, true)
        val CURRENT_TIME = Instant.now().epochSecond
        `when`(mockRepo.getAlarm(TEST_SKYCAM_KEY)).thenReturn(Result.Success(GET_TEST_ALARM))

        useCase.run(TEST_PARAMS)
        val newTime = ArgumentCaptor.forClass(Long::class.java)
        verify(mockRepo).setAlarm(anyString(), newTime.capture(), anyBoolean())
        assertThat(newTime.value).isEqualTo(CURRENT_TIME + TEST_PARAMS.plusEpochSeconds)
    }

    @Test
    fun updateAlarm_currentAlarmInTheFuture_returnsSuccessAlarmCurrentAlarmPlusParameterValue() = runBlockingTest {
        val TEST_SKYCAM_KEY = "lyngennorth"
        val TEST_PARAMS = UpdateAlarmTimeUseCase.Params(TEST_SKYCAM_KEY, TimeUnit.MINUTES.toSeconds(30L))
        val GET_TEST_ALARM = Alarm(TEST_SKYCAM_KEY, Instant.now().epochSecond + TimeUnit.MINUTES.toSeconds(240L), true)

        `when`(mockRepo.getAlarm(TEST_SKYCAM_KEY)).thenReturn(Result.Success(GET_TEST_ALARM))
        useCase.run(TEST_PARAMS)
        val newTime = ArgumentCaptor.forClass(Long::class.java)
        verify(mockRepo).setAlarm(anyString(), newTime.capture(), anyBoolean())
        assertThat(newTime.value).isEqualTo(GET_TEST_ALARM.alarmAvailableUntilEpochSeconds + TEST_PARAMS.plusEpochSeconds)
    }
}