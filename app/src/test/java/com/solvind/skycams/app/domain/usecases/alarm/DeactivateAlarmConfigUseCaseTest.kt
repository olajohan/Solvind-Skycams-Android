package com.solvind.skycams.app.domain.usecases.alarm

import com.google.common.truth.Truth
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
class DeactivateAlarmConfigUseCaseTest {

    lateinit var mockGetAlarmConfigUseCase: GetAlarmConfigUseCase
    lateinit var mockAlarmConfigRepo: IAlarmConfigRepo

    @Before
    fun setup() {
        mockGetAlarmConfigUseCase = mockk()
        mockAlarmConfigRepo = mockk()
    }

    @Test
    fun `Sets alarm to deactivated state`() = runBlockingTest {
        val SKYCAM_KEY = "lyngennorth"
        val ALARM_CONFIG = AlarmConfig(SKYCAM_KEY, 9999999999999L, true)
        val deactivateAlarmConfigUseCase = DeactivateAlarmConfigUseCase(mockGetAlarmConfigUseCase, mockAlarmConfigRepo)

        coEvery {
            mockGetAlarmConfigUseCase.run(GetAlarmConfigUseCase.Params(SKYCAM_KEY))
        } returns Resource.Success(ALARM_CONFIG)

        val isActiveParameter = slot<Boolean>()

        coEvery {
            mockAlarmConfigRepo.setAlarmConfig(any(), any(), capture(isActiveParameter))
        } returns Resource.Error(Failure.FailedToSetAlarmFailure)

        deactivateAlarmConfigUseCase.run(DeactivateAlarmConfigUseCase.Params(SKYCAM_KEY))

        Truth.assertThat(!isActiveParameter.captured)
    }
}