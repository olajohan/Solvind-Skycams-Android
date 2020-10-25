package com.solvind.skycams.skycam.presentation.single.tabs.live

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solvind.skycams.skycam.domain.model.ImageInfo
import com.solvind.skycams.skycam.domain.usecases.GetSkycamFlowUseCase
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SkycamLiveViewModel(
    private val getSkycamFlowUseCase: GetSkycamFlowUseCase
) : ViewModel() {

    private val _liveImage = MutableLiveData<ImageInfo>()
    val liveImage: LiveData<ImageInfo>
        get() = _liveImage

    fun refreshLiveImage(skycamKey: String) = viewModelScope.launch {
        getSkycamFlowUseCase(GetSkycamFlowUseCase.Params(skycamKey)).collect {
            _liveImage.postValue(it.mostRecentImage)
        }
    }

}