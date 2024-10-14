package com.sharc.ramdhd

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testNavigationToTimerPanel() {
        // Verify we're on the home screen
        onView(withId(R.id.my_image_view)).check(matches(isDisplayed()))

        // Click on the ImageView that navigates to the Timer panel
        onView(withId(R.id.my_image_view)).perform(click())

        // Check that the title of the screen is "Timer"
        onView(withText("Timer")).check(matches(isDisplayed()))
    }
}