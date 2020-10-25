package com.solvind.skycams.skycam.domain.usecases

import com.google.common.truth.Truth.assertThat
import com.solvind.skycams.skycam.Failure
import com.solvind.skycams.skycam.Result
import com.solvind.skycams.skycam.domain.repo.IImageInfoRepo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class GetImageInfoUseCaseTest {

    @Mock
    private lateinit var repo: IImageInfoRepo

    private lateinit var usecase: GetImageInfoUseCase

    @Before
    fun setupUsecase() {
        usecase = GetImageInfoUseCase(repo)
    }

    @Test
    fun getImageInfo_emptyImageId_returnsResourceError() = runBlockingTest{
        val TEST_IMAGE_ID = ""
        val TEST_PARAMS = GetImageInfoUseCase.Params(TEST_IMAGE_ID)

        val result = usecase.run(TEST_PARAMS)
        assertThat(result is Result.Error).isTrue()
        assertThat((result as Result.Error).failure).isEqualTo(Failure.EmptyImageIdFailure)
    }
}