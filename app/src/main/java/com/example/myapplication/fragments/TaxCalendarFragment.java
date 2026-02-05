package com.example.myapplication.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.myapplication.model.CalendarTaxItem;
import com.example.myapplication.R;
import com.example.myapplication.adapters.TaxCalendarAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaxCalendarFragment extends Fragment {

    private List<CalendarTaxItem> allTaxes;
    private TaxCalendarAdapter adapter;
    TextView tvMonthSummary;
    private Date currentVisibleMonth;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_tax_calendar, container, false);

        CalendarView calendarView = view.findViewById(R.id.calendarView);
        TextView tvSelectedDate = view.findViewById(R.id.tvSelectedDate);
        RecyclerView recyclerView = view.findViewById(R.id.rvTaxCalendar);

        tvMonthSummary = view.findViewById(R.id.tvMonthSummary);
        currentVisibleMonth = new Date();


        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 🔹 Existing data (UNCHANGED)
        allTaxes = new ArrayList<>();
        allTaxes.add(new CalendarTaxItem("Income Tax", "10 Feb 2026", "Upcoming"));
        allTaxes.add(new CalendarTaxItem("Property Tax", "15 Mar 2026", "Overdue"));
        allTaxes.add(new CalendarTaxItem("Vehicle Tax", "20 Apr 2026", "Upcoming"));
        allTaxes.add(new CalendarTaxItem("GST Payment", "10 Apr 2026", "Upcoming"));

        adapter = new TaxCalendarAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // Show today's taxes initially
        filterTaxesByDate(new Date(), tvSelectedDate);

        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            try {
                Date selectedDate = new SimpleDateFormat(
                        "dd MM yyyy", Locale.getDefault())
                        .parse(dayOfMonth + " " + (month + 1) + " " + year);

                filterTaxesByDate(selectedDate, tvSelectedDate);

                if (currentVisibleMonth == null ||
                        selectedDate.getMonth() != currentVisibleMonth.getMonth() ||
                        selectedDate.getYear() != currentVisibleMonth.getYear()) {

                    currentVisibleMonth = selectedDate;
                    updateMonthSummary(currentVisibleMonth);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });


        return view;
    }

    private void updateMonthSummary(Date date) {
        SimpleDateFormat monthFormat =
                new SimpleDateFormat("MMM yyyy", Locale.getDefault());

        String selectedMonth = monthFormat.format(date);
        int count = 0;

        for (CalendarTaxItem item : allTaxes) {
            if (item.getDueDate().contains(selectedMonth.split(" ")[0]) &&
                    item.getDueDate().contains(selectedMonth.split(" ")[1])) {
                count++;
            }
        }

        tvMonthSummary.setText("This month: " + count + " payment(s)");
    }

    private boolean hasTaxOnDate(String date) {
        for (CalendarTaxItem item : allTaxes) {
            if (item.getDueDate().equalsIgnoreCase(date)) {
                return true;
            }
        }
        return false;
    }

    private void filterTaxesByDate(Date selectedDate, TextView tvSelectedDate) {

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        String selected = sdf.format(selectedDate);

        tvSelectedDate.setText("Taxes on " + selected);

        List<CalendarTaxItem> filteredList = new ArrayList<>();

        for (CalendarTaxItem item : allTaxes) {
            if (item.getDueDate().equalsIgnoreCase(selected)) {
                filteredList.add(item);
            }
        }

        adapter.updateList(filteredList);
    }
}
