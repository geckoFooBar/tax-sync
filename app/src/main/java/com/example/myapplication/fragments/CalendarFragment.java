package com.example.myapplication.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.CalendarTaxAdapter;
import com.example.myapplication.model.TaxItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CalendarFragment extends Fragment {

    private CalendarView calendarView;
    private RecyclerView rvCalendarTaxes;
    private CalendarTaxAdapter adapter;
    private List<TaxItem> allTaxesMasterList;

    public CalendarFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        calendarView = view.findViewById(R.id.calendarView);
        rvCalendarTaxes = view.findViewById(R.id.rvCalendarTaxes);

        // Setup Recycler View
        rvCalendarTaxes.setLayoutManager(new LinearLayoutManager(getContext()));

        // Load dummy data (Notice the DD/MM/YYYY format to make matching exact)
        allTaxesMasterList = getDummyTaxes();

        // Initialize adapter with ALL taxes first, or an empty list. Let's show all initially.
        adapter = new CalendarTaxAdapter(allTaxesMasterList);
        rvCalendarTaxes.setAdapter(adapter);

        // Listen for user clicking on dates!
        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            // Android months are 0-indexed (Jan = 0), so we add 1.
            // We use %02d to ensure single digits have a leading zero (e.g., "05" instead of "5")
            String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year);

            filterTaxesByDate(selectedDate);
        });
    }

    private void filterTaxesByDate(String targetDate) {
        List<TaxItem> filteredList = new ArrayList<>();

        for (TaxItem item : allTaxesMasterList) {
            if (item.getDueDate().equals(targetDate)) {
                filteredList.add(item);
            }
        }

        // Push the filtered list to the adapter
        adapter.updateData(filteredList);
    }

    // Hardcoded dummy data for testing the calendar clicks
    private List<TaxItem> getDummyTaxes() {
        List<TaxItem> list = new ArrayList<>();

        // Set these dates to upcoming days on your calendar to test the click filter!
        list.add(new TaxItem("Advance Tax (Q1)", "15/06/2026", "₹45,000", 45000, false));
        list.add(new TaxItem("Capital Gains (LTCG)", "31/07/2026", "₹12,500", 12500, false));
        list.add(new TaxItem("Municipal Property Tax", "31/03/2026", "₹24,000", 24000, false)); // Already Paid Example
        list.add(new TaxItem("Crypto / VDA Tax", "31/07/2026", "₹8,300", 8300, false));

        SharedPreferences prefs = requireActivity().getSharedPreferences("TaxAppPrefs", android.content.Context.MODE_PRIVATE);
        for (TaxItem item : list) {
            // Looks for a saved boolean like "status_Advance Tax (Q1)". If it doesn't exist, it defaults to the item's current status.
            boolean isAlreadyPaid = prefs.getBoolean("status_" + item.getTaxName(), item.isPaid());
            item.setPaid(isAlreadyPaid);
        }

        return list;
    }
}