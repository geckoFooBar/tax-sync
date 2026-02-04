package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Quick Actions
        RecyclerView quickActions = view.findViewById(R.id.recyclerQuickActions);
        quickActions.setLayoutManager(new GridLayoutManager(getContext(), 3));
        quickActions.setAdapter(new QuickActionAdapter());

        // Upcoming Taxes
        RecyclerView upcomingTaxes = view.findViewById(R.id.recyclerUpcomingTaxes);
        upcomingTaxes.setLayoutManager(new LinearLayoutManager(getContext()));

        List<TaxItem> taxes = new ArrayList<>();
        taxes.add(new TaxItem("Income Tax", "31 Mar 2026", "₹12,000"));
        taxes.add(new TaxItem("Property Tax", "10 Apr 2026", "₹5,500"));
        taxes.add(new TaxItem("Vehicle Tax", "20 May 2026", "₹2,000"));

        upcomingTaxes.setAdapter(new UpcomingTaxAdapter(taxes));

        return view;
    }
}
