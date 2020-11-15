package com.solvind.skycams.app.domain.usecases

import com.google.common.truth.Truth.assertThat
import com.solvind.skycams.app.MainCoroutineRule
import com.solvind.skycams.app.core.Resource
import com.solvind.skycams.app.domain.repo.IImageInfoRepo
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class GetImageInfoUseCaseTest {

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    lateinit var imageInfoRepo: IImageInfoRepo
    lateinit var getImageInfoUseCase: GetImageInfoUseCase
    @Before
    fun setup() {
        imageInfoRepo = mockk()
        getImageInfoUseCase = GetImageInfoUseCase(imageInfoRepo, mainCoroutineRule.dispatcher, mainCoroutineRule.dispatcher)

    }

    @Test
    fun `Empty imageId returns error`() = mainCoroutineRule.dispatcher.runBlockingTest {
        val TEST_IMAGEID = ""
        val result = getImageInfoUseCase.run(GetImageInfoUseCase.Params(TEST_IMAGEID))
        assertThat(result is Resource.Error)
    }


}