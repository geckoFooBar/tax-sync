package com.example.myapplication.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.text.InputFilter;
import android.text.method.TransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

        TextView tvPaymentTaxName = sheetView.findViewById(R.id.tvPaymentTaxName);
        TextView tvPaymentAmount = sheetView.findViewById(R.id.tvPaymentAmount);
        com.google.android.material.button.MaterialButton btnConfirmPayment = sheetView.findViewById(R.id.btnConfirmPayment);

        View containerUPI = sheetView.findViewById(R.id.containerUPI);
        View containerNetBanking = sheetView.findViewById(R.id.containerNetBanking);
        View containerCard = sheetView.findViewById(R.id.containerCard);

        RadioGroup radioGroup = sheetView.findViewById(R.id.rgPaymentMethods);
        RadioButton rbUPI = sheetView.findViewById(R.id.rbUPI);
        RadioButton rbNetBanking = sheetView.findViewById(R.id.rbNetBanking);
        RadioButton rbCard = sheetView.findViewById(R.id.rbCard);

        TextInputEditText etUPI = sheetView.findViewById(R.id.etUPI);
        TextInputEditText etNetBanking = sheetView.findViewById(R.id.etNetBanking);
        TextInputEditText etCardPayment = sheetView.findViewById(R.id.etCardPayment);

        TextInputEditText etExpiry = sheetView.findViewById(R.id.etExpiry);

        tvPaymentTaxName.setText(item.getTaxName());
        tvPaymentAmount.setText(item.getDisplayAmount());

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            containerUPI.setVisibility(checkedId == rbUPI.getId() ? View.VISIBLE : View.GONE);
            containerNetBanking.setVisibility(checkedId == rbNetBanking.getId() ? View.VISIBLE : View.GONE);
            containerCard.setVisibility(checkedId == rbCard.getId() ? View.VISIBLE: View.GONE);
        });

        etCardPayment.setFilters(new InputFilter[] {
          new InputFilter.LengthFilter(16)
        });


        etExpiry.addTextChangedListener(new android.text.TextWatcher() {
            private boolean isUpdating = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdating) return;

                String input = s.toString().replace("/", "");
                StringBuilder formatted = new StringBuilder();

                if (input.length() >= 2) {
                    int month = Integer.parseInt(input.substring(0, 2));
                    if (month > 12 || month == 0) return;
                }

                for (int i = 0; i < input.length(); i++) {
                    char c = input.charAt(i);
                    if (i == 0 && c > '1') return;

                    formatted.append(c);
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

            if (containerUPI.getVisibility() == View.VISIBLE) {
                String upi = etUPI.getText().toString().trim();
                if (!upi.matches("^[\\w.-]+@[\\w.-]+$")) {
                    etUPI.setError("Enter valid UPI (username@bank)");
                    return;
                }
            }

            if (containerNetBanking.getVisibility() == View.VISIBLE) {
                String bankName = etNetBanking.getText().toString().trim();
                if (bankName.isEmpty()) {
                    etNetBanking.setError("Enter bank name");
                    return;
                }
            }

            if (containerCard.getVisibility() == View.VISIBLE) {
                if (etCardPayment.getText().toString().length() != 16) {
                    etCardPayment.setError("Card number must be 16 digits");
                    return;
                }
            }

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