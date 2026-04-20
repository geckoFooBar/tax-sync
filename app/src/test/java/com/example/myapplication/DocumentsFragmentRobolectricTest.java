package com.example.myapplication;

import android.view.View;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.fragments.DocumentsFragment;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class DocumentsFragmentRobolectricTest {

    // A standard Material theme to satisfy the ExtendedFloatingActionButton
    private static final int THEME_RES_ID = com.google.android.material.R.style.Theme_MaterialComponents_DayNight_NoActionBar;

    @Before
    public void setUp() {
        if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
            FirebaseApp.initializeApp(
                    ApplicationProvider.getApplicationContext(),
                    new FirebaseOptions.Builder()
                            .setApplicationId("1:000000000000:android:0000000000000000")
                            .setApiKey("fake-api-key")
                            .setProjectId("fake-project-id")
                            .build()
            );
        }
    }

    @Test
    public void fragmentLaunches_displaysBaseRequiredDocuments() {
        // Apply the Material theme here
        try (FragmentScenario<DocumentsFragment> scenario =
                     FragmentScenario.launchInContainer(DocumentsFragment.class, null, THEME_RES_ID)) {

            scenario.onFragment(fragment -> {
                RecyclerView rvDocuments = fragment.requireView().findViewById(R.id.rvDocuments);

                assertNotNull("RecyclerView should be present", rvDocuments);
                assertNotNull("Adapter should be attached", rvDocuments.getAdapter());

                assertEquals(3, rvDocuments.getAdapter().getItemCount());
            });
        }
    }

    @Test
    public void clickGeneralUploadFab_overlayIsInitiallyHidden() {
        // Apply the Material theme here
        try (FragmentScenario<DocumentsFragment> scenario =
                     FragmentScenario.launchInContainer(DocumentsFragment.class, null, THEME_RES_ID)) {

            scenario.onFragment(fragment -> {
                View overlay = fragment.requireView().findViewById(R.id.uploadProgressOverlay);
                ExtendedFloatingActionButton fab = fragment.requireView().findViewById(R.id.fabUploadDoc);

                assertEquals(View.GONE, overlay.getVisibility());
                assertTrue(fab.isClickable());
            });
        }
    }
}