package com.solvind.skycams.app.presentation.home

import androidx.test.core.app.launchActivity
import com.solvind.skycams.app.InitActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class MainActivityTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun loadingSkycamList_showLoadingIndicator() {
        val scenario = launchActivity<InitActivity>()

    }
}