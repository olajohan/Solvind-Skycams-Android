package com.solvind.skycams.app.service

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.ServiceTestRule
import com.nhaarman.mockitokotlin2.verify
import com.solvind.skycams.app.core.UseCaseFlow
import com.solvind.skycams.app.domain.model.AlarmConfig
import com.solvind.skycams.app.domain.usecases.GetAllAlarmsFlowUseCase
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`

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

    @Test
    fun dfs() {

        `when`(getAllAlarmsFlowUseCase.run(UseCaseFlow.None())).thenReturn(
            flow {
                emit(emptyList<AlarmConfig>())
            }
        )
        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            AlarmServiceImpl::class.java
        )

        val binder = serviceRule.bindService(intent)
        serviceRule.unbindService()

        verify(getAllAlarmsFlowUseCase).run(UseCaseFlow.None())

    }
}