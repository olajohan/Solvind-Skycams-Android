package com.lyngennorth.solvindskycams.login.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lyngennorth.solvindskycams.auth.domain.model.User
import com.lyngennorth.solvindskycams.auth.domain.usecases.GetCurrentUser
import com.lyngennorth.solvindskycams.auth.domain.usecases.SignInAnonymous
import com.lyngennorth.solvindskycams.common.Event
import com.lyngennorth.solvindskycams.common.Failure
import com.lyngennorth.solvindskycams.common.usecases.UseCase
import kotlinx.coroutines.launch

class LoginViewModel(
    private val getCurrentUser: GetCurrentUser,
    private val signInAnonymous: SignInAnonymous
) : ViewModel() {

    private val _navigateToHome = MutableLiveData<Event<Failure?>>()
    val navigateToHome: LiveData<Event<Failure?>>
        get() = _navigateToHome

    init {
        viewModelScope.launch {
            getCurrentUser(this, UseCase.None()) {
                it.either(::handleGetCurrentUserFailure, ::handleGetCurrentUserSuccess)
            }
        }
    }

    fun signInUserAnonymously() = viewModelScope.launch {
        signInAnonymous(this, UseCase.None()) {
            it.either(::handleSignInUserAnonymouslyFailure, ::handleSignInUserAnonymouslySuccess)
        }
    }

    private fun handleGetCurrentUserSuccess(user: User) {
        _navigateToHome.value = Event(null)
    }
    private fun handleGetCurrentUserFailure(failure: Failure) {}

    private fun handleSignInUserAnonymouslySuccess(u: Unit) {
        _navigateToHome.value = Event(null)
    }
    private fun handleSignInUserAnonymouslyFailure(failure: Failure) {}
}