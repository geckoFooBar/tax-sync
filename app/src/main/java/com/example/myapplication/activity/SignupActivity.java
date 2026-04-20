package com.example.myapplication.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class SignupActivity extends AppCompatActivity {

    private TextInputEditText nameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private TextInputLayout nameLayout, emailLayout, passwordLayout, confirmPasswordLayout;
    private MaterialButton signupButton;

    // Declare FirebaseAuth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Bind UI Elements
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);

        nameLayout = findViewById(R.id.nameLayout);
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout);

        signupButton = findViewById(R.id.signupButton);
        TextView loginText = findViewById(R.id.loginText);

        signupButton.setOnClickListener(v -> registerUser());

        loginText.setOnClickListener(v -> {
            finish();
        });
    }

    @SuppressLint("SetTextI18n")
    private void registerUser() {
        String name = Objects.requireNonNull(nameEditText.getText()).toString().trim();
        String email = Objects.requireNonNull(emailEditText.getText()).toString().trim();
        String password = Objects.requireNonNull(passwordEditText.getText()).toString().trim();
        String confirmPassword = Objects.requireNonNull(confirmPasswordEditText.getText()).toString().trim();

        nameLayout.setError(null);
        emailLayout.setError(null);
        passwordLayout.setError(null);
        confirmPasswordLayout.setError(null);

        // Validation
        if (name.isEmpty()) { nameLayout.setError("Full Name is required"); return; }
        if (email.isEmpty()) { emailLayout.setError("Email is required"); return; }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { emailLayout.setError("Enter a valid email"); return; }
        if (password.isEmpty()) { passwordLayout.setError("Password is required"); return; }
        if (password.length() < 6) { passwordLayout.setError("Password must be at least 6 characters"); return; }
        if (!password.equals(confirmPassword)) { confirmPasswordLayout.setError("Passwords do not match"); return; }

        // Disable button while loading
        signupButton.setEnabled(false);
        signupButton.setText("Creating Account...");

        // Create user in Firebase
        // Create user in Firebase
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Success! Get the newly created user
                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null) {
                            // NEW: Attach the Full Name to the official Firebase Cloud Profile!
                            com.google.firebase.auth.UserProfileChangeRequest profileUpdates =
                                    new com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                            .setDisplayName(name)
                                            .build();

                            user.updateProfile(profileUpdates);
                        }

                        // Save local data for immediate use
                        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean("isLoggedIn", true);
                        editor.putString("userName", name);
                        editor.putString("userEmail", email);
                        editor.apply();

                        Toast.makeText(SignupActivity.this, "Account Created Successfully!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        signupButton.setEnabled(true);
                        signupButton.setText("Create Account");
                        Toast.makeText(SignupActivity.this, "Error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}