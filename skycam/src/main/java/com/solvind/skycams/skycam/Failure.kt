package com.solvind.skycams.skycam

sealed class Failure {

    object UserIdNullFailure : Failure()
    object AlarmNotFoundFailure : Failure()
    object UpdateAlarmTimeLessThanZeroFailure : Failure()
    object UpdateAlarmTimeUnknownFailure : Failure()
    object ImageNotFoundFailure : Failure()
    object EmptySkycamListFailure : Failure()
    object SkycamNotFoundFailure : Failure()
    object EmptySkycamKeyFailure : Failure()
    object EmptyImageIdFailure : Failure()

}