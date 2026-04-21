package com.example.myapplication.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.services.SupabaseAuthService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class SignupActivity extends AppCompatActivity {

    private TextInputEditText nameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private TextInputLayout nameLayout, emailLayout, passwordLayout, confirmPasswordLayout;
    private MaterialButton signupButton;

    private SupabaseAuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        authService = new SupabaseAuthService();

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

        loginText.setOnClickListener(v -> finish());
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

        // Create user in Supabase
        authService.signUp(email, password, name, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // Must return to main thread to update UI
                runOnUiThread(() -> {
                    signupButton.setEnabled(true);
                    signupButton.setText("Create Account");
                    Toast.makeText(SignupActivity.this, "Network Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseData = response.body().string();

                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        try {
                            // Extract Supabase Session Tokens
                            JSONObject json = new JSONObject(responseData);
                            String accessToken = json.optString("access_token", "");
                            String refreshToken = json.optString("refresh_token", "");

                            // Save local data, including our new JWTs!
                            SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean("isLoggedIn", true);
                            editor.putString("userName", name);
                            editor.putString("userEmail", email);
                            editor.putString("accessToken", accessToken);
                            editor.putString("refreshToken", refreshToken);
                            editor.apply();

                            Toast.makeText(SignupActivity.this, "Account Created Successfully!", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();

                        } catch (JSONException e) {
                            signupButton.setEnabled(true);
                            signupButton.setText("Create Account");
                            Toast.makeText(SignupActivity.this, "Data parsing error.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        signupButton.setEnabled(true);
                        signupButton.setText("Create Account");
                        try {
                            // Supabase usually sends a nice JSON error message
                            JSONObject errorJson = new JSONObject(responseData);
                            String errorMsg = errorJson.optString("msg", "Registration Failed");
                            Toast.makeText(SignupActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            Toast.makeText(SignupActivity.this, "Registration Failed: " + response.code(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }
}