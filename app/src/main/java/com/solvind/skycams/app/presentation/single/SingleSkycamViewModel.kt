package com.solvind.skycams.app.presentation.single

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.solvind.skycams.app.domain.usecases.GetAlarmConfigUseCase
import com.solvind.skycams.app.domain.usecases.UpdateAlarmConfigTimeUseCase

class SingleSkycamViewModel @ViewModelInject constructor(
    private val getAlarmConfigUseCase: GetAlarmConfigUseCase,
    private val updateAlarmConfigTimeUseCase: UpdateAlarmConfigTimeUseCase,
    @Assisted private val args: SavedStateHandle
) : ViewModel() {

    private val _isSkycamAlarmActive = MutableLiveData<Boolean>()
    val isSkycamAlarmActive: LiveData<Boolean>
        get() = _isSkycamAlarmActive

}