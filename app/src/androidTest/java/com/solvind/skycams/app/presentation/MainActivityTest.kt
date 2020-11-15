package com.solvind.skycams.app.presentation

import androidx.test.espresso.IdlingRegistry
import com.google.firebase.auth.FirebaseAuth
import com.solvind.skycams.app.core.EspressoIdlingResource
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule

@ExperimentalCoroutinesApi
@HiltAndroidTest
class MainActivityTest {


    @get:Rule(order = 0)
    val hiltAndroidRule = HiltAndroidRule(this)

    @Before
    fun registerIdlingResource() {
        hiltAndroidRule.inject()
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        FirebaseAuth.getInstance().signOut()
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }
}