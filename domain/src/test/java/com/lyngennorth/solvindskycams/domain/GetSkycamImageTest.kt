package com.lyngennorth.solvindskycams.domain

import com.lyngennorth.solvindskycams.domain.repositories.ISkycamImageRepository
import com.lyngennorth.solvindskycams.domain.usecases.GetSkycamImage
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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


class GetSkycamImageTest {

    @ObsoleteCoroutinesApi
    private val mainThreadSurrogate = newSingleThreadContext("UI Thread")
    val repo: ISkycamImageRepository = mockk()
    val useCase = GetSkycamImage(repo)

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
    fun `Verify that the use case is calling the repository's getSkycamImage(imageId) method exactly once`() = runBlockingTest {
        val TEST_SKYCAM_IMAGE = getSkycamImage()
        val TEST_SKYCAM_IMAGE_ID = TEST_SKYCAM_IMAGE.imageId
        coEvery { repo.getSkycamImage(TEST_SKYCAM_IMAGE_ID) } returns Either.Right(TEST_SKYCAM_IMAGE)

        useCase(this, GetSkycamImage.Params(TEST_SKYCAM_IMAGE_ID)) {}

        coVerify(exactly = 1) { repo.getSkycamImage(TEST_SKYCAM_IMAGE_ID) }
    }
}