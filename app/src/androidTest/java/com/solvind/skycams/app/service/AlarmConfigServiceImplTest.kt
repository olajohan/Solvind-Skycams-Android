package com.solvind.skycams.app.service

import androidx.test.rule.ServiceTestRule
import com.solvind.skycams.app.domain.usecases.GetAllAlarmsFlowUseCase
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.mockito.Mock

@ExperimentalCoroutinesApi
@HiltAndroidTest
class AlarmConfigServiceImplTest {

    @get:Rule(order= 0 )
    val hiltAndroidRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val serviceRule = ServiceTestRule()

    @BindValue @Mock
    lateinit var getAllAlarmsFlowUseCase: GetAllAlarmsFlowUseCase

    @Before
    fun setup() {
        hiltAndroidRule.inject()
    }
}