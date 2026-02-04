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

public class DocumentsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_documents, container, false);

        RecyclerView rvDocuments = view.findViewById(R.id.rvDocuments);
        rvDocuments.setLayoutManager(new LinearLayoutManager(getContext()));

        List<DocumentItem> documents = new ArrayList<>();
        documents.add(new DocumentItem("Income Tax Return FY 24-25", "PDF", "20 Jan 2026"));
        documents.add(new DocumentItem("GST Registration Certificate", "PDF", "05 Dec 2025"));
        documents.add(new DocumentItem("Property Tax Receipt", "Image", "15 Nov 2025"));
        documents.add(new DocumentItem("Vehicle Insurance", "PDF", "02 Oct 2025"));

        rvDocuments.setAdapter(new DocumentsAdapter(documents));

        return view;
    }
}
