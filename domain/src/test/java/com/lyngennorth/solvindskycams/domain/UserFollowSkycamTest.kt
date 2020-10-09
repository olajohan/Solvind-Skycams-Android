package com.lyngennorth.solvindskycams.domain

import com.lyngennorth.solvindskycams.domain.repositories.IUserRepository
import com.lyngennorth.solvindskycams.domain.usecases.UserFollowSkycam
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


class UserFollowSkycamTest {

    @ObsoleteCoroutinesApi
    private val mainThreadSurrogate = newSingleThreadContext("UI Thread")
    val repo: IUserRepository = mockk()
    val useCase = UserFollowSkycam(repo)

    @ExperimentalCoroutinesApi
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
    fun `Verify that the use case is calling the repository's followSkycam() method exactly once`() = runBlockingTest {
        val TEST_SKYCAM = getSkycam()
        coEvery { repo.followSkycam(TEST_SKYCAM.skycamKey) } returns Either.Right(Unit)

        useCase(this, UserFollowSkycam.Params(TEST_SKYCAM.skycamKey)) {}

        coVerify(exactly = 1) { repo.followSkycam(TEST_SKYCAM.skycamKey) }
    }
}