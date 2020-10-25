package com.solvind.skycams.skycam.presentation.single

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.solvind.skycams.skycam.domain.usecases.GetAlarmUseCase
import com.solvind.skycams.skycam.domain.usecases.UpdateAlarmTimeUseCase
import javax.inject.Inject

class SingleSkycamViewModelFactory @Inject constructor(
    private val getAlarmUseCase: GetAlarmUseCase,
    private val updateAlarmTimeUseCase: UpdateAlarmTimeUseCase
): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SingleSkycamViewModel(getAlarmUseCase, updateAlarmTimeUseCase) as T
    }
}