package com.example.myapplication;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.myapplication.activity.SignupActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.CoreMatchers.not;

@RunWith(AndroidJUnit4.class)
public class SignupActivityTest {


    @Test
    public void signupScreen_allFieldsVisible() {
        ActivityScenario.launch(SignupActivity.class);

        onView(withId(R.id.nameEditText)).check(matches(isDisplayed()));
        onView(withId(R.id.emailEditText)).check(matches(isDisplayed()));
        onView(withId(R.id.passwordEditText)).check(matches(isDisplayed()));
        onView(withId(R.id.confirmPasswordEditText)).check(matches(isDisplayed()));
        onView(withId(R.id.signupButton)).check(matches(isDisplayed()));
    }

    @Test
    public void signupScreen_showsGetStartedText() {
        ActivityScenario.launch(SignupActivity.class);
        onView(withText("Get Started")).check(matches(isDisplayed()));
    }

    // --- Validation Error Tests ---

    @Test
    public void clickSignup_emptyName_showsNameError() {
        ActivityScenario.launch(SignupActivity.class);

        onView(withId(R.id.signupButton)).perform(click());

        onView(withId(R.id.nameLayout))
                .check(matches(hasDescendant(withText("Full Name is required"))));
    }

    @Test
    public void clickSignup_emptyEmail_showsEmailError() {
        ActivityScenario.launch(SignupActivity.class);

        onView(withId(R.id.nameEditText)).perform(typeText("John Doe"), closeSoftKeyboard());
        onView(withId(R.id.signupButton)).perform(click());

        onView(withId(R.id.emailLayout))
                .check(matches(hasDescendant(withText("Email is required"))));
    }

    @Test
    public void clickSignup_invalidEmail_showsFormatError() {
        ActivityScenario.launch(SignupActivity.class);

        onView(withId(R.id.nameEditText)).perform(typeText("John Doe"), closeSoftKeyboard());
        onView(withId(R.id.emailEditText)).perform(typeText("bademail"), closeSoftKeyboard());
        onView(withId(R.id.signupButton)).perform(click());

        onView(withId(R.id.emailLayout))
                .check(matches(hasDescendant(withText("Enter a valid email"))));
    }

    @Test
    public void clickSignup_shortPassword_showsLengthError() {
        ActivityScenario.launch(SignupActivity.class);

        onView(withId(R.id.nameEditText)).perform(typeText("John Doe"), closeSoftKeyboard());
        onView(withId(R.id.emailEditText)).perform(typeText("john@gmail.com"), closeSoftKeyboard());
        onView(withId(R.id.passwordEditText)).perform(typeText("abc"), closeSoftKeyboard());
        onView(withId(R.id.signupButton)).perform(click());

        onView(withId(R.id.passwordLayout))
                .check(matches(hasDescendant(withText("Password must be at least 6 characters"))));
    }

    @Test
    public void clickSignup_passwordMismatch_showsMismatchError() {
        ActivityScenario.launch(SignupActivity.class);

        onView(withId(R.id.nameEditText)).perform(typeText("John Doe"), closeSoftKeyboard());
        onView(withId(R.id.emailEditText)).perform(typeText("john@gmail.com"), closeSoftKeyboard());
        onView(withId(R.id.passwordEditText)).perform(typeText("password123"), closeSoftKeyboard());
        onView(withId(R.id.confirmPasswordEditText)).perform(typeText("different123"), closeSoftKeyboard());
        onView(withId(R.id.signupButton)).perform(click());

        onView(withId(R.id.confirmPasswordLayout))
                .check(matches(hasDescendant(withText("Passwords do not match"))));
    }

    @Test
    public void clickSignup_validInputs_buttonDisabledWhileLoading() {
        ActivityScenario.launch(SignupActivity.class);

        onView(withId(R.id.nameEditText)).perform(typeText("John Doe"), closeSoftKeyboard());
        onView(withId(R.id.emailEditText)).perform(typeText("john@gmail.com"), closeSoftKeyboard());
        onView(withId(R.id.passwordEditText)).perform(typeText("password123"), closeSoftKeyboard());
        onView(withId(R.id.confirmPasswordEditText)).perform(typeText("password123"), closeSoftKeyboard());
        onView(withId(R.id.signupButton)).perform(click());

        // Button should be disabled immediately (loading state)
        onView(withId(R.id.signupButton)).check(matches(not(isEnabled())));
    }

    // --- Navigation Tests ---

    @Test
    public void clickSignIn_navigatesBackToLogin() {
        ActivityScenario.launch(SignupActivity.class);

        onView(withId(R.id.loginText)).perform(click());

        // Login screen's unique element should now be visible
        onView(withId(R.id.rememberMe)).check(matches(isDisplayed()));
    }
}