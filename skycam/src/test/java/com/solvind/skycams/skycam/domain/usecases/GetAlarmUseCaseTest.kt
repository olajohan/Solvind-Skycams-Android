package com.solvind.skycams.skycam.domain.usecases

import com.google.common.truth.Truth.assertThat
import com.solvind.skycams.skycam.Result
import com.solvind.skycams.skycam.domain.repo.IAlarmRepo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class GetAlarmUseCaseTest {

    @Mock
    private lateinit var repo: IAlarmRepo

    private lateinit var usecase: GetAlarmUseCase

    @Before
    fun setupUseCase() {
        usecase = GetAlarmUseCase(repo)
    }

    @Test
    fun getAlarm_emptySkycamKey_returnsResourceError() = runBlockingTest {
        val TEST_SKYCAM_KEY = ""
        val TEST_PARAMS = GetAlarmUseCase.Params(TEST_SKYCAM_KEY)

        val result = usecase.run(TEST_PARAMS)
        assertThat(result is Result.Error).isTrue()
        assertThat((result as Result.Error).failure)
    }
}