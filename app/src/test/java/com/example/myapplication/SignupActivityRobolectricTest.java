package com.example.myapplication;

import android.app.Application;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import com.example.myapplication.activity.SignupActivity;
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
public class SignupActivityRobolectricTest {

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
    public void signupScreen_allFieldsPresent() {
        try (ActivityScenario<SignupActivity> scenario =
                     ActivityScenario.launchActivityForResult(SignupActivity.class)) {
            scenario.onActivity(activity -> {
                assertNotNull(activity.findViewById(R.id.nameEditText));
                assertNotNull(activity.findViewById(R.id.emailEditText));
                assertNotNull(activity.findViewById(R.id.passwordEditText));
                assertNotNull(activity.findViewById(R.id.confirmPasswordEditText));
                assertNotNull(activity.findViewById(R.id.signupButton));
            });
        }
    }

    @Test
    public void clickSignup_emptyName_setsNameError() {
        try (ActivityScenario<SignupActivity> scenario =
                     ActivityScenario.launch(SignupActivity.class)) {
            scenario.onActivity(activity -> {
                activity.findViewById(R.id.signupButton).performClick();

                TextInputLayout nameLayout = activity.findViewById(R.id.nameLayout);
                assertEquals("Full Name is required", nameLayout.getError().toString());
            });
        }
    }

    @Test
    public void clickSignup_emptyEmail_setsEmailError() {
        try (ActivityScenario<SignupActivity> scenario =
                     ActivityScenario.launch(SignupActivity.class)) {
            scenario.onActivity(activity -> {
                TextInputEditText nameEditText = activity.findViewById(R.id.nameEditText);
                nameEditText.setText("John Doe");

                activity.findViewById(R.id.signupButton).performClick();

                TextInputLayout emailLayout = activity.findViewById(R.id.emailLayout);
                assertEquals("Email is required", emailLayout.getError().toString());
            });
        }
    }

    @Test
    public void clickSignup_shortPassword_setsLengthError() {
        try (ActivityScenario<SignupActivity> scenario =
                     ActivityScenario.launch(SignupActivity.class)) {
            scenario.onActivity(activity -> {
                ((TextInputEditText) activity.findViewById(R.id.nameEditText)).setText("John Doe");
                ((TextInputEditText) activity.findViewById(R.id.emailEditText)).setText("john@gmail.com");
                ((TextInputEditText) activity.findViewById(R.id.passwordEditText)).setText("abc");

                activity.findViewById(R.id.signupButton).performClick();

                TextInputLayout passwordLayout = activity.findViewById(R.id.passwordLayout);
                assertEquals("Password must be at least 6 characters",
                        passwordLayout.getError().toString());
            });
        }
    }

    @Test
    public void clickSignup_passwordMismatch_setsConfirmError() {
        try (ActivityScenario<SignupActivity> scenario =
                     ActivityScenario.launch(SignupActivity.class)) {
            scenario.onActivity(activity -> {
                ((TextInputEditText) activity.findViewById(R.id.nameEditText)).setText("John Doe");
                ((TextInputEditText) activity.findViewById(R.id.emailEditText)).setText("john@gmail.com");
                ((TextInputEditText) activity.findViewById(R.id.passwordEditText)).setText("password123");
                ((TextInputEditText) activity.findViewById(R.id.confirmPasswordEditText)).setText("different");

                activity.findViewById(R.id.signupButton).performClick();

                TextInputLayout confirmLayout = activity.findViewById(R.id.confirmPasswordLayout);
                assertEquals("Passwords do not match", confirmLayout.getError().toString());
            });
        }
    }

    @Test
    public void clickSignup_validInputs_buttonBecomesDisabled() {
        try (ActivityScenario<SignupActivity> scenario =
                     ActivityScenario.launch(SignupActivity.class)) {
            scenario.onActivity(activity -> {
                ((TextInputEditText) activity.findViewById(R.id.nameEditText)).setText("John Doe");
                ((TextInputEditText) activity.findViewById(R.id.emailEditText)).setText("john@gmail.com");
                ((TextInputEditText) activity.findViewById(R.id.passwordEditText)).setText("password123");
                ((TextInputEditText) activity.findViewById(R.id.confirmPasswordEditText)).setText("password123");

                activity.findViewById(R.id.signupButton).performClick();

                MaterialButton signupButton = activity.findViewById(R.id.signupButton);
                assertFalse(signupButton.isEnabled());
            });
        }
    }
}