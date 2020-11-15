package com.solvind.skycams.app.domain.usecases

import com.google.common.truth.Truth.assertThat
import com.solvind.skycams.app.MainCoroutineRule
import com.solvind.skycams.app.core.Failure
import com.solvind.skycams.app.core.Resource
import com.solvind.skycams.app.core.UseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class GetListOfAllSkycamFlowsPairedWithAlarmConfigFlowsTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var getAllSkycamsUseCase: GetAllSkycamsUseCase
    private lateinit var getSkycamFlowUseCase: GetSkycamFlowUseCase
    private lateinit var getAlarmConfigFlowUseCase: GetAlarmConfigFlowUseCase
    private lateinit var getListOfAllSkycamFlowsPairedWithAlarmFlows: GetListOfAllSkycamFlowsPairedWithAlarmFlows

    @Before
    fun setup() {

        getAllSkycamsUseCase = mockk()
        getSkycamFlowUseCase = mockk()
        getAlarmConfigFlowUseCase = mockk()

        getListOfAllSkycamFlowsPairedWithAlarmFlows = GetListOfAllSkycamFlowsPairedWithAlarmFlows(
            getAllSkycamsUseCase,
            getSkycamFlowUseCase,
            getAlarmConfigFlowUseCase,
            mainCoroutineRule.dispatcher,
            mainCoroutineRule.dispatcher
        )
    }

    @Test
    fun `Get all skycams error returns error`() = mainCoroutineRule.dispatcher.runBlockingTest  {
        coEvery { getAllSkycamsUseCase.run(any()) } returns  Resource.Error(Failure.EmptySkycamListFailure)
        val result = getListOfAllSkycamFlowsPairedWithAlarmFlows.run(UseCase.None())
        assertThat(result is Resource.Error)
    }

    @Test
    fun `Get skycamFlows and get alarmflows called the same amount of times as there are skycams`() = mainCoroutineRule.dispatcher.runBlockingTest {
        val TEST_SKYCAMS = listOf(
            getSkycam("lyngennorth"),
            getSkycam("skaidi"),
            getSkycam("tromso")
        )
        every {
            getAlarmConfigFlowUseCase.run(any())
            getSkycamFlowUseCase.run(any())
        } returns flow {}

        coEvery { getAllSkycamsUseCase.run(any()) } returns Resource.Success(TEST_SKYCAMS)
        getListOfAllSkycamFlowsPairedWithAlarmFlows.run(UseCase.None())

        verify(exactly = TEST_SKYCAMS.size) {
            getAlarmConfigFlowUseCase.run(any())
            getSkycamFlowUseCase.run(any())
        }
    }


}