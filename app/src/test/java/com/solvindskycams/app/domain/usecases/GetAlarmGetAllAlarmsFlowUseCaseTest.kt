package com.solvindskycams.app.domain.usecases

import com.google.common.truth.Truth.assertThat
import com.solvind.skycams.app.core.Resource
import com.solvind.skycams.app.domain.repo.IAlarmRepo
import com.solvind.skycams.app.domain.usecases.GetAlarmUseCase
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
class GetAlarmGetAllAlarmsFlowUseCaseTest {

    @Mock
    private lateinit var repo: IAlarmRepo
    private lateinit var usecase: GetAlarmUseCase

    private val testDispatcher = TestCoroutineDispatcher()

    @After
    fun cleanup() {
        testDispatcher.cleanupTestCoroutines()
    }

    @Before
    fun setupUseCase() {
        usecase = GetAlarmUseCase(repo, testDispatcher)
    }

    @Test
    fun getAlarm_emptySkycamKey_returnsResourceError() = runBlockingTest {
        val TEST_SKYCAM_KEY = ""
        val TEST_PARAMS = GetAlarmUseCase.Params(TEST_SKYCAM_KEY)

        val result = usecase.run(TEST_PARAMS)
        assertThat(result is Resource.Error).isTrue()
        assertThat((result as Resource.Error).failure)
    }
}