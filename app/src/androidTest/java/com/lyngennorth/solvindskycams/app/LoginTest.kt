package com.lyngennorth.solvindskycams.app

import android.view.View
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit


/*
* 1. Start at login /onboarding screen
* 2. Display adConsent form
* 3. Click "Consent"
* 4. Click continue/login anonymously
* 5. Auto Navigate to homescreen
* 6. Scroll recyclerview to position of Lyngen North
* 8. Click "Watch ad button"
* 9. Wait for ad to finish playing
* 10. Click X on the fullscreen ad
* 11. Click activate alarm
* */
@RunWith(AndroidJUnit4::class)
@LargeTest
class LoginTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun logInAnonymously_watchAd_clickActivateAlarmButton() {

        // CLick login anonymously button
        onView(withId(R.id.login_anonymously_button))
            .perform(click())
            .check(matches(isDisplayed()))


        // Scroll to the item in list to make sure it is loaded in the viewholder
        onView(withId(R.id.skycam_recyclerView)).perform(
            RecyclerViewActions.scrollTo<SkycamUiModel>(
                hasDescendant(withText("Lyngen North"))
            )
        )

        // Wait for max 10 seconds before making ad button available
        Thread.sleep(TimeUnit.SECONDS.toMillis(10L))
        onView(withId(R.id.skycam_recyclerview)).perform(
            RecyclerViewActions.actionOnItemAtPosition<SkycamUiModel>(
                0, AdButtonClickAction()
            )
        )

        // Wait for ad to finish showing
        Thread.sleep(TimeUnit.SECONDS.toMillis(30L))

        // Locate and click close button on fullscreen ad by adMob
        val imageButton = onView(allOf(withContentDescription("Interstitial close button"), isDisplayed()))
        imageButton.perform(click())

        // Click "Activate" button on recyclerview item
        onView(withId(R.id.skycam_recyclerview)).perform(
            RecyclerViewActions.actionOnItemAtPosition<SkycamUiModel>(
                0, ActivateAlarmButtonClick()
            )
        )
    }

    private class ActivateAlarmButtonClick: ViewAction {
        override fun getConstraints(): Matcher<View> =allOf(isAssignableFrom(Button::class.java))
        override fun getDescription(): String = "Activates the aurora alarm"
        override fun perform(uiController: UiController?, view: View?) {
            view?.findViewById<Button>(R.id.activate_alarm_button)?.performClick()
        }

    }

    private class AdButtonClickAction: ViewAction {
        override fun getConstraints(): Matcher<View> =allOf(isAssignableFrom(Button::class.java))
        override fun getDescription(): String = "Clicks the ad button to start displaying ads in exchange for alarm time"
        override fun perform(uiController: UiController?, view: View?) {
            view?.findViewById<Button>(R.id.watch_ad_button)?.performClick()
        }
    }
}