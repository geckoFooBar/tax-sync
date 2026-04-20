package com.example.myapplication.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.TaxItem;

import java.util.List;

public class CalendarTaxAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<TaxItem> taxList;

    public CalendarTaxAdapter(List<TaxItem> taxList) {
        this.taxList = taxList;
    }

    // Method to instantly swap the list when a user clicks a new date
    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<TaxItem> newList) {
        this.taxList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_tax, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder h = (ViewHolder) holder;
        TaxItem item = taxList.get(position);

        h.tvCalTaxTitle.setText(item.getTaxName());
        h.tvCalAmount.setText(item.getDisplayAmount());

        try {
            String[] dateParts = item.getDueDate().split("/");
            h.tvCalDay.setText(dateParts[0]);
            String[] months = {"JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC"};
            int monthIndex = Integer.parseInt(dateParts[1]) - 1;
            h.tvCalMonth.setText(months[monthIndex]);
        } catch (Exception e) {
            h.tvCalDay.setText("--");
            h.tvCalMonth.setText("---");
        }

        if (item.isPaid()) {
            h.tvCalStatusBadge.setText("CLEARED");
            h.tvCalStatusBadge.setTextColor(ContextCompat.getColor(h.itemView.getContext(), R.color.success_icon));
            h.tvCalStatusBadge.setBackgroundColor(ContextCompat.getColor(h.itemView.getContext(), R.color.success_bg));
        } else {
            h.tvCalStatusBadge.setText("ACTION REQUIRED");
            h.tvCalStatusBadge.setTextColor(ContextCompat.getColor(h.itemView.getContext(), R.color.error_text));
            h.tvCalStatusBadge.setBackgroundColor(ContextCompat.getColor(h.itemView.getContext(), R.color.error_bg));
        }
    }

    @Override
    public int getItemCount() {
        return taxList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCalDay, tvCalMonth, tvCalTaxTitle, tvCalStatusBadge, tvCalAmount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCalDay = itemView.findViewById(R.id.tvCalDay);
            tvCalMonth = itemView.findViewById(R.id.tvCalMonth);
            tvCalTaxTitle = itemView.findViewById(R.id.tvCalTaxTitle);
            tvCalStatusBadge = itemView.findViewById(R.id.tvCalStatusBadge);
            tvCalAmount = itemView.findViewById(R.id.tvCalAmount);
        }
    }
}