package com.solvind.skycams.app.presentation

import androidx.lifecycle.LiveData
import com.solvind.skycams.app.domain.model.Alarm
import kotlinx.coroutines.Job

interface IHandleAlarm {

    fun activateAlarm(skycamKey: String) : Job
    fun deactivateAlarm(skycamKey: String): Job
    fun getAlarmLiveData(skycamKey: String) : LiveData<Alarm>
}