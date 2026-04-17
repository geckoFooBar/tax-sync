package com.example.myapplication.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.fragments.DashboardFragment;
import com.example.myapplication.fragments.DocumentsFragment;
import com.example.myapplication.fragments.ProfileFragment;
import com.example.myapplication.fragments.CalendarFragment;
import com.example.myapplication.fragments.TaxesFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences("TaxAppPrefs", Context.MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("isDarkMode", false);

        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        loadFragment(new DashboardFragment());

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;

            switch (item.getItemId()) {
                case R.id.nav_home:
                    fragment = new DashboardFragment();
                    break;
                case R.id.nav_taxes:
                    fragment = new TaxesFragment();
                    break;
                case R.id.nav_calendar:
                    fragment = new CalendarFragment();
                    break;
                case R.id.nav_documents:
                    fragment = new DocumentsFragment();
                    break;
                case R.id.nav_profile:
                    fragment = new ProfileFragment();
                    break;
            }

            return loadFragment(fragment);
        });
        FirebaseApp.initializeApp(this);
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}
