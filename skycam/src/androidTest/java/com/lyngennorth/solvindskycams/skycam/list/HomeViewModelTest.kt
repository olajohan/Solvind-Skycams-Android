package com.lyngennorth.solvindskycams.skycam.list

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.FirebaseAuth
import com.lyngennorth.solvindskycams.auth.data.FirebaseAuthenticatorRepoImpl
import com.lyngennorth.solvindskycams.auth.domain.model.User
import com.lyngennorth.solvindskycams.auth.domain.usecases.GetCurrentUser
import com.lyngennorth.solvindskycams.skycam.presentation.list.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    private val repo = FirebaseAuthenticatorRepoImpl(FirebaseAuth.getInstance())
    private val useCase = GetCurrentUser(repo)
    private val viewModel = HomeViewModel(useCase)

    @Before
    fun setUpViewModel() {
        Dispatchers.setMain(mainThreadSurrogate)
    }
    @After
    fun tearDown() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        mainThreadSurrogate.close()
    }


    @Test
    fun postFailureToUserLiveData() = runBlocking {
        viewModel.user.observeForever()
    }

}