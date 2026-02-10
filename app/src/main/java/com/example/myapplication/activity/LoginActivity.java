package com.example.myapplication.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.R;

public class LoginActivity extends AppCompatActivity {

    EditText etUsername, etPassword;
    Button btn;

    private static final String DUMMY_USERNAME = "admin";
    private static final String DUMMY_PASSWORD = "admin123";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btn = findViewById(R.id.btnLogin);

        btn.setOnClickListener(v -> {

            String username = etUsername.getText().toString();
            String password = etPassword.getText().toString();

            if (username.equals(DUMMY_USERNAME) && password.equals(DUMMY_PASSWORD)) {

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this,
                "Invalid Username or Password",
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}