package com.solvind.skycams.app.domain.usecases

import com.google.common.truth.Truth.assertThat
import com.solvind.skycams.app.core.Failure.UpdateAlarmTimeLessThanZeroFailure
import com.solvind.skycams.app.core.Resource
import com.solvind.skycams.app.domain.model.AlarmConfig
import com.solvind.skycams.app.domain.repo.IAlarmConfigRepo
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.util.concurrent.TimeUnit

@ExperimentalCoroutinesApi
class UpdateAlarmConfigTimeUseCaseTest {

    lateinit var mockConfigRepo: IAlarmConfigRepo

    lateinit var getAlarmConfigUseCase: GetAlarmConfigUseCase
    lateinit var useCaseConfig: UpdateAlarmConfigTimeUseCase

    private val testDispatcher = TestCoroutineDispatcher()

    @After
    fun cleanup() {
        testDispatcher.cleanupTestCoroutines()
    }

    @Before
    fun setupUseCase() {
        mockConfigRepo = mockk()
        getAlarmConfigUseCase = GetAlarmConfigUseCase(mockConfigRepo, testDispatcher, testDispatcher)
        useCaseConfig = UpdateAlarmConfigTimeUseCase(mockConfigRepo, getAlarmConfigUseCase, testDispatcher, testDispatcher, )
    }

    @Test
    fun `Plus seconds less than zero returns error`() = runBlocking {
        val TEST_SKYCAM_KEY = "lyngennorth"
        val TEST_PARAMS = UpdateAlarmConfigTimeUseCase.Params(TEST_SKYCAM_KEY, -200L)

        val result = useCaseConfig.run(TEST_PARAMS)
        assertThat(result is Resource.Error)
        assertThat((result as Resource.Error).failure).isEqualTo(UpdateAlarmTimeLessThanZeroFailure)
    }

    @Test
    fun `Set alarm to instant now plus reward seconds when previous alarm was in the past`() = runBlockingTest {
        val TEST_SKYCAM_KEY = "lyngennorth"
        val TEST_PARAMS = UpdateAlarmConfigTimeUseCase.Params(TEST_SKYCAM_KEY, TimeUnit.MINUTES.toSeconds(30L))
        val GET_TEST_ALARM = AlarmConfig(TEST_SKYCAM_KEY, 200L)

        val slot = slot<Long>()

        coEvery { mockConfigRepo.getAlarmConfig(TEST_SKYCAM_KEY) } returns Resource.Success(GET_TEST_ALARM)
        coEvery { mockConfigRepo.setAlarmConfig(any(), capture(slot)) } returns Resource.Success(
            Unit
        )

        useCaseConfig.run(TEST_PARAMS)

        assertThat(slot.captured).isEqualTo(Instant.now().epochSecond + TEST_PARAMS.plusEpochSeconds)

    }

    @Test
    fun `Set alarm timeout to previous alarm plus reward seconds when previous alarm is in the future`() = runBlockingTest {
        val TEST_SKYCAM_KEY = "lyngennorth"
        val TEST_PARAMS = UpdateAlarmConfigTimeUseCase.Params(TEST_SKYCAM_KEY, TimeUnit.MINUTES.toSeconds(30L))
        val GET_TEST_ALARM = AlarmConfig(TEST_SKYCAM_KEY, Instant.now().epochSecond + TimeUnit.MINUTES.toSeconds(240L))

        val slot = slot<Long>()

        coEvery { mockConfigRepo.getAlarmConfig(TEST_SKYCAM_KEY) } returns Resource.Success(GET_TEST_ALARM)
        coEvery { mockConfigRepo.setAlarmConfig(any(), capture(slot)) } returns Resource.Success(
            Unit
        )
        useCaseConfig.run(TEST_PARAMS)

        assertThat(slot.captured).isEqualTo(GET_TEST_ALARM.alarmAvailableUntilEpochSeconds + TEST_PARAMS.plusEpochSeconds)
    }
}