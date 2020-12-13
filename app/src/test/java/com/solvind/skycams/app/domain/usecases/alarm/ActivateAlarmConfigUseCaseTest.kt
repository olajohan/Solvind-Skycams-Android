package com.solvind.skycams.app.domain.usecases.alarm

import com.google.common.truth.Truth.assertThat
import com.solvind.skycams.app.core.Failure
import com.solvind.skycams.app.core.Resource
import com.solvind.skycams.app.domain.model.AlarmConfig
import com.solvind.skycams.app.domain.repo.IAlarmConfigRepo
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class ActivateAlarmConfigUseCaseTest {

    lateinit var mockGetAlarmConfigUseCase: GetAlarmConfigUseCase
    lateinit var mockAlarmConfigRepo: IAlarmConfigRepo

    @Before
    fun setup() {
        mockGetAlarmConfigUseCase = mockk()
        mockAlarmConfigRepo = mockk()
    }

    @Test
    fun `Returns alarmNotFoundFailure when alarm does not exist`() = runBlockingTest {
        val SKYCAM_KEY = "lyngennorth"
        val activateAlarmConfigUseCase = ActivateAlarmConfigUseCase(mockGetAlarmConfigUseCase, mockAlarmConfigRepo)
        coEvery { mockGetAlarmConfigUseCase.run(GetAlarmConfigUseCase.Params(SKYCAM_KEY)) } returns Resource.Error(Failure.AlarmNotFoundFailure)
        val result = activateAlarmConfigUseCase.run(ActivateAlarmConfigUseCase.Params(SKYCAM_KEY))

        assertThat(result is Resource.Error)
        assertThat((result as Resource.Error).failure == Failure.AlarmNotFoundFailure)
    }

    @Test
    fun `Returns alarmTimedOutFailure when the alarm has timed out`() = runBlockingTest {
        val SKYCAM_KEY = "lyngennorth"
        val ALARM_CONFIG = AlarmConfig(SKYCAM_KEY, 100L, false)
        val activateAlarmConfigUseCase = ActivateAlarmConfigUseCase(mockGetAlarmConfigUseCase, mockAlarmConfigRepo)

        coEvery { mockGetAlarmConfigUseCase.run(GetAlarmConfigUseCase.Params(SKYCAM_KEY)) } returns Resource.Success(ALARM_CONFIG)

        val result = activateAlarmConfigUseCase.run(ActivateAlarmConfigUseCase.Params(SKYCAM_KEY))

        assertThat(result is Resource.Error)
        assertThat((result as Resource.Error).failure == Failure.AlarmTimedOutFailure)
    }

    @Test
    fun `Returns failedToSetAlarmFailure when failing to set alarm status to active`() = runBlockingTest {
        val SKYCAM_KEY = "lyngennorth"
        val ALARM_CONFIG = AlarmConfig(SKYCAM_KEY, 9999999999999L, false)
        val activateAlarmConfigUseCase = ActivateAlarmConfigUseCase(mockGetAlarmConfigUseCase, mockAlarmConfigRepo)

        coEvery {
            mockGetAlarmConfigUseCase.run(GetAlarmConfigUseCase.Params(SKYCAM_KEY))
        } returns Resource.Success(ALARM_CONFIG)

        coEvery {
            mockAlarmConfigRepo.setAlarmConfig(ALARM_CONFIG.skycamKey, ALARM_CONFIG.alarmAvailableUntilEpochSeconds, true)
        } returns Resource.Error(Failure.FailedToSetAlarmFailure)

        val result = activateAlarmConfigUseCase.run(ActivateAlarmConfigUseCase.Params(SKYCAM_KEY))

        assertThat(result is Resource.Error)
        assertThat((result as Resource.Error).failure == Failure.FailedToSetAlarmFailure)
    }

    @Test
    fun `Sets the alarmConfig isActive field to true`() = runBlockingTest {
        val SKYCAM_KEY = "lyngennorth"
        val ALARM_CONFIG = AlarmConfig(SKYCAM_KEY, 9999999999999L, false)
        val activateAlarmConfigUseCase = ActivateAlarmConfigUseCase(mockGetAlarmConfigUseCase, mockAlarmConfigRepo)

        coEvery {
            mockGetAlarmConfigUseCase.run(GetAlarmConfigUseCase.Params(SKYCAM_KEY))
        } returns Resource.Success(ALARM_CONFIG)

        val isActiveParameter = slot<Boolean>()

        coEvery {
            mockAlarmConfigRepo.setAlarmConfig(any(), any(), capture(isActiveParameter))
        } returns Resource.Error(Failure.FailedToSetAlarmFailure)

        activateAlarmConfigUseCase.run(ActivateAlarmConfigUseCase.Params(SKYCAM_KEY))

        assertThat(isActiveParameter.captured)
    }

    @Test
    fun `Returns resource success when alarm has been set to active`() = runBlockingTest {
        val SKYCAM_KEY = "lyngennorth"
        val ALARM_CONFIG = AlarmConfig(SKYCAM_KEY, 9999999999999L, false)
        val activateAlarmConfigUseCase = ActivateAlarmConfigUseCase(mockGetAlarmConfigUseCase, mockAlarmConfigRepo)

        coEvery {
            mockGetAlarmConfigUseCase.run(GetAlarmConfigUseCase.Params(SKYCAM_KEY))
        } returns Resource.Success(ALARM_CONFIG)

        coEvery {
            mockAlarmConfigRepo.setAlarmConfig(ALARM_CONFIG.skycamKey, ALARM_CONFIG.alarmAvailableUntilEpochSeconds, true)
        } returns Resource.Success(Unit)

        val result = activateAlarmConfigUseCase.run(ActivateAlarmConfigUseCase.Params(SKYCAM_KEY))

        assertThat(result is Resource.Success)
    }
}