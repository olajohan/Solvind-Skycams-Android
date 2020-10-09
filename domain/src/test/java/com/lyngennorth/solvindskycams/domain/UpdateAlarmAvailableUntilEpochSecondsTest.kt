package com.lyngennorth.solvindskycams.domain

import com.lyngennorth.solvindskycams.domain.repositories.IUserRepository
import com.lyngennorth.solvindskycams.domain.usecases.GetUser
import com.lyngennorth.solvindskycams.domain.usecases.UpdateAlarmAvailableUntilEpochSeconds
import com.lyngennorth.solvindskycams.domain.usecases.UseCase
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.concurrent.TimeUnit


class UpdateAlarmAvailableUntilEpochSecondsTest {

    @ObsoleteCoroutinesApi
    private val mainThreadSurrogate = newSingleThreadContext("UI Thread")
    val repo: IUserRepository = mockk()
    val useCase = UpdateAlarmAvailableUntilEpochSeconds(repo)

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
    fun `Verify that the use case is calling the repository's updateAlarmAvailableUntilEpochSeconds(plusSeconds) method exactly once`() = runBlockingTest {
        val PLUS_SECONDS = Instant.now().epochSecond + TimeUnit.MINUTES.toSeconds(30L)

        coEvery { repo.updateAlarmAvailableUntilEpochSeconds(PLUS_SECONDS) } returns Either.Right(Unit)

        useCase(this, UpdateAlarmAvailableUntilEpochSeconds.Params(PLUS_SECONDS)) {}

        coVerify(exactly = 1) { repo.updateAlarmAvailableUntilEpochSeconds(PLUS_SECONDS) }
    }
}