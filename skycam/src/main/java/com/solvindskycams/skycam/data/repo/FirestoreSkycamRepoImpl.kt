package com.solvindskycams.skycam.data.repo


import com.google.firebase.firestore.FirebaseFirestore
import com.solvindskycams.common.Either
import com.solvindskycams.common.Failure
import com.solvindskycams.skycam.domain.model.Skycam
import com.solvindskycams.skycam.domain.repo.ISkycamRepo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

class FirestoreSkycamRepoImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val mapper: DocumentSnapshotToSkycamMapper
) : ISkycamRepo {

    override suspend fun getAllSkycams(): Either<Failure, List<Skycam>> = try {
        val skycamQuerySnapshot = firestore.collection("skycams").get().await()
        val skycamList = mapper.listFromLeftToRight(skycamQuerySnapshot.documents)
        if (skycamList.isEmpty()) Either.Left(Failure.EmptySkycamListFailure)
        else Either.Right(skycamList)

    } catch (e: Exception) {
        Either.Left(Failure.GetAllSkycamsFirestoreFailure)
    }


    override suspend fun getSkycam(skycamKey: String): Either<Failure, Skycam> = try {
        val documentSnapshot = firestore.collection("skycams").document(skycamKey).get().await()
        if (documentSnapshot.exists()) {
            val skycam = mapper.singleFromLeftToRight(documentSnapshot)
            Either.Right(skycam)
        } else Either.Left(Failure.SkycamNotFoundFailure)
    } catch (e: Exception) {
        Either.Left(Failure.LoadSingleSkycamFirestoreFailure)
    }


    @ExperimentalCoroutinesApi
    override fun getSkycamFlow(skycamKey: String) = callbackFlow {
        val listenerRegistration = firestore.collection("skycams").document(skycamKey)
            .addSnapshotListener { snapshot, error ->

                if (error != null) throw error
                if (snapshot != null) offer(mapper.singleFromLeftToRight(snapshot))
            }
        awaitClose {
            listenerRegistration.remove()
            Timber.i("Listener removed")
        }
    }
}