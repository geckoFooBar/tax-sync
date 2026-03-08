package com.example.myapplication.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.TaxItem;
import com.google.android.material.textfield.TextInputEditText;

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

        // Standard UI bindings...
        TextView tvPaymentTaxName = sheetView.findViewById(R.id.tvPaymentTaxName);
        TextView tvPaymentAmount = sheetView.findViewById(R.id.tvPaymentAmount);
        com.google.android.material.button.MaterialButton btnConfirmPayment = sheetView.findViewById(R.id.btnConfirmPayment);

        // Payment containers and inputs
        View containerUPI = sheetView.findViewById(R.id.containerUPI);
        View containerNetBanking = sheetView.findViewById(R.id.containerNetBanking);
        View containerCard = sheetView.findViewById(R.id.containerCard);

        TextInputEditText etUPI = sheetView.findViewById(R.id.etUPI);
        TextInputEditText etCardPayment = sheetView.findViewById(R.id.etCardPayment);
        // Find the MM/YY EditText (you'll need to add an ID in XML or find by position)
        TextInputEditText etExpiry = sheetView.findViewById(R.id.etExpiry);
        // Note: It's better to add android:id="@+id/etExpiry" to your XML for the MM/YY field.

        tvPaymentTaxName.setText(item.getTaxName());
        tvPaymentAmount.setText(item.getDisplayAmount());

        etExpiry.addTextChangedListener(new android.text.TextWatcher() {
            private boolean isUpdating = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdating) return;

                String input = s.toString().replace("/", "");
                StringBuilder formatted = new StringBuilder();

                for (int i = 0; i < input.length(); i++) {
                    char c = input.charAt(i);

                    // Month validation logic
                    if (i == 0 && c > '1') return; // First digit of month can't be > 1
                    if (i == 1) {
                        int month = Integer.parseInt(input.substring(0, 2));
                        if (month > 12 || month == 0) return; // Month can't be > 12 or 00
                    }

                    formatted.append(c);
                    // Auto-insert slash after MM
                    if (i == 1 && input.length() > 2) {
                        formatted.append("/");
                    }
                }

                isUpdating = true;
                etExpiry.setText(formatted.toString());
                etExpiry.setSelection(formatted.length());
                isUpdating = false;
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        btnConfirmPayment.setOnClickListener(v -> {
            // --- 2. Validation Logic ---

            // UPI Validation
            if (containerUPI.getVisibility() == View.VISIBLE) {
                String upi = etUPI.getText().toString().trim();
                if (!upi.matches("^[\\w.-]+@[\\w.-]+$")) {
                    etUPI.setError("Enter valid UPI (username@bank)");
                    return;
                }
            }

            // Card Length Validation
            if (containerCard.getVisibility() == View.VISIBLE) {
                if (etCardPayment.getText().toString().length() != 16) {
                    etCardPayment.setError("Card number must be 16 digits");
                    return;
                }
            }

            // Process Payment (Existing Logic)
            item.setPaid(true);
            notifyItemChanged(position);

            SharedPreferences prefs = context.getSharedPreferences("TaxAppPrefs", Context.MODE_PRIVATE);
            prefs.edit().putBoolean("status_" + item.getTaxName(), true).apply();

            float currentTotalPaid = prefs.getFloat("totalTaxesPaid", 0f);
            float newTotal = currentTotalPaid + (float) item.getNumericAmount();
            prefs.edit().putFloat("totalTaxesPaid", newTotal).apply();

            dialog.dismiss();
            Toast.makeText(context, "Payment Processed!", Toast.LENGTH_LONG).show();
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
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
        }
    }
}