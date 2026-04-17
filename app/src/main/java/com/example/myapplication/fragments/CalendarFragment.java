package com.example.myapplication.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// IMPORTANT: These are the new imports replacing android.widget.CalendarView
import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;

import com.example.myapplication.R;
import com.example.myapplication.adapters.CalendarTaxAdapter;
import com.example.myapplication.model.TaxItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarFragment extends Fragment {

    private CalendarView calendarView; // Now uses the custom widget
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
        RecyclerView rvCalendarTaxes = view.findViewById(R.id.rvCalendarTaxes);

        // Setup Recycler View
        rvCalendarTaxes.setLayoutManager(new LinearLayoutManager(getContext()));

        allTaxesMasterList = getDummyTaxes();

        // Initialize adapter
        adapter = new CalendarTaxAdapter(allTaxesMasterList);
        rvCalendarTaxes.setAdapter(adapter);

        // --- NEW: Map taxes to the calendar with Red/Green dots ---
        highlightTaxDates();

        // --- NEW: Updated Click Listener for the library ---
        calendarView.setOnDayClickListener(eventDay -> {
            Calendar clickedDayCalendar = eventDay.getCalendar();

            // Reconstruct the DD/MM/YYYY format to match your logic
            String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d",
                    clickedDayCalendar.get(Calendar.DAY_OF_MONTH),
                    clickedDayCalendar.get(Calendar.MONTH) + 1,
                    clickedDayCalendar.get(Calendar.YEAR));

            filterTaxesByDate(selectedDate);
        });
    }

    // NEW METHOD: Generates the dots on the calendar
    private void highlightTaxDates() {
        List<EventDay> events = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        for (TaxItem item : allTaxesMasterList) {
            try {
                Date date = sdf.parse(item.getDueDate());
                if (date != null) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);

                    // Pick the drawable based on paid status
                    int drawableRes = item.isPaid() ? R.drawable.dot_green : R.drawable.dot_red;

                    events.add(new EventDay(calendar, drawableRes));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        // Push the dots to the calendar UI
        calendarView.setEvents(events);
    }

    private void filterTaxesByDate(String targetDate) {
        List<TaxItem> filteredList = new ArrayList<>();

        for (TaxItem item : allTaxesMasterList) {
            if (item.getDueDate().equals(targetDate)) {
                filteredList.add(item);
            }
        }
        adapter.updateData(filteredList);
    }

    private List<TaxItem> getDummyTaxes() {
        List<TaxItem> list = new ArrayList<>();

        list.add(new TaxItem("GSTR-1 (March)", "11/04/2026", "₹12,400", 12400, true));
        list.add(new TaxItem("Provident Fund (March)", "15/04/2026", "₹4,500", 4500, true));
        list.add(new TaxItem("GSTR-3B (March)", "20/04/2026", "₹38,000", 38000, false));
        list.add(new TaxItem("TDS Payment (March)", "30/04/2026", "₹8,200", 8200, false));
        list.add(new TaxItem("TDS Return (Q4)", "31/05/2026", "₹0", 0, false));
        list.add(new TaxItem("Professional Tax", "31/05/2026", "₹200", 200, false));
        list.add(new TaxItem("Advance Tax (Q1)", "15/06/2026", "₹45,000", 45000, false));
        list.add(new TaxItem("GSTR-3B (May)", "20/06/2026", "₹15,600", 15600, false));
        list.add(new TaxItem("Income Tax Return (ITR)", "31/07/2026", "₹12,500", 12500, false));
        list.add(new TaxItem("Crypto / VDA Tax", "31/07/2026", "₹8,300", 8300, false));
        list.add(new TaxItem("Advance Tax (Q2)", "15/09/2026", "₹45,000", 45000, false));
        list.add(new TaxItem("Municipal Property Tax", "30/09/2026", "₹24,000", 24000, false));

        SharedPreferences prefs = requireActivity().getSharedPreferences("TaxAppPrefs", android.content.Context.MODE_PRIVATE);
        for (TaxItem item : list) {
            boolean isAlreadyPaid = prefs.getBoolean("status_" + item.getTaxName(), item.isPaid());
            item.setPaid(isAlreadyPaid);
        }

        return list;
    }
}