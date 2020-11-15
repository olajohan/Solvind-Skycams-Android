package com.solvind.skycams.app.domain.usecases

import com.google.common.truth.Truth.assertThat
import com.solvind.skycams.app.core.Resource
import com.solvind.skycams.app.domain.repo.IAlarmConfigRepo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class GetAlarmConfigGetAllAlarmsFlowUseCaseTest {

    @Mock
    private lateinit var configRepo: IAlarmConfigRepo
    private lateinit var usecase: GetAlarmConfigUseCase

    private val testDispatcher = TestCoroutineDispatcher()

    @After
    fun cleanup() {
        testDispatcher.cleanupTestCoroutines()
    }

    @Before
    fun setupUseCase() {
        usecase = GetAlarmConfigUseCase(configRepo, testDispatcher, testDispatcher)
    }

    @Test
    fun getAlarm_emptySkycamKey_returnsResourceError() = runBlockingTest {
        val TEST_SKYCAM_KEY = ""
        val TEST_PARAMS = GetAlarmConfigUseCase.Params(TEST_SKYCAM_KEY)

        val result = usecase.run(TEST_PARAMS)
        assertThat(result is Resource.Error).isTrue()
        assertThat((result as Resource.Error).failure)
    }
}