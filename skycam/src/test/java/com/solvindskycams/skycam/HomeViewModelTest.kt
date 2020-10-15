package com.solvindskycams.skycam

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.solvindskycams.common.Either
import com.solvindskycams.common.Failure
import com.solvindskycams.skycam.domain.repo.ISkycamRepo
import com.solvindskycams.skycam.domain.usecases.GetAllSkycamsUseCase
import com.solvindskycams.skycam.domain.usecases.GetSkycamFlowUseCase
import com.solvindskycams.skycam.presentation.list.HomeViewModel
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test


class HomeViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val uiThread = newSingleThreadContext("UI thread")
    private val repo: ISkycamRepo = mockk()
    private val getAllSkycamsUseCase = GetAllSkycamsUseCase(repo)
    private val getSkycamFlow = GetSkycamFlowUseCase(repo)
    private val viewModel = HomeViewModel(getAllSkycamsUseCase, getSkycamFlow)


    @Before
    fun setUp() {
        Dispatchers.setMain(uiThread)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        uiThread.close()
    }

    @Test
    fun getAllSkycams_called_success() {
        val TEST_SKYCAM_LIST = listOf(
            getSkycam(),
            getSkycam(skycamKey = "alta"),
            getSkycam(skycamKey = "skaidi"),
            getSkycam(skycamKey = "reisafjord")
        )
        coEvery { repo.getAllSkycams() } returns Either.Right(TEST_SKYCAM_LIST)
        viewModel.loadSkycams()
        assertEquals(viewModel.skycams.getOrAwaitValue(), TEST_SKYCAM_LIST)
    }

    @Test
    fun getAllSkycams_called_failure() {
        coEvery { repo.getAllSkycams() } returns Either.Left(Failure.LoadSingleSkycamFirestoreFailure)
        viewModel.loadSkycams()
        assertEquals(viewModel.skycams.getOrAwaitValue(), null)
    }
}