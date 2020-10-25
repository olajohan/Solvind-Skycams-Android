package com.solvind.skycams.skycam.data.repo

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.solvind.skycams.skycam.Failure
import com.solvind.skycams.skycam.Result
import com.solvind.skycams.skycam.data.repo.mappers.SnapshotToAlarmMapper
import com.solvind.skycams.skycam.domain.model.Alarm
import com.solvind.skycams.skycam.domain.repo.IAlarmRepo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


private const val USER_DATA_COLLECTION = "userData"
private const val ALARMS_COLLECTION = "alarms"

@ExperimentalCoroutinesApi
class FirestoreAlarmRepoImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val mapper: SnapshotToAlarmMapper
) : IAlarmRepo {
    override suspend fun getAlarm(skycamKey: String): Result<Alarm> {
        val uid = firebaseAuth.uid ?: return Result.Error(Failure.UserIdNullFailure)
        val skycamAlarmDocumentSnapshot = firestore.collection(USER_DATA_COLLECTION).document(uid).collection(
            ALARMS_COLLECTION
        ).document(skycamKey).get().await()
        if (!skycamAlarmDocumentSnapshot.exists()) return Result.Error(Failure.AlarmNotFoundFailure)
        return Result.Success(mapper.singleFromLeftToRight(skycamAlarmDocumentSnapshot))
    }
    override fun getAlarmFlow(skycamKey: String): Flow<Alarm> = callbackFlow {
        val uid = firebaseAuth.uid
        if (uid != null) {
            val document = firestore.collection(USER_DATA_COLLECTION).document(uid).collection(ALARMS_COLLECTION).document(skycamKey)

            val subscription = document.addSnapshotListener { snapshot, error ->
                if (error != null) throw error
                if (snapshot != null && snapshot.exists()) offer(mapper.singleFromLeftToRight(snapshot))
            }
            awaitClose { subscription.remove() }
        }
    }
    override fun getAllAlarmsFlow(): Flow<List<Alarm>> = callbackFlow {
        val uid = firebaseAuth.uid
        if (uid != null) {
            val collectionQuery = firestore.collection(USER_DATA_COLLECTION).document(uid).collection(ALARMS_COLLECTION)

            val subscription = collectionQuery.addSnapshotListener { querySnapshot, error ->
                if (error != null) throw error
                if (querySnapshot != null && !querySnapshot.isEmpty) offer(mapper.listFromLeftToRight(querySnapshot.documents))
            }
            awaitClose { subscription.remove() }
        }
    }
    override suspend fun setAlarm(skycamKey: String, alarmAvailableUntil: Long, isActive: Boolean): Result<Unit> {
        val uid = firebaseAuth.uid ?: return Result.Error(Failure.UserIdNullFailure)
        val result = firestore.collection(USER_DATA_COLLECTION).document(uid).collection(ALARMS_COLLECTION).document(skycamKey).set(
            mapOf(
                "alarmAvailableUntil" to alarmAvailableUntil,
                "isActive" to isActive
            ))
        result.await()
        if (!result.isSuccessful) return Result.Error(Failure.UserIdNullFailure)
        return Result.Success(Unit)
    }
}