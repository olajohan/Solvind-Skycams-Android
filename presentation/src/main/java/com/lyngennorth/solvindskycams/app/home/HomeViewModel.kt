package com.lyngennorth.solvindskycams.app.home

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lyngennorth.solvindskycams.domain.Failure
import com.lyngennorth.solvindskycams.domain.domainmodel.Skycam
import com.lyngennorth.solvindskycams.domain.usecases.GetAllSkycams
import com.lyngennorth.solvindskycams.domain.usecases.UseCase
import kotlinx.coroutines.launch

class HomeViewModel @ViewModelInject constructor(
    private val getAllSkycams: GetAllSkycams,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    fun getAllSkycams() = viewModelScope.launch {
        getAllSkycams(this, UseCase.None()) {
            it.either(::handleError, ::handleSuccess)
        }
    }

    private fun handleError(failure: Failure) {}
    private fun handleSuccess(skycamList: List<Skycam>) {}
}