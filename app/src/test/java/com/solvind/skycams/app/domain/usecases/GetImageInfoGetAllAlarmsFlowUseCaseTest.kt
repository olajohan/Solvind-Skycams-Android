package com.solvind.skycams.app.domain.usecases

import com.google.common.truth.Truth.assertThat
import com.solvind.skycams.app.MainCoroutineRule
import com.solvind.skycams.app.core.Failure
import com.solvind.skycams.app.core.Resource
import com.solvind.skycams.app.domain.repo.IImageInfoRepo
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class GetImageInfoGetAllAlarmsFlowUseCaseTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var repo: IImageInfoRepo

    private lateinit var usecase: GetImageInfoUseCase

    @After
    fun cleanup() {
        mainCoroutineRule.dispatcher.cleanupTestCoroutines()
    }

    @Before
    fun setupUsecase() {
        repo = mockk()
        usecase = GetImageInfoUseCase(repo, mainCoroutineRule.dispatcher, mainCoroutineRule.dispatcher)
    }

    @Test
    fun getImageInfo_emptyImageId_returnsResourceError() = mainCoroutineRule.dispatcher.runBlockingTest {
        val TEST_IMAGE_ID = ""
        val TEST_PARAMS = GetImageInfoUseCase.Params(TEST_IMAGE_ID)

        val result = usecase.run(TEST_PARAMS)
        assertThat(result is Resource.Error).isTrue()
        assertThat((result as Resource.Error).failure).isEqualTo(Failure.EmptyImageIdFailure)
    }
}