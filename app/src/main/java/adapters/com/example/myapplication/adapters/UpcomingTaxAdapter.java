package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.models.TaxItem;

import java.util.List;

public class UpcomingTaxAdapter extends RecyclerView.Adapter<UpcomingTaxAdapter.ViewHolder> {

    List<TaxItem> list;

    public UpcomingTaxAdapter(List<TaxItem> list) {
        this.list = list;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, date, amount;

        ViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.taxName);
            date = v.findViewById(R.id.taxDate);
            amount = v.findViewById(R.id.taxAmount);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_upcoming_tax, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder h, int pos) {
        TaxItem t = list.get(pos);
        h.name.setText(t.getTaxName());
        h.date.setText("Due: " + t.getDueDate());
        h.amount.setText(t.getAmount());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
