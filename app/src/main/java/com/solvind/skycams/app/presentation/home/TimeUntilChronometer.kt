package com.solvind.skycams.app.presentation.home

import android.content.Context
import android.text.format.DateUtils
import android.util.AttributeSet
import android.widget.Chronometer
import java.time.Instant
import java.util.concurrent.TimeUnit

class TimeUntilChronometer(context: Context, attributeSet: AttributeSet) : Chronometer(context, attributeSet) {

    init { isCountDown = true }

    fun reset(epochSeconds: Long) {
        stop()

        setOnChronometerTickListener {
            text =if (epochSeconds < Instant.now().epochSecond) {
                stop()
                "--:--"
            } else {
                DateUtils.getRelativeTimeSpanString(
                    TimeUnit.SECONDS.toMillis(epochSeconds),
                    Instant.now().toEpochMilli(),
                    DateUtils.SECOND_IN_MILLIS
                )
            }

        }
        start()
    }
}