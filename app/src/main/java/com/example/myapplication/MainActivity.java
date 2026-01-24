package com.example.myapplication;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    TextView txtMessage;
    Button btnClick;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        txtMessage = findViewById(R.id.txtMessage);
        // btnClick = findViewById(R.id.btnClick);
        // btnClick.setOnClickListener(view -> txtMessage.setText("Hello to TYBBA CA students!!!"));

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                txtMessage.setText("Home");
                return true;
            } else if(itemId == R.id.nav_fav) {
                txtMessage.setText("Favorite");
                return true;
            } else if (itemId == R.id.nav_search) {
                txtMessage.setText("Services");
                return true;
            } else if (itemId == R.id.nav_profile) {
                txtMessage.setText("Profile");
                return true;
            } else {
                return false;
            }

        });
    }

}
