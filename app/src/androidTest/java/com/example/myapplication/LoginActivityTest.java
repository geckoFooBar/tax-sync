package com.example.myapplication;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.myapplication.activity.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.core.IsNot.not;

@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {

    @Before
    public void signOut() {
        // Always start logged out so LoginActivity doesn't skip to MainActivity
        FirebaseAuth.getInstance().signOut();
    }

    // --- UI Visibility Tests ---

    @Test
    public void loginScreen_allFieldsVisible() {
        ActivityScenario.launch(LoginActivity.class);

        onView(withId(R.id.emailEditText)).check(matches(isDisplayed()));
        onView(withId(R.id.passwordEditText)).check(matches(isDisplayed()));
        onView(withId(R.id.loginButton)).check(matches(isDisplayed()));
        onView(withId(R.id.rememberMe)).check(matches(isDisplayed()));
        onView(withId(R.id.signupText)).check(matches(isDisplayed()));
    }

    @Test
    public void loginScreen_showsWelcomeBackText() {
        ActivityScenario.launch(LoginActivity.class);
        onView(withText("Welcome Back")).check(matches(isDisplayed()));
    }

    // --- Validation Error Tests ---

    @Test
    public void clickLogin_emptyEmail_showsEmailError() {
        ActivityScenario.launch(LoginActivity.class);

        onView(withId(R.id.loginButton)).perform(click());

        onView(withId(R.id.emailLayout))
                .check(matches(hasDescendant(withText("Email is required"))));
    }

    @Test
    public void clickLogin_invalidEmail_showsFormatError() {
        ActivityScenario.launch(LoginActivity.class);

        onView(withId(R.id.emailEditText)).perform(typeText("notanemail"), closeSoftKeyboard());
        onView(withId(R.id.loginButton)).perform(click());

        onView(withId(R.id.emailLayout))
                .check(matches(hasDescendant(withText("Enter a valid email"))));
    }

    @Test
    public void clickLogin_validEmail_emptyPassword_showsPasswordError() {
        ActivityScenario.launch(LoginActivity.class);

        onView(withId(R.id.emailEditText)).perform(typeText("user@gmail.com"), closeSoftKeyboard());
        onView(withId(R.id.loginButton)).perform(click());

        onView(withId(R.id.passwordLayout))
                .check(matches(hasDescendant(withText("Password is required"))));
    }

    @Test
    public void clickLogin_validInputs_buttonDisabledWhileLoading() {
        ActivityScenario.launch(LoginActivity.class);

        onView(withId(R.id.emailEditText)).perform(typeText("user@gmail.com"), closeSoftKeyboard());
        onView(withId(R.id.passwordEditText)).perform(typeText("password123"), closeSoftKeyboard());
        onView(withId(R.id.loginButton)).perform(click());

        // Button should be disabled immediately after click (loading state)
        onView(withId(R.id.loginButton)).check(matches(not(isEnabled())));
    }

    // --- Navigation Tests ---

    @Test
    public void clickCreateAccount_navigatesToSignup() {
        ActivityScenario.launch(LoginActivity.class);

        onView(withId(R.id.signupText)).perform(click());

        // Signup screen's unique element should now be visible
        onView(withId(R.id.nameEditText)).check(matches(isDisplayed()));
    }

    // --- Remember Me Tests ---

    @Test
    public void rememberMe_defaultState_isUnchecked() {
        ActivityScenario.launch(LoginActivity.class);
        onView(withId(R.id.rememberMe)).check(matches(isNotChecked()));
    }

    @Test
    public void rememberMe_click_becomesChecked() {
        ActivityScenario.launch(LoginActivity.class);
        onView(withId(R.id.rememberMe)).perform(click());
        onView(withId(R.id.rememberMe)).check(matches(isChecked()));
    }
}