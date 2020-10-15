package com.solvindskycams.common

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
    object LoadSingleSkycamFirestoreFailure : Failure()
    object FirestoreConnectionFailure : Failure()
    object EmptySkycamListFailure : Failure()
    object SkycamNotFoundFailure : Failure()
    object GetAllSkycamsFirestoreFailure : Failure()
    object GetFirestoreSkycamFlowFailure : Failure()
}