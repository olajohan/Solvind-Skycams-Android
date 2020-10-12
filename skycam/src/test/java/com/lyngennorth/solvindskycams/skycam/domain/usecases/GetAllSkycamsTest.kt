package com.lyngennorth.solvindskycams.skycam.domain.usecases

import com.lyngennorth.solvindskycams.common.Either
import com.lyngennorth.solvindskycams.common.usecases.UseCase
import com.lyngennorth.solvindskycams.skycam.domain.repo.ISkycamRepo
import com.lyngennorth.solvindskycams.skycam.getSkycam
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test


class GetAllSkycamsTest {

    @ObsoleteCoroutinesApi
    private val mainThreadSurrogate = newSingleThreadContext("UI Thread")
    val repo: ISkycamRepo = mockk()
    val useCase = GetAllSkycams(repo)

    @ExperimentalCoroutinesApi
    @ObsoleteCoroutinesApi
    @Before
    fun build() {
        clearAllMocks()
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @ObsoleteCoroutinesApi
    @ExperimentalCoroutinesApi
    @After
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