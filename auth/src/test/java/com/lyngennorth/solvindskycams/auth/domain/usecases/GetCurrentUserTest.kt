package com.lyngennorth.solvindskycams.auth.domain.usecases

import com.lyngennorth.solvindskycams.auth.domain.repo.IAuthenticatorRepo
import com.lyngennorth.solvindskycams.common.Either
import com.lyngennorth.solvindskycams.common.usecases.UseCase
import io.mockk.*
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


class GetCurrentUserTest {

    private val mainThreadSurrogate = newSingleThreadContext("UI Thread")
    private val repo: IAuthenticatorRepo = mockk()
    private val usecase = GetCurrentUser(repo)

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

    @Test
    fun `Call getCurrentUser exactly once with success`() = runBlockingTest {
        val TEST_USER = getTestUser()
        coEvery { repo.getCurrentUser() } returns Either.Right(TEST_USER)
        usecase(this, UseCase.None()) {}
        coVerify(exactly = 1) { repo.getCurrentUser() }
    }

}