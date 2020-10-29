package com.solvind.skycams.app.presentation.single.tabs.live

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.solvind.skycams.app.domain.usecases.GetSkycamFlowUseCase

class SkycamLiveViewModel @ViewModelInject constructor(
    private val getSkycamFlowUseCase: GetSkycamFlowUseCase
) : ViewModel() {}