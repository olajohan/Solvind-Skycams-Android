package com.lyngennorth.solvindskycams.skycam.presentation.list

import androidx.lifecycle.*
import com.lyngennorth.solvindskycams.auth.domain.model.User
import com.lyngennorth.solvindskycams.auth.domain.usecases.GetCurrentUser
import com.lyngennorth.solvindskycams.common.Either
import com.lyngennorth.solvindskycams.common.Failure
import com.lyngennorth.solvindskycams.common.usecases.UseCase
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getCurrentUser: GetCurrentUser
) : ViewModel() {

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?>
        get() = _user

    fun loadUser() = viewModelScope.launch {
        getCurrentUser(this, UseCase.None()) {

        }
    }
}