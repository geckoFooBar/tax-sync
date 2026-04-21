package com.example.myapplication.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.CheckBox;
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

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText emailEditText, passwordEditText;
    private TextInputLayout emailLayout, passwordLayout;
    private CheckBox rememberMe;
    private MaterialButton loginButton;

    private SupabaseAuthService authService;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        prefs = getSharedPreferences("auth", MODE_PRIVATE);

        // Check if user is already logged in locally
        if (prefs.getBoolean("isLoggedIn", false)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Initialize our custom unified auth service
        authService = new SupabaseAuthService();

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

    @SuppressLint("SetTextI18n")
    private void loginUser() {
        String email = Objects.requireNonNull(emailEditText.getText()).toString().trim();
        String password = Objects.requireNonNull(passwordEditText.getText()).toString().trim();

        emailLayout.setError(null);
        passwordLayout.setError(null);

        if (email.isEmpty()) { emailLayout.setError("Email is required"); return; }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { emailLayout.setError("Enter a valid email"); return; }
        if (password.isEmpty()) { passwordLayout.setError("Password is required"); return; }

        // Disable button while loading
        loginButton.setEnabled(false);
        loginButton.setText("Signing In...");

        // Authenticate with Supabase
        authService.login(email, password, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    loginButton.setEnabled(true);
                    loginButton.setText("Sign In");
                    Toast.makeText(LoginActivity.this, "Network Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseData = response.body().string();

                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        try {
                            // Extract Supabase Session Data
                            JSONObject json = new JSONObject(responseData);
                            String accessToken = json.optString("access_token", "");
                            String refreshToken = json.optString("refresh_token", "");

                            // Extract user info from nested JSON object
                            JSONObject userObj = json.optJSONObject("user");
                            String fetchedName = "Taxpayer"; // Fallback
                            String fetchedEmail = email;

                            if (userObj != null) {
                                fetchedEmail = userObj.optString("email", email);
                                JSONObject metadata = userObj.optJSONObject("user_metadata");
                                if (metadata != null) {
                                    fetchedName = metadata.optString("full_name", "Taxpayer");
                                }
                            }

                            SharedPreferences.Editor editor = prefs.edit();

                            if (rememberMe.isChecked()) {
                                editor.putBoolean("isLoggedIn", true);
                            }

                            // Save tokens and user data for the session
                            editor.putString("userName", fetchedName);
                            editor.putString("userEmail", fetchedEmail);
                            editor.putString("accessToken", accessToken);
                            editor.putString("refreshToken", refreshToken);
                            editor.apply();

                            Toast.makeText(LoginActivity.this, "Welcome Back!", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();

                        } catch (JSONException e) {
                            loginButton.setEnabled(true);
                            loginButton.setText("Sign In");
                            Toast.makeText(LoginActivity.this, "Data parsing error.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        loginButton.setEnabled(true);
                        loginButton.setText("Sign In");
                        try {
                            JSONObject errorJson = new JSONObject(responseData);
                            String errorMsg = errorJson.optString("error_description", "Invalid login credentials");
                            Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            Toast.makeText(LoginActivity.this, "Authentication failed: " + response.code(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }
}