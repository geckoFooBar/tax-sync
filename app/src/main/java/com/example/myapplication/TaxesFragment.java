package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TaxesFragment extends Fragment {

    private RecyclerView rvTaxes;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_taxes, container, false);

        rvTaxes = view.findViewById(R.id.rvTaxes);
        rvTaxes.setLayoutManager(new LinearLayoutManager(getContext()));

        List<TaxItem> taxList = new ArrayList<>();
        taxList.add(new TaxItem("Income Tax", "31 March 2026", "₹42,500"));
        taxList.add(new TaxItem("Property Tax", "15 April 2026", "₹12,000"));
        taxList.add(new TaxItem("Vehicle Tax", "30 June 2026", "₹3,200"));
        taxList.add(new TaxItem("Professional Tax", "Monthly", "₹200"));

        TaxAdapter adapter = new TaxAdapter(taxList);
        rvTaxes.setAdapter(adapter);

        return view;
    }
}
