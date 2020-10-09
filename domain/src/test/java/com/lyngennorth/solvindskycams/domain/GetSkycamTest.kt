package com.lyngennorth.solvindskycams.domain

import com.lyngennorth.solvindskycams.domain.domainmodel.Skycam
import com.lyngennorth.solvindskycams.domain.repositories.ISkycamRepository
import com.lyngennorth.solvindskycams.domain.usecases.GetSkycam
import com.lyngennorth.solvindskycams.domain.usecases.UseCase
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


class GetSkycamTest {

    @ObsoleteCoroutinesApi
    private val mainThreadSurrogate = newSingleThreadContext("UI Thread")
    val repo: ISkycamRepository = mockk()
    val useCase = GetSkycam(repo)

    @ObsoleteCoroutinesApi
    @BeforeEach
    fun build() {
        clearAllMocks()
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @ObsoleteCoroutinesApi
    @ExperimentalCoroutinesApi
    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        mainThreadSurrogate.close()
    }

    /*
    * When a user opens the application, retrieve all the skycams.
    * 1. Request skycamlist from repository
    *
    * */
    @ExperimentalCoroutinesApi
    @Test
    fun `Verify that the use case is calling the repository's getSkycam() method exactly once`() = runBlockingTest {
        val TEST_SKYCAM = getSkycam()
        val TEST_SKYCAMKEY = TEST_SKYCAM.skycamKey
        coEvery { repo.getSkycam(TEST_SKYCAMKEY) } returns Either.Right(TEST_SKYCAM)

        useCase(this, GetSkycam.Params(TEST_SKYCAMKEY)) {}

        coVerify(exactly = 1) { repo.getSkycam(TEST_SKYCAMKEY) }
    }
}