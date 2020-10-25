package com.solvind.skycams.skycam.presentation.single.tabs.live

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.solvind.skycams.skycam.domain.usecases.GetSkycamFlowUseCase
import javax.inject.Inject

class SkycamLiveViewModelFactory @Inject constructor(
    private val getSkycamFlowUseCase: GetSkycamFlowUseCase
): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SkycamLiveViewModel(getSkycamFlowUseCase) as T
    }
}