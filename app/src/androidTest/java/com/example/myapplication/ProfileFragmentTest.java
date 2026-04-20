package com.example.myapplication;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import com.example.myapplication.fragments.ProfileFragment;

@RunWith(AndroidJUnit4.class)
public class ProfileFragmentTest {

    @Test
    public void logoutButton_isDisplayed() {
        FragmentScenario.launchInContainer(ProfileFragment.class);

        onView(withId(R.id.logoutButton))
                .check(matches(isDisplayed()));
    }

    @Test
    public void darkModeSwitch_isDisplayed() {
        FragmentScenario.launchInContainer(ProfileFragment.class);

        onView(withId(R.id.switchTheme))
                .check(matches(isDisplayed()));
    }

    @Test
    public void darkModeSwitch_canBeToggled() {
        FragmentScenario.launchInContainer(ProfileFragment.class);

        onView(withId(R.id.switchTheme))
                .perform(click());
    }
}