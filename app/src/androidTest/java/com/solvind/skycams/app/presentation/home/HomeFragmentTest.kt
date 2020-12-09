package com.solvind.skycams.app.presentation.home

import android.view.View
import androidx.navigation.NavController
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import com.google.firebase.auth.FirebaseAuth
import com.solvind.skycams.app.core.EspressoIdlingResource
import com.solvind.skycams.app.data.repo.FirestoreAlarmConfigRepoImpl
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.Matcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
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

