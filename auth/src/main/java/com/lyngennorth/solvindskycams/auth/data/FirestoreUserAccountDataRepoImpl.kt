package com.lyngennorth.solvindskycams.auth.data

import com.google.firebase.firestore.FirebaseFirestore
import com.lyngennorth.solvindskycams.auth.domain.model.AccountData
import com.lyngennorth.solvindskycams.auth.domain.repo.IUserAccountDataRepo
import com.lyngennorth.solvindskycams.common.Either
import com.lyngennorth.solvindskycams.common.Failure
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreUserAccountDataRepoImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : IUserAccountDataRepo {

    override suspend fun getUserAccountData(uid: String): Either<Failure, AccountData> {
        val firestoreAccountData = firestore.collection("users").document(uid).get().await()
        val accountData = firestoreAccountData.toObject(AccountData::class.java)
        return if (accountData != null) Either.Right(accountData) else Either.Left(Failure.AccountDataNotFoundFailure)
    }

}