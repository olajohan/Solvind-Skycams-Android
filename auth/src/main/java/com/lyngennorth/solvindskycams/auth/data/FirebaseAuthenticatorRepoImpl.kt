package com.lyngennorth.solvindskycams.auth.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.lyngennorth.solvindskycams.auth.domain.model.User
import com.lyngennorth.solvindskycams.auth.domain.repo.IAuthenticatorRepo
import com.lyngennorth.solvindskycams.common.Either
import com.lyngennorth.solvindskycams.common.Failure
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import javax.inject.Inject

class FirebaseAuthenticatorRepoImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
): IAuthenticatorRepo {

    override suspend fun signInAnonymously(): Either<Failure, Unit> {
        val firebaseAuthSignInResult = firebaseAuth.signInAnonymously().await()
        return if (firebaseAuthSignInResult.user != null) Either.Right(Unit)
        else Either.Left(Failure.LoginFailure)
    }

    override suspend fun signOut(): Either<Failure, Unit>  = try {
        firebaseAuth.signOut()
        Either.Right(Unit)
    } catch (e: Exception) {
        Either.Left(Failure.SignOutFailure)
    }

    override suspend fun getCurrentUser(): Either<Failure, User> {
        val firebaseUser = firebaseAuth.currentUser
        return if (firebaseUser == null) Either.Left(Failure.UserNotFoundFailure)
        else Either.Right(
            User(
                uid = firebaseUser.uid,
                created = firebaseUser.metadata?.creationTimestamp ?: 0L,
                isAnonymous = firebaseUser.isAnonymous
        ))
    }
}