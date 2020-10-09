package com.lyngennorth.solvindskycams.androiddata

import com.lyngennorth.solvindskycams.androiddata.repositories.FirestoreSkycamRepositoryImpl
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test

class FirestoreSkycamRepositoryImplTest {

    val firestoreImpl: FirestoreSkycamRepositoryImpl = mockk()

    @Test
    fun `Get all skycams from Firestore success`() = runBlockingTest {

    }
}