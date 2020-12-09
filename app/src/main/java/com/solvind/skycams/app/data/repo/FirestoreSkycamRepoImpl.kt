package com.solvind.skycams.app.data.repo


import com.google.firebase.firestore.FirebaseFirestore
import com.solvind.skycams.app.core.Failure
import com.solvind.skycams.app.core.Resource
import com.solvind.skycams.app.data.repo.mappers.SnapshotToSkycamMapper
import com.solvind.skycams.app.domain.model.Skycam
import com.solvind.skycams.app.domain.repo.ISkycamRepo
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
    override suspend fun getAllSkycams(): Resource<List<Skycam>> {
        val result = firestore.collection(SKYCAM_COLLECTION).get()
        val collection = result.await()
        if (collection.isEmpty) return Resource.Error(Failure.EmptySkycamListFailure)
        return Resource.Success(mapper.listFromLeftToRight(collection.documents))
    }

    override fun getAllSkycamsFlow(): Flow<Skycam> = callbackFlow {
        val collectionQuery = firestore.collection(SKYCAM_COLLECTION)

        val subscription = collectionQuery.addSnapshotListener { querySnapshot, error ->
            if (error != null) {
                throw error
            }

            if (querySnapshot != null && !querySnapshot.isEmpty) {
                querySnapshot.documents.forEach {
                    offer(mapper.singleFromLeftToRight(it))
                }
                close()
            }
        }
        awaitClose { subscription.remove() }
    }

    override suspend fun getSkycam(skycamKey: String): Resource<Skycam> {
        val result = firestore.collection(SKYCAM_COLLECTION).document(skycamKey).get()
        val document = result.await()
        if (!document.exists()) return Resource.Error(Failure.SkycamNotFoundFailure)
        return Resource.Success(mapper.singleFromLeftToRight(document))
    }

    override fun getSkycamFlow(skycamKey: String): Flow<Skycam> = callbackFlow {
        val document = firestore.collection(SKYCAM_COLLECTION).document(skycamKey)
        val listener = document.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                throw exception
            }
            if (snapshot != null) {
                offer(mapper.singleFromLeftToRight(snapshot))
            }
        }
        awaitClose {
            listener.remove()
        }
    }

}