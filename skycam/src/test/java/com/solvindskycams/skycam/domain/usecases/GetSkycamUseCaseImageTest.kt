package com.solvindskycams.skycam.domain.usecases

import com.solvindskycams.skycam.getSkycamImage

class GetSkycamUseCaseImageTest {

    @ObsoleteCoroutinesApi
    private val mainThreadSurrogate = newSingleThreadContext("UI Thread")
    val detailsRepo: ISkycamImageDetailsRepo = mockk()
    val useCase = GetSkycamImageUseCase(detailsRepo)

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
    fun `Verify that the use case is calling the repository's getSkycamImage(imageId) method exactly once`() = runBlockingTest {
        val TEST_SKYCAM_IMAGE = getSkycamImage()
        val TEST_SKYCAM_IMAGE_ID = TEST_SKYCAM_IMAGE.imageId
        coEvery { detailsRepo.getSkycamImage(TEST_SKYCAM_IMAGE_ID) } returns Either.Right(TEST_SKYCAM_IMAGE)

        useCase(this, GetSkycamImageUseCase.Params(TEST_SKYCAM_IMAGE_ID)) {}

        coVerify(exactly = 1) { detailsRepo.getSkycamImage(TEST_SKYCAM_IMAGE_ID) }
    }
}