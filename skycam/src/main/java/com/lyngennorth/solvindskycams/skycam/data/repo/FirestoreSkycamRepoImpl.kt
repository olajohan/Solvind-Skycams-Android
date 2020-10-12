package com.lyngennorth.solvindskycams.skycam.data.repo

import com.google.firebase.firestore.FirebaseFirestore
import com.lyngennorth.solvindskycams.common.Either
import com.lyngennorth.solvindskycams.common.Failure
import com.lyngennorth.solvindskycams.skycam.domain.model.Skycam
import com.lyngennorth.solvindskycams.skycam.domain.repo.ISkycamRepo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FirestoreSkycamRepoImpl @Inject constructor(private val firestore: FirebaseFirestore) :
    ISkycamRepo {
    override suspend fun getAllSkycams(): Either<Failure, List<Skycam>> {
        TODO("Not yet implemented")
    }

    override suspend fun getSkycam(skycamKey: String): Either<Failure, Skycam> {
        TODO("Not yet implemented")
    }

    override suspend fun getSkycamFlow(skycamKey: String): Either<Failure, Flow<Skycam>> {
        TODO("Not yet implemented")
    }
}