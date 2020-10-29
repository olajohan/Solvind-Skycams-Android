package com.solvind.skycams.app.core

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
    object FailedToSetAlarmFailure : Failure()
    object FailedToGetAllAlarmsFailure : Failure()
    object DeactivateAllAlarmsUnknownFailure : Failure()
    object OneOrMoreAlarmsFailedToDeactivateFailure : Failure()
}

// Thrown inside flows if the userid is null
object UserIdIsNullException : Exception()