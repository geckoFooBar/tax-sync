package com.example.myapplication.fragments;

import android.annotation.SuppressLint;
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

import com.example.myapplication.R;
import com.example.myapplication.adapters.TaxAdapter;
import com.example.myapplication.model.TaxItem;

import java.util.ArrayList;
import java.util.List;

public class TaxesFragment extends Fragment {

    private TaxAdapter taxAdapter;

    // We keep all data here, but only pass unpaid items to the adapter
    private List<TaxItem> masterTaxList;
    private List<TaxItem> displayList; // The filtered list shown on screen

    public TaxesFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_taxes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rvTaxes = view.findViewById(R.id.rvTaxes);
        rvTaxes.setLayoutManager(new LinearLayoutManager(getContext()));

        masterTaxList = getDummyTaxes();
        displayList = new ArrayList<>();

        // This method will filter out paid taxes and update the Dashboard total
        processTaxesAndDashboard();

        // Pass ONLY the unpaid taxes (displayList) to the adapter
        taxAdapter = new TaxAdapter(displayList, this::markTaxAsPaid);
        rvTaxes.setAdapter(taxAdapter);
    }

    // --- Core Logic: Separates Paid vs Unpaid & Updates Dashboard ---
    @SuppressLint("NotifyDataSetChanged")
    public void processTaxesAndDashboard() {
        float totalPaid = 0;
        displayList.clear(); // Clear the screen list before rebuilding

        for (TaxItem item : masterTaxList) {
            if (item.isPaid()) {
                // If paid, do NOT add to screen. Just add to Dashboard total.
                totalPaid += (float) item.getNumericAmount();
            } else {
                // If unpaid, show it on this Fragment screen.
                displayList.add(item);
            }
        }

        // Save the total of hidden/paid taxes to SharedPreferences for DashboardFragment
        SharedPreferences prefs = requireActivity().getSharedPreferences("TaxAppPrefs", android.content.Context.MODE_PRIVATE);
        prefs.edit().putFloat("totalTaxesPaid", totalPaid).apply();

        // Tell the adapter the list has changed (removes items that were just paid)
        if (taxAdapter != null) {
            taxAdapter.notifyDataSetChanged();
        }
    }

    public void markTaxAsPaid(int position) {

        TaxItem item = displayList.get(position);

        item.setPaid(true);

        SharedPreferences prefs = requireActivity().getSharedPreferences("TaxAppPrefs", android.content.Context.MODE_PRIVATE);
        prefs.edit().putBoolean("status_" + item.getTaxName(), true).apply();

        processTaxesAndDashboard();
    }

    private List<TaxItem> getDummyTaxes() {
        List<TaxItem> list = new ArrayList<>();

        // --- CURRENT MONTH: APRIL 2026 ---
        // Notice these two are true. They will NOT appear in the list, but will show up as ₹16,900 cleared in the Dashboard!
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