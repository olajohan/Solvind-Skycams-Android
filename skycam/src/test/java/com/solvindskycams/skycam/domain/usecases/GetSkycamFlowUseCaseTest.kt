package com.solvindskycams.skycam.domain.usecases

import com.solvindskycams.skycam.getSkycam

class GetSkycamFlowUseCaseTest {

    @ObsoleteCoroutinesApi
    private val mainThreadSurrogate = newSingleThreadContext("UI Thread")
    val repo: ISkycamRepo = mockk()
    val useCase = GetSkycamFlowUseCase(repo)

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

    /**
    * When a user opens the application, retrieve all the skycams.
    * 1. Request skycamlist from repository
    *
    * */
    @ExperimentalCoroutinesApi
    @Test
    fun `Verify that the use case is calling the repository's getSkycamFlow(skycamKey) method exactly once`() = runBlockingTest {
        val TEST_SKYCAM = getSkycam()
        val TEST_SKYCAMKEY = TEST_SKYCAM.skycamKey
        coEvery { repo.getSkycamFlow(TEST_SKYCAMKEY) } returns flow {}
        useCase(GetSkycamFlowUseCase.Params(TEST_SKYCAMKEY))
        coVerify(exactly = 1) { repo.getSkycamFlow(TEST_SKYCAMKEY) }
    }
}