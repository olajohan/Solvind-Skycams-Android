package com.solvindskycams.skycam.domain.usecases

import com.solvindskycams.common.Either
import com.solvindskycams.skycam.domain.repo.ISkycamImageDetailsRepo
import com.solvindskycams.skycam.getSkycamImageUserVote
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

class VoteSkycamImageUseCaseTest {

    @ObsoleteCoroutinesApi
    private val mainThreadSurrogate = newSingleThreadContext("UI Thread")
    val detailsRepo: ISkycamImageDetailsRepo = mockk()
    val useCase = VoteSkycamImageUseCase(detailsRepo)

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
    fun `Verify that the use case is calling the repository's voteSkycamImage(imageId) method exactly once`() = runBlockingTest {
        val TEST_SKYCAM_IMAGE_USER_VOTE = getSkycamImageUserVote()

        coEvery { detailsRepo.voteSkycamImage(TEST_SKYCAM_IMAGE_USER_VOTE) } returns Either.Right(Unit)

        useCase(this, VoteSkycamImageUseCase.Params(TEST_SKYCAM_IMAGE_USER_VOTE)) {}

        coVerify(exactly = 1) { detailsRepo.voteSkycamImage(TEST_SKYCAM_IMAGE_USER_VOTE) }
    }
}