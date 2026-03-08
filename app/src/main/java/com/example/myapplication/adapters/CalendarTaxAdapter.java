package com.example.myapplication.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.TaxItem;

import java.util.List;

public class CalendarTaxAdapter extends RecyclerView.Adapter<CalendarTaxAdapter.ViewHolder> {

    private List<TaxItem> taxList;

    public CalendarTaxAdapter(List<TaxItem> taxList) {
        this.taxList = taxList;
    }

    // Method to instantly swap the list when a user clicks a new date
    public void updateData(List<TaxItem> newList) {
        this.taxList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_tax, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TaxItem item = taxList.get(position);

        holder.tvCalTaxTitle.setText(item.getTaxName());
        holder.tvCalAmount.setText(item.getDisplayAmount());

        // Simple trick to split "15/06/2026" into Day and Month for the UI Box
        try {
            String[] dateParts = item.getDueDate().split("/");
            holder.tvCalDay.setText(dateParts[0]);

            // Convert month number to text
            String[] months = {"JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};
            int monthIndex = Integer.parseInt(dateParts[1]) - 1;
            holder.tvCalMonth.setText(months[monthIndex]);
        } catch (Exception e) {
            holder.tvCalDay.setText("--");
            holder.tvCalMonth.setText("---");
        }

        // Apply Red (Due) / Green (Paid) logic
        if (item.isPaid()) {
            holder.tvCalStatusBadge.setText("CLEARED");
            holder.tvCalStatusBadge.setTextColor(Color.parseColor("#10B981"));
            holder.tvCalStatusBadge.setBackgroundColor(Color.parseColor("#ECFDF5"));
        } else {
            holder.tvCalStatusBadge.setText("ACTION REQUIRED");
            holder.tvCalStatusBadge.setTextColor(Color.parseColor("#EF4444"));
            holder.tvCalStatusBadge.setBackgroundColor(Color.parseColor("#FEF2F2"));
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