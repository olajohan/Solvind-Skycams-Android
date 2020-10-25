package com.solvind.skycams.skycam.data.repo


import com.google.firebase.firestore.FirebaseFirestore
import com.solvind.skycams.skycam.Failure
import com.solvind.skycams.skycam.Result
import com.solvind.skycams.skycam.data.repo.mappers.SnapshotToSkycamMapper
import com.solvind.skycams.skycam.domain.model.Skycam
import com.solvind.skycams.skycam.domain.repo.ISkycamRepo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

private const val SKYCAM_COLLECTION = "skycams"

@ExperimentalCoroutinesApi
class FirestoreSkycamRepoImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val mapper: SnapshotToSkycamMapper
) : ISkycamRepo {
    override suspend fun getAllSkycams(): Result<List<Skycam>> {
        val result = firestore.collection(SKYCAM_COLLECTION).get()
        val collection = result.await()
        if (collection.isEmpty) return Result.Error(Failure.EmptySkycamListFailure)
        return Result.Success(mapper.listFromLeftToRight(collection.documents))
    }

    override suspend fun getSkycam(skycamKey: String): Result<Skycam> {
        val result = firestore.collection(SKYCAM_COLLECTION).document(skycamKey).get()
        val document = result.await()
        if (!document.exists()) return Result.Error(Failure.SkycamNotFoundFailure)
        return Result.Success(mapper.singleFromLeftToRight(document))
    }

    override fun getSkycamFlow(skycamKey: String): Flow<Skycam> = callbackFlow {
        val document = firestore.collection(SKYCAM_COLLECTION).document(skycamKey)
        val listener = document.addSnapshotListener { snapshot, exception ->
            if (exception != null) throw exception
            if (snapshot != null) offer(mapper.singleFromLeftToRight(snapshot))
        }
        awaitClose {
            listener.remove()
        }
    }
}