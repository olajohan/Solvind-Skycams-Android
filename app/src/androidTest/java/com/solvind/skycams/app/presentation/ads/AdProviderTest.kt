package com.solvind.skycams.app.presentation.ads

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import com.solvind.skycams.app.core.EspressoIdlingResource
import com.solvind.skycams.app.launchFragmentInHiltContainer
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltAndroidTest
class AdProviderTest {

    @get:Rule
    val hiltAndroidRule = HiltAndroidRule(this)

    @Inject
    lateinit var adProvider: AdProvider



    @Before
    fun registerIdlingResource() {
        hiltAndroidRule.inject()
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }

    @Test
    fun showAd_displays_fullscreenAd() = runBlockingTest {
        launchFragmentInHiltContainer<Fragment> {
            lifecycleScope.launch {
                adProvider.showRewardedAd(this@launchFragmentInHiltContainer.requireActivity() as AppCompatActivity, "lyngennorth")
            }
        }

        onView(withContentDescription("close_button")).perform(ViewActions.click())

    }
}