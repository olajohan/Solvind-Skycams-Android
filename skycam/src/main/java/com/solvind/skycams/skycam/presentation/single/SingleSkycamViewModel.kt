package com.solvind.skycams.skycam.presentation.single

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.solvind.skycams.skycam.domain.usecases.GetAlarmUseCase
import com.solvind.skycams.skycam.domain.usecases.UpdateAlarmTimeUseCase

class SingleSkycamViewModel(
    private val getAlarmUseCase: GetAlarmUseCase,
    private val updateAlarmTimeUseCase: UpdateAlarmTimeUseCase
) : ViewModel() {

    private val _isSkycamAlarmActive = MutableLiveData<Boolean>()
    val isSkycamAlarmActive: LiveData<Boolean>
        get() = _isSkycamAlarmActive

}