package com.solvind.skycams.app.presentation.listener

import android.view.View
import android.widget.ToggleButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.solvind.skycams.app.presentation.IHandleAlarm

class AlarmOnClickListener(
    private val alarmHandler: IHandleAlarm,
    private val skycamKey: String
) : View.OnClickListener {
    override fun onClick(v: View?) {
        when (v) {
            is ToggleButton -> {
                if (v.isChecked) alarmHandler.activateAlarm(skycamKey)
                else alarmHandler.deactivateAlarm(skycamKey)
            }
            is FloatingActionButton -> {}
        }
    }
}