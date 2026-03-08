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

import com.example.myapplication.R;
import com.example.myapplication.adapters.TaxAdapter;
import com.example.myapplication.model.TaxItem;

import java.util.ArrayList;
import java.util.List;

public class TaxesFragment extends Fragment {

    private RecyclerView rvTaxes;
    private TaxAdapter taxAdapter;

    public TaxesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate your updated minimalist XML layout here
        return inflater.inflate(R.layout.fragment_taxes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvTaxes = view.findViewById(R.id.rvTaxes);
        rvTaxes.setLayoutManager(new LinearLayoutManager(getContext()));
        List<TaxItem> myTaxes = getDummyTaxes();
        taxAdapter = new TaxAdapter(myTaxes);
        rvTaxes.setAdapter(taxAdapter);
    }

    private List<TaxItem> getDummyTaxes() {
        List<TaxItem> list = new ArrayList<>();

        list.add(new TaxItem("Advance Tax (Q1)", "June 15, 2026", "₹45,000", 45000, false));
        list.add(new TaxItem("Capital Gains (LTCG)", "July 31, 2026", "₹12,500", 12500, false));
        list.add(new TaxItem("Municipal Property Tax", "March 31, 2026", "₹24,000", 24000, false));
        list.add(new TaxItem("Crypto / VDA Tax", "July 31, 2026", "₹8,300", 8300, false));
        list.add(new TaxItem("Professional Tax", "Monthly", "₹200", 200, false));

        SharedPreferences prefs = requireActivity().getSharedPreferences("TaxAppPrefs", android.content.Context.MODE_PRIVATE);
        for (TaxItem item : list) {
            boolean isAlreadyPaid = prefs.getBoolean("status_" + item.getTaxName(), item.isPaid());
            item.setPaid(isAlreadyPaid);
        }

        return list;
    }
}