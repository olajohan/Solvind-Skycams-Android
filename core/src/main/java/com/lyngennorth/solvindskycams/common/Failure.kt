package com.lyngennorth.solvindskycams.common

sealed class Failure {
    object NetworkConnection : Failure()
    object ServerError : Failure()

    /** * Extend this class for feature specific failures.*/
    abstract class FeatureFailure: Failure()

    object LoginFailure : FeatureFailure()
    object SignOutFailure : FeatureFailure()
    object UserNotFoundFailure: FeatureFailure()
    object AccountDataNotFoundFailure: FeatureFailure()
    object GetCurrentUserFailure: FeatureFailure()
}