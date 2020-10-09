package com.lyngennorth.solvindskycams.domain

import com.lyngennorth.solvindskycams.domain.domainmodel.Skycam
import com.lyngennorth.solvindskycams.domain.repositories.ISkycamRepository
import com.lyngennorth.solvindskycams.domain.usecases.GetAllSkycams
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


class GetAllSkycamsTest {

    @ObsoleteCoroutinesApi
    private val mainThreadSurrogate = newSingleThreadContext("UI Thread")
    val repo: ISkycamRepository = mockk()
    val useCase = GetAllSkycams(repo)

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
    fun `Verify that the use case is calling the repository's getAllSkycams exactly once`() = runBlockingTest {
        coEvery { repo.getAllSkycams() } returns Either.Right(listOf(getSkycam(), getSkycam(skycamKey = "alta")))

        useCase(this, UseCase.None()) {}

        coVerify(exactly = 1) { repo.getAllSkycams() }
    }
}