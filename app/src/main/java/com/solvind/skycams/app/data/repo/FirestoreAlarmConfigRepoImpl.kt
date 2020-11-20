package com.solvind.skycams.app.data.repo

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.solvind.skycams.app.core.Failure
import com.solvind.skycams.app.core.Resource
import com.solvind.skycams.app.core.UserIdIsNullException
import com.solvind.skycams.app.data.repo.mappers.SnapshotToAlarmMapper
import com.solvind.skycams.app.domain.model.AlarmConfig
import com.solvind.skycams.app.domain.repo.IAlarmConfigRepo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


private const val USER_DATA_COLLECTION = "userData"
private const val ALARMS_COLLECTION = "alarms"

@ExperimentalCoroutinesApi
class FirestoreAlarmConfigRepoImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val mapper: SnapshotToAlarmMapper
) : IAlarmConfigRepo {

    override suspend fun getAlarmConfig(skycamKey: String): Resource<AlarmConfig> {
        val uid = firebaseAuth.uid ?: return Resource.Error(Failure.UserIdNullFailure)
        val skycamAlarmDocumentSnapshot =
            firestore.collection(USER_DATA_COLLECTION).document(uid).collection(
                ALARMS_COLLECTION
            ).document(skycamKey).get().await()
        if (!skycamAlarmDocumentSnapshot.exists()) return Resource.Error(Failure.AlarmNotFoundFailure)
        return Resource.Success(mapper.singleFromLeftToRight(skycamAlarmDocumentSnapshot))
    }

    /**
     * @throws UserIdIsNullException
     * */
    override fun getAlarmConfigFlow(skycamKey: String): Flow<AlarmConfig> = callbackFlow {

        val uid = firebaseAuth.uid ?: throw UserIdIsNullException
        val document = firestore.collection(USER_DATA_COLLECTION).document(uid)
            .collection(ALARMS_COLLECTION).document(skycamKey)

        val subscription = document.addSnapshotListener { snapshot, error ->
            if (error != null) throw error
            if (snapshot != null && snapshot.exists()) offer(
                mapper.singleFromLeftToRight(
                    snapshot
                )
            )
        }
        awaitClose { subscription.remove() }
    }

    /**
     * @throws UserIdIsNullException
     * */
    override fun getAllAlarmConfigFlows(): Flow<AlarmConfig> = callbackFlow {
        val uid = firebaseAuth.uid ?: throw UserIdIsNullException
        val collectionQuery = firestore.collection(USER_DATA_COLLECTION).document(uid)
            .collection(ALARMS_COLLECTION)

        val subscription = collectionQuery.addSnapshotListener { querySnapshot, error ->
            if (error != null) {
                throw error
            }
            if (querySnapshot != null && !querySnapshot.isEmpty)  {
                querySnapshot.documents.forEach {
                    offer(mapper.singleFromLeftToRight(it))
                }
            }
        }
        awaitClose { subscription.remove() }
    }

    override suspend fun getAllAlarmConfigs(): Resource<List<AlarmConfig>> {
        val uid = firebaseAuth.uid ?: return Resource.Error(Failure.UserIdNullFailure)
        val result = firestore.collection(USER_DATA_COLLECTION).document(uid)
            .collection(ALARMS_COLLECTION).get()

        val documents = result.await().documents
        if (!result.isSuccessful) return Resource.Error(Failure.FailedToGetAllAlarmsFailure)
        return Resource.Success(mapper.listFromLeftToRight(documents))
    }

    override suspend fun setAlarmConfig(
        skycamKey: String,
        alarmAvailableUntil: Long,
        isActive: Boolean
    ): Resource<kotlin.Unit> {
        val uid = firebaseAuth.uid ?: return Resource.Error(Failure.UserIdNullFailure)
        val result = firestore.collection(USER_DATA_COLLECTION).document(uid)
            .collection(ALARMS_COLLECTION).document(skycamKey).set(
                mapOf(
                    "alarmAvailableUntilEpochSeconds" to alarmAvailableUntil,
                    "isActive" to isActive
                )
            )
        result.await()
        if (!result.isSuccessful) return Resource.Error(Failure.FailedToSetAlarmFailure)
        return Resource.Success(Unit)
    }
}