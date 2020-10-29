package com.solvind.skycams.app.presentation.single

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.solvind.skycams.app.domain.usecases.GetAlarmUseCase
import com.solvind.skycams.app.domain.usecases.UpdateAlarmTimeUseCase

class SingleSkycamViewModel @ViewModelInject constructor(
    private val getAlarmUseCase: GetAlarmUseCase,
    private val updateAlarmTimeUseCase: UpdateAlarmTimeUseCase,
    @Assisted private val args: SavedStateHandle
) : ViewModel() {

    private val _isSkycamAlarmActive = MutableLiveData<Boolean>()
    val isSkycamAlarmActive: LiveData<Boolean>
        get() = _isSkycamAlarmActive

}