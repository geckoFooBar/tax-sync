package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaxAdapter extends RecyclerView.Adapter<TaxAdapter.TaxViewHolder> {

    private List<TaxItem> taxList;

    public TaxAdapter(List<TaxItem> taxList) {
        this.taxList = taxList;
    }

    @NonNull
    @Override
    public TaxViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tax_card, parent, false);
        return new TaxViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaxViewHolder holder, int position) {
        TaxItem item = taxList.get(position);

        holder.tvTaxName.setText(item.getTaxName());
        holder.tvDueDate.setText("Due: " + item.getDueDate());
        holder.tvAmount.setText("Amount: " + item.getAmount());

        holder.btnPay.setOnClickListener(v -> {
            // Later: navigate to payment screen
        });
    }

    @Override
    public int getItemCount() {
        return taxList.size();
    }

    static class TaxViewHolder extends RecyclerView.ViewHolder {

        TextView tvTaxName, tvDueDate, tvAmount;
        Button btnPay;

        public TaxViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTaxName = itemView.findViewById(R.id.tvTaxName);
            tvDueDate = itemView.findViewById(R.id.tvDueDate);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            btnPay = itemView.findViewById(R.id.btnPay);
        }
    }
}
