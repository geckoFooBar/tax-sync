package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaxCalendarAdapter extends RecyclerView.Adapter<TaxCalendarAdapter.CalendarViewHolder> {

    private List<CalendarTaxItem> calendarList;

    public TaxCalendarAdapter(List<CalendarTaxItem> calendarList) {
        this.calendarList = calendarList;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_tax, parent, false);
        return new CalendarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        CalendarTaxItem item = calendarList.get(position);

        holder.tvTaxTitle.setText(item.getTaxName());
        holder.tvDueDate.setText("Due: " + item.getDueDate());
        holder.tvStatus.setText(item.getStatus());
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), TaxDetailActivity.class);
            intent.putExtra("name", item.getTaxName());
            intent.putExtra("date", item.getDueDate());
            intent.putExtra("status", item.getStatus());
            v.getContext().startActivity(intent);
        });


        if (item.getStatus().equalsIgnoreCase("Overdue")) {
            holder.tvStatus.setTextColor(
                    holder.itemView.getContext().getColor(R.color.status_overdue)
            );
        } else {
            holder.tvStatus.setTextColor(
                    holder.itemView.getContext().getColor(R.color.status_upcoming)
            );
        }
    }

    @Override
    public int getItemCount() {
        return calendarList.size();
    }

    public void updateList(List<CalendarTaxItem> newList) {
        this.calendarList = newList;
        notifyDataSetChanged();
    }

    static class CalendarViewHolder extends RecyclerView.ViewHolder {

        TextView tvTaxTitle, tvDueDate, tvStatus;

        public CalendarViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTaxTitle = itemView.findViewById(R.id.tvTaxTitle);
            tvDueDate = itemView.findViewById(R.id.tvDueDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
