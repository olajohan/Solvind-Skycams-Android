package com.solvindskycams.app.domain.usecases

import com.google.common.truth.Truth.assertThat
import com.solvind.skycams.app.core.Failure
import com.solvind.skycams.app.core.Resource
import com.solvind.skycams.app.domain.repo.IImageInfoRepo
import com.solvind.skycams.app.domain.usecases.GetImageInfoUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class GetImageInfoGetAllAlarmsFlowUseCaseTest {

    @Mock
    private lateinit var repo: IImageInfoRepo

    private lateinit var usecase: GetImageInfoUseCase

    private val testDispatcher = TestCoroutineDispatcher()

    @After
    fun cleanup() {
        testDispatcher.cleanupTestCoroutines()
    }

    @Before
    fun setupUsecase() {
        usecase = GetImageInfoUseCase(repo, testDispatcher)
    }

    @Test
    fun getImageInfo_emptyImageId_returnsResourceError() = runBlockingTest{
        val TEST_IMAGE_ID = ""
        val TEST_PARAMS = GetImageInfoUseCase.Params(TEST_IMAGE_ID)

        val result = usecase.run(TEST_PARAMS)
        assertThat(result is Resource.Error).isTrue()
        assertThat((result as Resource.Error).failure).isEqualTo(Failure.EmptyImageIdFailure)
    }
}