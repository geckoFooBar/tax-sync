package com.example.myapplication;

import android.app.Application;
import android.widget.CheckBox;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import com.example.myapplication.activity.LoginActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(
        application = Application.class,
        sdk = 34
)
public class LoginActivityRobolectricTest {

    @Before
    public void setUp() {
        // Initialize Firebase manually before each test
        if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
            FirebaseApp.initializeApp(
                    ApplicationProvider.getApplicationContext(),
                    new FirebaseOptions.Builder()
                            .setApplicationId("1:000000000000:android:0000000000000000") // fake value
                            .setApiKey("fake-api-key")                                    // fake value
                            .setProjectId("fake-project-id")                              // fake value
                            .build()
            );
        }
    }

    @Test
    public void loginScreen_allFieldsPresent() {
        try (ActivityScenario<LoginActivity> scenario =
                     ActivityScenario.launch(LoginActivity.class)) {
            scenario.onActivity(activity -> {
                assertNotNull(activity.findViewById(R.id.emailEditText));
                assertNotNull(activity.findViewById(R.id.passwordEditText));
                assertNotNull(activity.findViewById(R.id.loginButton));
                assertNotNull(activity.findViewById(R.id.rememberMe));
                assertNotNull(activity.findViewById(R.id.signupText));
            });
        }
    }

    @Test
    public void clickLogin_emptyEmail_setsEmailError() {
        try (ActivityScenario<LoginActivity> scenario =
                     ActivityScenario.launch(LoginActivity.class)) {
            scenario.onActivity(activity -> {
                MaterialButton loginButton = activity.findViewById(R.id.loginButton);
                loginButton.performClick();

                TextInputLayout emailLayout = activity.findViewById(R.id.emailLayout);
                assertEquals("Email is required", emailLayout.getError().toString());
            });
        }
    }

    @Test
    public void clickLogin_invalidEmail_setsFormatError() {
        try (ActivityScenario<LoginActivity> scenario =
                     ActivityScenario.launch(LoginActivity.class)) {
            scenario.onActivity(activity -> {
                TextInputEditText emailEditText = activity.findViewById(R.id.emailEditText);
                emailEditText.setText("notanemail");

                activity.findViewById(R.id.loginButton).performClick();

                TextInputLayout emailLayout = activity.findViewById(R.id.emailLayout);
                assertEquals("Enter a valid email", emailLayout.getError().toString());
            });
        }
    }

    @Test
    public void clickLogin_validEmail_emptyPassword_setsPasswordError() {
        try (ActivityScenario<LoginActivity> scenario =
                     ActivityScenario.launch(LoginActivity.class)) {
            scenario.onActivity(activity -> {
                TextInputEditText emailEditText = activity.findViewById(R.id.emailEditText);
                emailEditText.setText("user@gmail.com");

                activity.findViewById(R.id.loginButton).performClick();

                TextInputLayout passwordLayout = activity.findViewById(R.id.passwordLayout);
                assertEquals("Password is required", passwordLayout.getError().toString());
            });
        }
    }

    @Test
    public void rememberMe_defaultState_isUnchecked() {
        try (ActivityScenario<LoginActivity> scenario =
                     ActivityScenario.launch(LoginActivity.class)) {
            scenario.onActivity(activity -> {
                CheckBox rememberMe = activity.findViewById(R.id.rememberMe);
                assertFalse(rememberMe.isChecked());
            });
        }
    }

    @Test
    public void rememberMe_afterClick_isChecked() {
        try (ActivityScenario<LoginActivity> scenario =
                     ActivityScenario.launch(LoginActivity.class)) {
            scenario.onActivity(activity -> {
                CheckBox rememberMe = activity.findViewById(R.id.rememberMe);
                rememberMe.performClick();
                assertTrue(rememberMe.isChecked());
            });
        }
    }
}