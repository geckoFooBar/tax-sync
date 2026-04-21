package com.example.myapplication.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.activity.LoginActivity;
import com.google.android.material.button.MaterialButton;

public class ProfileFragment extends Fragment {

    public ProfileFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView profileName = view.findViewById(R.id.profileName);
        TextView profileEmail = view.findViewById(R.id.profileEmail);
        TextView profileInitials = view.findViewById(R.id.profileInitials);
        MaterialButton logoutButton = view.findViewById(R.id.logoutButton);

        TextView btnSettings = view.findViewById(R.id.btnSettings);
        TextView btnTaxHistory = view.findViewById(R.id.btnTaxHistory);
        TextView btnHelp = view.findViewById(R.id.btnHelp);

        SwitchCompat switchTheme = view.findViewById(R.id.switchTheme);

        SharedPreferences authPrefs = requireActivity().getSharedPreferences("auth", Context.MODE_PRIVATE);
        SharedPreferences prefs = requireActivity().getSharedPreferences("TaxAppPrefs", Context.MODE_PRIVATE);

        boolean isDarkMode = prefs.getBoolean("isDarkMode", false);

        String name = authPrefs.getString("userName", "User");
        String email = authPrefs.getString("userEmail", "No Email Provided");

        profileName.setText(name);
        profileEmail.setText(email);

        try {
            assert name != null;
            String[] nameParts = name.trim().split("\\s+");
            if (nameParts.length > 1) {
                String initials = String.valueOf(nameParts[0].charAt(0)) + String.valueOf(nameParts[1].charAt(0));
                profileInitials.setText(initials.toUpperCase());
            } else if (nameParts.length == 1 && !nameParts[0].isEmpty()) {
                profileInitials.setText(String.valueOf(nameParts[0].charAt(0)).toUpperCase());
            } else {
                profileInitials.setText("U");
            }
        } catch (Exception e) {
            profileInitials.setText("U");
        }

        btnSettings.setOnClickListener(v ->
                Toast.makeText(getContext(), "Opening Account Settings...", Toast.LENGTH_SHORT).show()
        );

        btnTaxHistory.setOnClickListener(v ->
                Toast.makeText(getContext(), "Loading past filings...", Toast.LENGTH_SHORT).show()
        );

        btnHelp.setOnClickListener(v ->
                Toast.makeText(getContext(), "Opening Support Center...", Toast.LENGTH_SHORT).show()
        );

        switchTheme.setChecked(isDarkMode);

        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("isDarkMode", isChecked).apply();

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }

            requireActivity().recreate();
        });

        logoutButton.setOnClickListener(v -> logoutUser());
    }

    private void logoutUser() {
        SharedPreferences authPrefs = requireActivity().getSharedPreferences("auth", Context.MODE_PRIVATE);
        SharedPreferences.Editor authEditor = authPrefs.edit();
        authEditor.clear();
        authEditor.putBoolean("isLoggedIn", false);
        authEditor.apply();

        SharedPreferences taxPrefs = requireActivity().getSharedPreferences("TaxAppPrefs", Context.MODE_PRIVATE);
        taxPrefs.edit().clear().apply();

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}