package com.lyngennorth.solvindskycams.skycam.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lyngennorth.solvindskycams.auth.domain.usecases.GetCurrentUser
import com.lyngennorth.solvindskycams.auth.domain.usecases.SignInAnonymous
import javax.inject.Inject

class HomeViewModelFactory @Inject constructor(
    private val getCurrentUser: GetCurrentUser
): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return HomeViewModel(getCurrentUser) as T
    }
}