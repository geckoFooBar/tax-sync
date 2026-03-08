package com.example.myapplication.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.TaxItem;

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
                .inflate(R.layout.item_tax_row, parent, false);
        return new TaxViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaxViewHolder holder, int position) {
        TaxItem item = taxList.get(position);

        holder.tvTaxTitle.setText(item.getTaxName());
        holder.tvTaxSubtitle.setText("Due: " + item.getDueDate());
        holder.tvTaxAmount.setText(item.getDisplayAmount());

        // 1. Color Code the Badge (Red = Due, Green = Paid)
        if (item.isPaid()) {
            holder.tvStatusBadge.setText("PAID");
            holder.tvStatusBadge.setTextColor(Color.parseColor("#10B981")); // Green Text
            holder.tvStatusBadge.setBackgroundColor(Color.parseColor("#ECFDF5")); // Light Green Bg
        } else {
            holder.tvStatusBadge.setText("PENDING");
            holder.tvStatusBadge.setTextColor(Color.parseColor("#EF4444")); // Red Text
            holder.tvStatusBadge.setBackgroundColor(Color.parseColor("#FEF2F2")); // Light Red Bg
        }

        holder.itemView.setOnClickListener(v -> {
            if (!item.isPaid()) {
                showPaymentBottomSheet(v.getContext(), item, position);
            } else {
                Toast.makeText(v.getContext(), "This tax is already cleared.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showPaymentBottomSheet(Context context, TaxItem item, int position) {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog =
                new com.google.android.material.bottomsheet.BottomSheetDialog(context);

        View sheetView = LayoutInflater.from(context).inflate(R.layout.layout_bottom_sheet_payment, null);
        dialog.setContentView(sheetView);

        // Bind standard UI elements
        TextView tvPaymentTaxName = sheetView.findViewById(R.id.tvPaymentTaxName);
        TextView tvPaymentAmount = sheetView.findViewById(R.id.tvPaymentAmount);
        com.google.android.material.button.MaterialButton btnConfirmPayment = sheetView.findViewById(R.id.btnConfirmPayment);

        // Bind RadioGroup and the dynamic containers
        android.widget.RadioGroup rgPaymentMethods = sheetView.findViewById(R.id.rgPaymentMethods);
        View containerUPI = sheetView.findViewById(R.id.containerUPI);
        View containerNetBanking = sheetView.findViewById(R.id.containerNetBanking);
        View containerCard = sheetView.findViewById(R.id.containerCard);

        // Populate the sheet with the tax data
        tvPaymentTaxName.setText(item.getTaxName());
        tvPaymentAmount.setText(item.getDisplayAmount());
        btnConfirmPayment.setText("Pay " + item.getDisplayAmount());

        // Listen for Radio Button changes to swap the visible inputs
        rgPaymentMethods.setOnCheckedChangeListener((group, checkedId) -> {
            // Hide all containers first
            containerUPI.setVisibility(View.GONE);
            containerNetBanking.setVisibility(View.GONE);
            containerCard.setVisibility(View.GONE);

            // Reveal only the selected one
            if (checkedId == R.id.rbUPI) {
                containerUPI.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.rbNetBanking) {
                containerNetBanking.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.rbCard) {
                containerCard.setVisibility(View.VISIBLE);
            }
        });

        // Handle the actual payment logic
        btnConfirmPayment.setOnClickListener(v -> {
            item.setPaid(true);
            notifyItemChanged(position);

            SharedPreferences prefs = context.getSharedPreferences("TaxAppPrefs", Context.MODE_PRIVATE);
            prefs.edit().putBoolean("status_" + item.getTaxName(), true).apply();

            float currentTotalPaid = prefs.getFloat("totalTaxesPaid", 0f);
            float newTotal = currentTotalPaid + (float) item.getNumericAmount();
            prefs.edit().putFloat("totalTaxesPaid", newTotal).apply();

            dialog.dismiss();
            Toast.makeText(context, "Payment Processed via Gateway! Dashboard Updated.", Toast.LENGTH_LONG).show();
        });

        dialog.show();
    }

    @Override
    public int getItemCount() { return taxList.size(); }

    static class TaxViewHolder extends RecyclerView.ViewHolder {
        TextView tvTaxTitle, tvTaxSubtitle, tvTaxAmount, tvStatusBadge;

        public TaxViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTaxTitle = itemView.findViewById(R.id.tvTaxTitle);
            tvTaxSubtitle = itemView.findViewById(R.id.tvTaxSubtitle);
            tvTaxAmount = itemView.findViewById(R.id.tvTaxAmount);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge); // Bind the new badge
        }
    }
}