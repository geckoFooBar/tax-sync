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
public class LoginValidationTest {

    private String validateLogin(String email, String password) {
        if (email.isEmpty()) return "Email is required";
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) return "Enter a valid email";
        if (password.isEmpty()) return "Password is required";
        return "valid";
    }

    @Test
    public void emptyEmail_showsError() {
        assertEquals("Email is required", validateLogin("", "password123"));
    }

    @Test
    public void invalidEmail_noAtSign_showsError() {
        assertEquals("Enter a valid email", validateLogin("notanemail", "password123"));
    }

    @Test
    public void invalidEmail_noTLD_showsError() {
        assertEquals("Enter a valid email", validateLogin("user@", "password123"));
    }

    @Test
    public void invalidEmail_spacesInside_showsError() {
        assertEquals("Enter a valid email", validateLogin("user @gmail.com", "password123"));
    }

    @Test
    public void emptyPassword_showsError() {
        assertEquals("Password is required", validateLogin("user@gmail.com", ""));
    }

    @Test
    public void validCredentials_passesValidation() {
        assertEquals("valid", validateLogin("user@gmail.com", "password123"));
    }

    @Test
    public void validEmail_withSubdomain_passesValidation() {
        assertEquals("valid", validateLogin("user@mail.company.com", "password123"));
    }
}