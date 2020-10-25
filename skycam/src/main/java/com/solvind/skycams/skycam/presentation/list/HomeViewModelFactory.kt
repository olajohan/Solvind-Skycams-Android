package com.solvind.skycams.skycam.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.solvind.skycams.skycam.domain.usecases.GetAllSkycamsUseCase
import com.solvind.skycams.skycam.domain.usecases.GetSkycamFlowUseCase
import javax.inject.Inject

class HomeViewModelFactory @Inject constructor(
    private val getAllSkycamsUseCase: GetAllSkycamsUseCase,
    private val getSkycamFlowUseCase: GetSkycamFlowUseCase
): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return HomeViewModel(getAllSkycamsUseCase, getSkycamFlowUseCase) as T
    }
}