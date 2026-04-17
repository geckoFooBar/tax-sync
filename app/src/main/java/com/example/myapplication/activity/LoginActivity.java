package com.example.myapplication.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText emailEditText, passwordEditText;
    private TextInputLayout emailLayout, passwordLayout;
    private CheckBox rememberMe;
    private MaterialButton loginButton;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Check if user is already logged in natively via Firebase
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Bind UI Elements
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        rememberMe = findViewById(R.id.rememberMe);
        loginButton = findViewById(R.id.loginButton);
        TextView signupText = findViewById(R.id.signupText);

        loginButton.setOnClickListener(v -> loginUser());

        signupText.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        });
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        emailLayout.setError(null);
        passwordLayout.setError(null);

        if (email.isEmpty()) { emailLayout.setError("Email is required"); return; }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { emailLayout.setError("Enter a valid email"); return; }
        if (password.isEmpty()) { passwordLayout.setError("Password is required"); return; }

        // Disable button while loading
        loginButton.setEnabled(false);
        loginButton.setText("Signing In...");

        // Authenticate with Firebase
        // Authenticate with Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {

                        FirebaseUser user = mAuth.getCurrentUser();

                        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();

                        if (rememberMe.isChecked()) {
                            editor.putBoolean("isLoggedIn", true);
                        }

                        // NEW: Restore the Name and Email from Firebase to local memory!
                        if (user != null) {
                            String fetchedName = user.getDisplayName();
                            String fetchedEmail = user.getEmail();

                            // Fallback just in case they signed up before we added the name feature
                            if (fetchedName == null || fetchedName.isEmpty()) {
                                fetchedName = "Taxpayer";
                            }

                            editor.putString("userName", fetchedName);
                            editor.putString("userEmail", fetchedEmail);
                        }

                        editor.apply();

                        Toast.makeText(LoginActivity.this, "Welcome Back!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        loginButton.setEnabled(true);
                        loginButton.setText("Sign In");
                        Toast.makeText(LoginActivity.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}