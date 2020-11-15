package com.solvind.skycams.app.domain.usecases

import com.google.common.truth.Truth.assertThat
import com.solvind.skycams.app.core.Failure
import com.solvind.skycams.app.core.Resource
import com.solvind.skycams.app.domain.model.AlarmConfig
import com.solvind.skycams.app.domain.repo.IAlarmConfigRepo
import io.mockk.coVerify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import java.time.Instant
import java.util.concurrent.TimeUnit

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class ActivateAlarmConfigUseCaseTest {

    @Mock
    private lateinit var alarmConfigRepo: IAlarmConfigRepo

    private lateinit var activateAlarmUseCase: ActivateAlarmConfigUseCase
    private lateinit var getAlarmConfigUseCase: GetAlarmConfigUseCase

    private val testDispatcher = TestCoroutineDispatcher()

    @After
    fun cleanup() {
        testDispatcher.cleanupTestCoroutines()
    }

    @Before
    fun setup() {
        getAlarmConfigUseCase = GetAlarmConfigUseCase(alarmConfigRepo, testDispatcher, testDispatcher)
        activateAlarmUseCase = ActivateAlarmConfigUseCase(testDispatcher, testDispatcher, getAlarmConfigUseCase, alarmConfigRepo)
    }

    @Test
    fun activateAlarm_emptySkycamKey_returnsResourceError() = runBlockingTest {
        val TEST_SKYCAM_KEY = ""
        val params = ActivateAlarmConfigUseCase.Params(TEST_SKYCAM_KEY)
        val result = activateAlarmUseCase.run(params)
        assertThat((result as Resource.Error).failure).isEqualTo(Failure.EmptySkycamKeyFailure)
    }

    @Test
    fun setAlarmStatus_isActiveTrue_keepOtherParamsTheSameAndIsActiveTrue() = runBlockingTest {
        val TEST_SKYCAM_KEY = "lyngennorth"
        val TEST_CURRENT_ALARM = AlarmConfig(TEST_SKYCAM_KEY, Instant.now().epochSecond + TimeUnit.MINUTES.toSeconds(30L), true)
        val setAlarmParams = ActivateAlarmConfigUseCase.Params(TEST_SKYCAM_KEY)
        coVerify {  }
        activateAlarmUseCase.run(setAlarmParams)

    }
    
}
