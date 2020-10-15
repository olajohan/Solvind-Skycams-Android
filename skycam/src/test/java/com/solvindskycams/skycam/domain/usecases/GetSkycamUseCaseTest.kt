package com.solvindskycams.skycam.domain.usecases

import com.solvindskycams.skycam.getSkycam

class GetSkycamUseCaseTest {

    @ObsoleteCoroutinesApi
    private val mainThreadSurrogate = newSingleThreadContext("UI Thread")
    val repo: ISkycamRepo = mockk()
    val useCase = GetSkycamUseCase(repo)

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
    fun `Verify that the use case is calling the repository's getSkycam() method exactly once`() = runBlockingTest {
        val TEST_SKYCAM = getSkycam()
        val TEST_SKYCAMKEY = TEST_SKYCAM.skycamKey
        coEvery { repo.getSkycam(TEST_SKYCAMKEY) } returns Either.Right(TEST_SKYCAM)

        useCase(this, GetSkycamUseCase.Params(TEST_SKYCAMKEY)) {}

        coVerify(exactly = 1) { repo.getSkycam(TEST_SKYCAMKEY) }
    }
}