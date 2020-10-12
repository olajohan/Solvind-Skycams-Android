package com.lyngennorth.solvindskycams.skycam.domain.usecases


import com.lyngennorth.solvindskycams.common.Either
import com.lyngennorth.solvindskycams.skycam.domain.model.Skycam
import com.lyngennorth.solvindskycams.skycam.domain.repo.ISkycamRepo
import com.lyngennorth.solvindskycams.skycam.getSkycam
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test


class GetSkycamFlowTest {

    @ObsoleteCoroutinesApi
    private val mainThreadSurrogate = newSingleThreadContext("UI Thread")
    val repo: ISkycamRepo = mockk()
    val useCase = GetSkycamFlow(repo)

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
    fun `Verify that the use case is calling the repository's getSkycamFlow(skycamKey) method exactly once`() = runBlockingTest {
        val TEST_SKYCAM = getSkycam()
        val TEST_SKYCAMKEY = TEST_SKYCAM.skycamKey
        coEvery { repo.getSkycamFlow(TEST_SKYCAMKEY) } returns Either.Right(flow<Skycam> { })

        useCase(this, GetSkycamFlow.Params(TEST_SKYCAMKEY)) {}

        coVerify(exactly = 1) { repo.getSkycamFlow(TEST_SKYCAMKEY) }
    }
}