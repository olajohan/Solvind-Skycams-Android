package com.solvind.skycams.app.presentation

import androidx.lifecycle.LiveData
import com.solvind.skycams.app.domain.model.Skycam

interface IProvideSkycamLiveData {
    fun getSkycamLiveData(skycamKey: String) : LiveData<Skycam>
}