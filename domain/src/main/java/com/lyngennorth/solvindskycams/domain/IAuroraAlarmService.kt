package com.lyngennorth.solvindskycams.domain

import java.lang.Exception

interface IAuroraAlarmService {
    fun startListeningToSkycam(skycamKey: String): Either<Exception, Unit>
    fun stoplListeningToSkycam(skycamKey: String): Either<Exception, Unit>
    fun stopListeningToAllSkycams(): Either<Exception, Unit>
    fun startAlarm(): Either<Exception, Unit>
    fun stopAlarm(): Either<Exception, Unit>
}