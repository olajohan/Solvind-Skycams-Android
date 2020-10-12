package com.lyngennorth.solvindskycams.androiddata

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.lyngennorth.solvindskycams.skycam.data.repositories.FirestoreSkycamRepositoryImpl
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FirestoreSkycamRepositoryImplTest {

    private var firestore = Firebase.firestore.apply {
        useEmulator("127.0.0.1", 8080)
    }

    val firestoreSkycamRepositoryImpl =
        com.lyngennorth.solvindskycams.skycam.data.repositories.FirestoreSkycamRepositoryImpl(
            firestore
        )

    @Before
    fun setUp() {

    }

    @Test
    fun GetAllSkycamsFromFirestoreSuccess() = runBlocking {

    }
}