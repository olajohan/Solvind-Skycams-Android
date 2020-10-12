package com.lyngennorth.solvindskycams.login.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lyngennorth.solvindskycams.auth.domain.usecases.GetCurrentUser
import com.lyngennorth.solvindskycams.auth.domain.usecases.SignInAnonymous
import javax.inject.Inject

class LoginViewModelFactory @Inject constructor(
    private val getCurrentUser: GetCurrentUser,
    private val signInAnonymous: SignInAnonymous
): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return LoginViewModel(getCurrentUser, signInAnonymous) as T
    }
}