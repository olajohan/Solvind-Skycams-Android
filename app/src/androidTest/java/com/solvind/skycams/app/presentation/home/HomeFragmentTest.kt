package com.solvind.skycams.app.presentation.home

import android.view.View
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.web.sugar.Web
import androidx.test.espresso.web.webdriver.DriverAtoms
import androidx.test.espresso.web.webdriver.Locator
import com.google.firebase.auth.FirebaseAuth
import com.solvind.skycams.app.R
import com.solvind.skycams.app.core.EspressoIdlingResource
import com.solvind.skycams.app.data.repo.FirestoreAlarmConfigRepoImpl
import com.solvind.skycams.app.launchFragmentInHiltContainer
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.Matcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import javax.inject.Inject


@ExperimentalCoroutinesApi
@HiltAndroidTest
class HomeFragmentTest {

    @get:Rule(order = 0)
    val hiltAndroidRule = HiltAndroidRule(this)

    @Inject
    lateinit var fragmentFactory: HomeFragmentFactory

    @Inject
    lateinit var alarmRepo: FirestoreAlarmConfigRepoImpl

    private val mockNavController = mock(NavController::class.java)

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

    @Test
    fun click_watchAd_showAd_click_activateAlarm() {

        EspressoIdlingResource.increment()
        FirebaseAuth.getInstance().signInWithEmailAndPassword(
            "ola@ola.com",
            "ola123"
        ).addOnCompleteListener { EspressoIdlingResource.decrement() }

        Espresso.onIdle()

        launchFragmentInHiltContainer<HomeFragment>(fragmentFactory = fragmentFactory) {
            Navigation.setViewNavController(requireView(), mockNavController)
        }

        onView(withId(R.id.skycam_recyclerView)).perform(RecyclerViewActions.actionOnItemAtPosition<SkycamAdapter.SkycamViewHolder>(0,
            MyViewAction.clickChildViewWithId(R.id.watch_ad_button)
        ))

        Espresso.onIdle()

        Web.onWebView().withElement(DriverAtoms.findElement(Locator.ID, "close_button")
        ).perform(DriverAtoms.webClick())

        onView(withId(R.id.skycam_recyclerView)).perform(RecyclerViewActions.actionOnItemAtPosition<SkycamAdapter.SkycamViewHolder>(0,
            MyViewAction.clickChildViewWithId(R.id.activate_alarm_button)
        ))
    }
}

object MyViewAction {
    fun clickChildViewWithId(id: Int): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View>? {
                return null
            }

            override fun getDescription(): String {
                return "Click on a child view with specified id."
            }

            override fun perform(uiController: UiController?, view: View) {
                val v: View = view.findViewById(id)
                v.performClick()
            }
        }
    }
}

