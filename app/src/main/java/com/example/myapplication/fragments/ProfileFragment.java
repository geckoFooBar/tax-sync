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
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.activity.LoginActivity;
import com.google.android.material.button.MaterialButton;

public class ProfileFragment extends Fragment {

    private MaterialButton logoutButton;
    private TextView profileName, profileEmail, profileInitials;
    private TextView btnSettings, btnTaxHistory, btnHelp;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Bind UI Elements
        profileName = view.findViewById(R.id.profileName);
        profileEmail = view.findViewById(R.id.profileEmail);
        profileInitials = view.findViewById(R.id.profileInitials);
        logoutButton = view.findViewById(R.id.logoutButton);

        btnSettings = view.findViewById(R.id.btnSettings);
        btnTaxHistory = view.findViewById(R.id.btnTaxHistory);
        btnHelp = view.findViewById(R.id.btnHelp);

        SharedPreferences authPrefs = requireActivity().getSharedPreferences("auth", Context.MODE_PRIVATE);

        // Fetch the saved strings. If nothing is found, it defaults to "User" and "No Email"
        String name = authPrefs.getString("userName", "User");
        String email = authPrefs.getString("userEmail", "No Email Provided");

        profileName.setText(name);
        profileEmail.setText(email);

        try {
            String[] nameParts = name.trim().split("\\s+");
            if (nameParts.length > 1) {
                String initials = String.valueOf(nameParts[0].charAt(0)) + String.valueOf(nameParts[1].charAt(0));
                profileInitials.setText(initials.toUpperCase());
            } else if (nameParts.length == 1 && !nameParts[0].isEmpty()) {
                // If they only entered a first name
                profileInitials.setText(String.valueOf(nameParts[0].charAt(0)).toUpperCase());
            } else {
                profileInitials.setText("U");
            }
        } catch (Exception e) {
            profileInitials.setText("U"); // Fallback for 'User'
        }

        // 3. Setup Click Listeners
        btnSettings.setOnClickListener(v ->
                Toast.makeText(getContext(), "Opening Account Settings...", Toast.LENGTH_SHORT).show()
        );

        btnTaxHistory.setOnClickListener(v ->
                Toast.makeText(getContext(), "Loading past filings...", Toast.LENGTH_SHORT).show()
        );

        btnHelp.setOnClickListener(v ->
                Toast.makeText(getContext(), "Opening Support Center...", Toast.LENGTH_SHORT).show()
        );

        // 4. Logout Logic
        logoutButton.setOnClickListener(v -> logoutUser());
    }

    private void logoutUser() {
        // 1. Explicitly log out of Firebase's servers
        try {
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
        } catch (Exception e) {
            // Failsafe just in case Firebase isn't initialized properly
        }

        // 2. Wipe the Authentication Memory
        SharedPreferences authPrefs = requireActivity().getSharedPreferences("auth", Context.MODE_PRIVATE);
        SharedPreferences.Editor authEditor = authPrefs.edit();
        authEditor.clear(); // Wipes everything
        authEditor.putBoolean("isLoggedIn", false); // Forces the flag to false just to be bulletproof
        authEditor.apply();

        // 3. Wipe the Financial Data Memory (So a new user doesn't see the old user's taxes)
        SharedPreferences taxPrefs = requireActivity().getSharedPreferences("TaxAppPrefs", Context.MODE_PRIVATE);
        taxPrefs.edit().clear().apply();

        // 4. Send them back to Login and completely destroy the Dashboard from the phone's back-stack
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}