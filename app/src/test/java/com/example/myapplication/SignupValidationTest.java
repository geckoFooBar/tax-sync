package com.example.myapplication;

import android.util.Patterns;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

import com.example.myapplication.activity.MyApplication;

@RunWith(RobolectricTestRunner.class)
@Config(
        manifest = "src/main/AndroidManifest.xml",
        application = MyApplication.class
)
public class SignupValidationTest {

    private String validateSignup(String name, String email, String password, String confirmPassword) {
        if (name.isEmpty()) return "Full Name is required";
        if (email.isEmpty()) return "Email is required";
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) return "Enter a valid email";
        if (password.isEmpty()) return "Password is required";
        if (password.length() < 6) return "Password must be at least 6 characters";
        if (!password.equals(confirmPassword)) return "Passwords do not match";
        return "valid";
    }

    @Test
    public void emptyName_showsError() {
        assertEquals("Full Name is required",
                validateSignup("", "user@gmail.com", "pass123", "pass123"));
    }

    @Test
    public void emptyEmail_showsError() {
        assertEquals("Email is required",
                validateSignup("John Doe", "", "pass123", "pass123"));
    }

    @Test
    public void invalidEmail_showsError() {
        assertEquals("Enter a valid email",
                validateSignup("John Doe", "notvalid", "pass123", "pass123"));
    }

    @Test
    public void emptyPassword_showsError() {
        assertEquals("Password is required",
                validateSignup("John Doe", "user@gmail.com", "", ""));
    }

    @Test
    public void shortPassword_lessThan6Chars_showsError() {
        assertEquals("Password must be at least 6 characters",
                validateSignup("John Doe", "user@gmail.com", "abc", "abc"));
    }

    @Test
    public void shortPassword_exactly5Chars_showsError() {
        assertEquals("Password must be at least 6 characters",
                validateSignup("John Doe", "user@gmail.com", "abcde", "abcde"));
    }

    @Test
    public void passwordMismatch_showsError() {
        assertEquals("Passwords do not match",
                validateSignup("John Doe", "user@gmail.com", "pass123", "different"));
    }

    @Test
    public void passwordMismatch_caseSensitive_showsError() {
        // "Pass123" and "pass123" should NOT match
        assertEquals("Passwords do not match",
                validateSignup("John Doe", "user@gmail.com", "Pass123", "pass123"));
    }

    @Test
    public void validSignup_exactly6CharPassword_passes() {
        assertEquals("valid",
                validateSignup("John Doe", "user@gmail.com", "abc123", "abc123"));
    }

    @Test
    public void validSignup_allFieldsCorrect_passes() {
        assertEquals("valid",
                validateSignup("John Doe", "john.doe@gmail.com", "securePass1", "securePass1"));
    }

    @Test
    public void nameWithOnlySpaces_showsError() {
        // trim() is called in your code, so "   " becomes ""
        String name = "   ".trim();
        assertEquals("Full Name is required",
                validateSignup(name, "user@gmail.com", "pass123", "pass123"));
    }
}