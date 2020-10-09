package com.lyngennorth.solvindskycams.androiddata.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.lyngennorth.solvindskycams.domain.Either
import com.lyngennorth.solvindskycams.domain.Failure
import com.lyngennorth.solvindskycams.domain.domainmodel.Skycam
import com.lyngennorth.solvindskycams.domain.repositories.ISkycamRepository
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject

class FirestoreSkycamRepositoryImpl @Inject constructor() : ISkycamRepository {
    override suspend fun getAllSkycams(): Either<Failure, List<Skycam>> {
        TODO("LOLOLO")
    }

    override suspend fun getSkycam(skycamKey: String): Either<Failure, Skycam> {
        TODO("Not yet implemented")
    }

    override suspend fun getSkycamFlow(skycamKey: String): Either<Failure, Flow<Skycam>> {
        TODO("Not yet implemented")
    }
}