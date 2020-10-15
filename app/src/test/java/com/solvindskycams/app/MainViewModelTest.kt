package com.solvindskycams.app

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.lyngennorth.solvindskycams.common.Either
import com.lyngennorth.solvindskycams.common.Failure
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MainViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val uiThread = newSingleThreadContext("UI thread")
    private val repo: IAuthenticatorRepo = mockk()
    private val getCurrentUser = GetCurrentUser(repo)
    private val viewModel = MainViewModel(getCurrentUser)


    @Before
    fun setUp() {
        Dispatchers.setMain(uiThread)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        uiThread.close()
    }


    /*
    * Sets the current user to the member variable _user of type livedata.value
    * */
    @Test
    fun loadUser_called_success() = runBlocking {
        val TEST_USER = User(
            uid = "7eoYnhqUCwSRnHR2ldehvXbN4ln2",
            created = 1602541085L,
            isAnonymous = false
        )
        coEvery {  repo.getCurrentUser() } returns Either.Right(TEST_USER)

        viewModel.loadUser()
        assertEquals(viewModel.user.getOrAwaitValue(), TEST_USER)
    }

    @Test
    fun loadUser_called_fail() = runBlocking {
        coEvery {  repo.getCurrentUser() } returns Either.Left(Failure.GetCurrentUserFailure)
        viewModel.loadUser()
        assertEquals(viewModel.user.getOrAwaitValue(), null)
    }
}