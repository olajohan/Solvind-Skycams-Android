package com.solvindskycams.app.presentation.list

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.solvind.skycams.app.core.Failure
import com.solvind.skycams.app.core.Resource
import com.solvind.skycams.app.core.UseCase
import com.solvind.skycams.app.domain.repo.IAlarmRepo
import com.solvind.skycams.app.domain.repo.ISkycamRepo
import com.solvind.skycams.app.domain.usecases.*
import com.solvind.skycams.app.presentation.home.HomeViewModel
import com.solvindskycams.app.MainCoroutineRule
import com.solvindskycams.app.domain.usecases.getSkycam
import com.solvindskycams.app.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class HomeViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var skycamRepo: ISkycamRepo

    @Mock
    private lateinit var alarmRepo: IAlarmRepo

    private lateinit var getAllSkycamsUseCase: GetAllSkycamsUseCase
    private lateinit var getSkycamFlowUseCase: GetSkycamFlowUseCase
    private lateinit var activateAlarmUseCase: ActivateAlarmUseCase
    private lateinit var deactivateAlarmUseCase: DeactivateAlarmUseCase
    private lateinit var getAlarmUseCase: GetAlarmUseCase
    private lateinit var getAlarmFlowUseCase: GetAlarmFlowUseCase
    private val testCoroutineDispatcher = TestCoroutineDispatcher()

    @Before
    fun setup() {
        getSkycamFlowUseCase = GetSkycamFlowUseCase(skycamRepo)
        getAlarmFlowUseCase = GetAlarmFlowUseCase(alarmRepo)
        getAlarmUseCase = GetAlarmUseCase(alarmRepo, testCoroutineDispatcher)
        activateAlarmUseCase = ActivateAlarmUseCase(alarmRepo, getAlarmUseCase, testCoroutineDispatcher)
        deactivateAlarmUseCase = DeactivateAlarmUseCase(alarmRepo, getAlarmUseCase, testCoroutineDispatcher)
        getAllSkycamsUseCase = GetAllSkycamsUseCase(skycamRepo, testCoroutineDispatcher)
    }

    @After
    fun tearDown() {
        testCoroutineDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun refreshSkycamList_setsViewStateLoadingToSuccess() = testCoroutineDispatcher.runBlockingTest {
        val returningSkycamList = listOf(
            getSkycam("lyngennorth"),
            getSkycam("skaidi"),
            getSkycam("reisafjord")
        )
        `when`(skycamRepo.getAllSkycams()).thenReturn(Resource.Success(returningSkycamList))

        testCoroutineDispatcher.pauseDispatcher()
        val viewModel = HomeViewModel(
            getAllSkycamsUseCase,
            getSkycamFlowUseCase,
            getAlarmFlowUseCase,
            activateAlarmUseCase,
            deactivateAlarmUseCase
            )

        val loadingResult = viewModel.mainViewStateReadOnly.getOrAwaitValue()
        assertThat(loadingResult).isEqualTo(HomeViewModel.MainViewState.Loading)

        testCoroutineDispatcher.resumeDispatcher()
        val successResult = viewModel.mainViewStateReadOnly.getOrAwaitValue()
        assertThat(successResult).isEqualTo(HomeViewModel.MainViewState.Success(returningSkycamList))
    }

    @Test
    fun refreshSkycamList_setsViewStateLoadingtoFailure() = testCoroutineDispatcher.runBlockingTest {
        `when`(getAllSkycamsUseCase.run(UseCase.None())).thenReturn(Resource.Error(Failure.EmptySkycamListFailure))


        `when`(skycamRepo.getAllSkycams()).thenReturn(Resource.Error(Failure.EmptySkycamListFailure))

        testCoroutineDispatcher.pauseDispatcher()
        val viewModel = HomeViewModel(
            getAllSkycamsUseCase,
            getSkycamFlowUseCase,
            getAlarmFlowUseCase,
            activateAlarmUseCase,
            deactivateAlarmUseCase
        )
        assertThat(viewModel.mainViewStateReadOnly.getOrAwaitValue()).isEqualTo(HomeViewModel.MainViewState.Loading)

        testCoroutineDispatcher.resumeDispatcher()
        assertThat(viewModel.mainViewStateReadOnly.getOrAwaitValue()).isEqualTo(HomeViewModel.MainViewState.Failed(
            Failure.EmptySkycamListFailure))
    }

}