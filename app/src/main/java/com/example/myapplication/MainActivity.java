package com.example.myapplication;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.maps.MapView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    TextView txtMessage;
    Button btnClick;
    MapView mapView;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        txtMessage = findViewById(R.id.txtMessage);

        // Replace the old NavController line with this:
        // Replace the crashy lines (31-34) with this:
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(bottomNavigationView, navController);

            bottomNavigationView.setOnItemSelectedListener(item -> {
                boolean handled = NavigationUI.onNavDestinationSelected(item, navController);
                if (handled) {
                    int itemId = item.getItemId();
                    if (itemId == R.id.nav_home) {
                        txtMessage.setText("Home");
                    } else if (itemId == R.id.nav_fav) {
                        txtMessage.setText("Favorite");
                    } else if (itemId == R.id.nav_search) {
                        txtMessage.setText("Search");
                    } else if (itemId == R.id.nav_profile) {
                        txtMessage.setText("Profile");
                    }
                }
                return handled;
            });
        } else {
            // This logs a message to your console so you know the ID is still wrong
            android.util.Log.e("MainActivity", "Error: fragment_container not found in XML");
        }
    }

}
