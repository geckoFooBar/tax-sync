package com.example.myapplication.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.InputFilter;
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
import java.util.Objects;

public class TaxAdapter extends RecyclerView.Adapter<TaxAdapter.TaxViewHolder> {

    private final List<TaxItem> taxList;
    private final OnTaxPaymentListener paymentListener;
    private String rawCardNumber = "";

    public interface OnTaxPaymentListener {
        void onTaxPaid(int position);
    }

    public TaxAdapter(List<TaxItem> taxList, OnTaxPaymentListener listener) {
        this.taxList = taxList;
        this.paymentListener = listener;
    }

    @NonNull
    @Override
    public TaxViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tax_row, parent, false);
        return new TaxViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull TaxViewHolder holder, int position) {
        TaxItem item = taxList.get(position);

        holder.tvTaxTitle.setText(item.getTaxName());
        holder.tvTaxSubtitle.setText("Due: " + item.getDueDate());
        holder.tvTaxAmount.setText(item.getDisplayAmount());

        // Color Code the Badge
        if (item.isPaid()) {
            holder.tvStatusBadge.setText("PAID");
            holder.tvStatusBadge.setTextColor(Color.parseColor("#10B981"));
            holder.tvStatusBadge.setBackgroundColor(Color.parseColor("#ECFDF5"));
        } else {
            holder.tvStatusBadge.setText("PENDING");
            holder.tvStatusBadge.setTextColor(Color.parseColor("#EF4444"));
            holder.tvStatusBadge.setBackgroundColor(Color.parseColor("#FEF2F2"));
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

        @SuppressLint("InflateParams") View sheetView = LayoutInflater.from(context).inflate(R.layout.layout_bottom_sheet_payment, null);
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
        androidx.recyclerview.widget.RecyclerView rvBankList = sheetView.findViewById(R.id.rvBankList);
        com.google.android.material.chip.Chip chipSelectedBank = sheetView.findViewById(R.id.chipSelectedBank);
        TextInputEditText etAccountNumber = sheetView.findViewById(R.id.etAccountNumber);
        TextInputEditText etConfirmAccount = sheetView.findViewById(R.id.etConfirmAccount);
        TextInputEditText etIFSC = sheetView.findViewById(R.id.etIFSC);
        android.widget.RadioGroup rgAccountType = sheetView.findViewById(R.id.rgAccountType);
        TextInputEditText etCardPayment = sheetView.findViewById(R.id.etCardPayment);
        TextInputEditText etExpiry = sheetView.findViewById(R.id.etExpiry);

        tvPaymentTaxName.setText(item.getTaxName());
        tvPaymentAmount.setText(item.getDisplayAmount());

        // ── Indian Banks List ──────────────────────────────────────────────────────
        java.util.List<String> allBanks = java.util.Arrays.asList(
                "State Bank of India (SBI)",
                "HDFC Bank",
                "ICICI Bank",
                "Axis Bank",
                "Punjab National Bank (PNB)",
                "Bank of Baroda",
                "Canara Bank",
                "Union Bank of India",
                "Kotak Mahindra Bank",
                "IndusInd Bank",
                "IDFC First Bank",
                "Yes Bank",
                "Federal Bank",
                "South Indian Bank",
                "Karnataka Bank",
                "Bank of India",
                "Central Bank of India",
                "Indian Bank",
                "UCO Bank",
                "Bank of Maharashtra",
                "Indian Overseas Bank",
                "Punjab & Sind Bank",
                "City Union Bank",
                "Dhanlaxmi Bank",
                "Jammu & Kashmir Bank",
                "Karur Vysya Bank",
                "Lakshmi Vilas Bank",
                "Nainital Bank",
                "RBL Bank",
                "Tamilnad Mercantile Bank",
                "DCB Bank",
                "CSB Bank",
                "Bandhan Bank",
                "AU Small Finance Bank",
                "Equitas Small Finance Bank",
                "ESAF Small Finance Bank",
                "Ujjivan Small Finance Bank",
                "Suryoday Small Finance Bank",
                "Jana Small Finance Bank",
                "Fincare Small Finance Bank",
                "IDBI Bank",
                "Saraswat Bank",
                "Abhyudaya Bank",
                "Cosmos Bank",
                "Shamrao Vithal Bank",
                "NKGSB Bank",
                "Bassein Catholic Bank",
                "Apna Sahakari Bank",
                "Greater Bombay Bank"
        );

        // Simple inline adapter for the bank suggestion list
        final String[] selectedBankHolder = {""};

        android.widget.ArrayAdapter<String> bankAdapter = new android.widget.ArrayAdapter<String>(
                context, android.R.layout.simple_list_item_1, new java.util.ArrayList<>()) {
        };

        rvBankList.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(context));

        // Use a simple RecyclerView adapter for bank suggestions
        final java.util.List<String> filteredBanks = new java.util.ArrayList<>();
        androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>
                bankRvAdapter = new androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {
            @NonNull
            @Override
            public androidx.recyclerview.widget.RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent2, int viewType2) {
                TextView tv = new TextView(parent2.getContext());
                tv.setPadding(32, 24, 32, 24);
                tv.setTextSize(14);
                tv.setTextColor(android.graphics.Color.parseColor("#111827"));
                tv.setBackground(getBankItemBackground(parent2.getContext()));
                tv.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                return new androidx.recyclerview.widget.RecyclerView.ViewHolder(tv) {};
            }

            @Override
            public void onBindViewHolder(@NonNull androidx.recyclerview.widget.RecyclerView.ViewHolder holder, int pos) {
                TextView tv = (TextView) holder.itemView;
                String bankName = filteredBanks.get(pos);
                tv.setText(bankName);
                tv.setOnClickListener(vv -> {
                    selectedBankHolder[0] = bankName;
                    chipSelectedBank.setText(bankName);
                    chipSelectedBank.setVisibility(View.VISIBLE);
                    rvBankList.setVisibility(View.GONE);
                    etNetBanking.setText("");
                    etNetBanking.clearFocus();
                    // Hide keyboard
                    android.view.inputmethod.InputMethodManager imm =
                            (android.view.inputmethod.InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) imm.hideSoftInputFromWindow(etNetBanking.getWindowToken(), 0);
                });
            }

            @Override
            public int getItemCount() { return filteredBanks.size(); }
        };
        rvBankList.setAdapter(bankRvAdapter);

        // Filter banks as user types
        etNetBanking.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override
            public void afterTextChanged(android.text.Editable s) {
                String query = s.toString().trim().toLowerCase();
                filteredBanks.clear();
                if (!query.isEmpty()) {
                    for (String bank : allBanks) {
                        if (bank.toLowerCase().contains(query)) filteredBanks.add(bank);
                    }
                }
                bankRvAdapter.notifyDataSetChanged();
                rvBankList.setVisibility(!query.isEmpty() && !filteredBanks.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });

        // Chip close clears the selection
        chipSelectedBank.setOnCloseIconClickListener(vv -> {
            selectedBankHolder[0] = "";
            chipSelectedBank.setVisibility(View.GONE);
            chipSelectedBank.setText("No bank selected");
            etNetBanking.setText("");
            etNetBanking.requestFocus();
        });

        // Auto-uppercase IFSC as user types
        etIFSC.addTextChangedListener(new android.text.TextWatcher() {
            private boolean isUpdating = false;
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override
            public void afterTextChanged(android.text.Editable s) {
                if (isUpdating) return;
                isUpdating = true;
                String upper = s.toString().toUpperCase();
                s.replace(0, s.length(), upper);
                isUpdating = false;
            }
        });

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            containerUPI.setVisibility(checkedId == rbUPI.getId() ? View.VISIBLE : View.GONE);
            containerNetBanking.setVisibility(checkedId == rbNetBanking.getId() ? View.VISIBLE : View.GONE);
            containerCard.setVisibility(checkedId == rbCard.getId() ? View.VISIBLE : View.GONE);
        });

        // --- FIX 1: TextWatcher now skips re-formatting when the field is already masked ---
        etCardPayment.addTextChangedListener(new android.text.TextWatcher() {
            private boolean isUpdating = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                if (isUpdating) return;
                isUpdating = true;

                // Strip everything except digits and asterisks
                String raw = s.toString().replaceAll("[^0-9*]", "");

                // If the field is masked (contains asterisks), don't reformat — leave it as-is
                if (raw.contains("*")) {
                    isUpdating = false;
                    return;
                }

                // Only reach here when the user is actively typing digits
                rawCardNumber = raw;

                // Build the formatted string with spaces every 4 digits
                StringBuilder formatted = new StringBuilder();
                for (int i = 0; i < raw.length(); i++) {
                    if (i > 0 && i % 4 == 0) {
                        formatted.append(" ");
                    }
                    formatted.append(raw.charAt(i));
                }

                s.replace(0, s.length(), formatted.toString());
                isUpdating = false;
            }
        });

        etCardPayment.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                // User tapped away — mask all but the last 4 digits
                if (rawCardNumber.length() >= 4) {
                    String lastFour = rawCardNumber.substring(rawCardNumber.length() - 4);
                    // Build asterisk prefix to match the total card length
                    String asterisks = "*".repeat(rawCardNumber.length() - 4);
                    String fullMasked = asterisks + lastFour;

                    // Format with spaces every 4 characters
                    StringBuilder formatted = new StringBuilder();
                    for (int i = 0; i < fullMasked.length(); i++) {
                        if (i > 0 && i % 4 == 0) formatted.append(" ");
                        formatted.append(fullMasked.charAt(i));
                    }
                    // Setting masked text — the TextWatcher's early return (Fix 1)
                    // ensures it won't strip the asterisks
                    etCardPayment.setText(formatted.toString());
                }
            } else {
                // User tapped back in — reveal the raw digits for editing
                if (!rawCardNumber.isEmpty()) {
                    etCardPayment.setText(rawCardNumber);
                    if (etCardPayment.getText() != null) {
                        etCardPayment.setSelection(etCardPayment.getText().length());
                    }
                }
            }
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
                String upi = Objects.requireNonNull(etUPI.getText()).toString().trim();
                if (!upi.matches("^[\\w.-]+@[\\w.-]+$")) {
                    etUPI.setError("Enter valid UPI (username@bank)");
                    return;
                }
            }

            if (containerNetBanking.getVisibility() == View.VISIBLE) {
                String selectedBank = chipSelectedBank.getVisibility() == View.VISIBLE
                        ? chipSelectedBank.getText().toString() : "";
                if (selectedBank.isEmpty() || selectedBank.equals("No bank selected")) {
                    etNetBanking.setError("Please select a bank");
                    return;
                }
                String accountNo = Objects.requireNonNull(etAccountNumber.getText()).toString().trim();
                if (accountNo.length() < 9) {
                    etAccountNumber.setError("Enter a valid account number (min 9 digits)");
                    return;
                }
                String confirmAccountNo = Objects.requireNonNull(etConfirmAccount.getText()).toString().trim();
                if (!accountNo.equals(confirmAccountNo)) {
                    etConfirmAccount.setError("Account numbers do not match");
                    return;
                }
                String ifsc = Objects.requireNonNull(etIFSC.getText()).toString().trim().toUpperCase();
                if (!ifsc.matches("^[A-Z]{4}0[A-Z0-9]{6}$")) {
                    etIFSC.setError("Enter valid IFSC (e.g. SBIN0001234)");
                    return;
                }

                // Read selected account type for confirmation toast
                int checkedAccountType = rgAccountType.getCheckedRadioButtonId();
                String accountType = "Savings";
                if (checkedAccountType == R.id.rbCurrent) accountType = "Current";
                else if (checkedAccountType == R.id.rbNRE) accountType = "NRE/NRO";

                android.util.Log.d("TaxAdapter", "Net Banking payment: bank=" + selectedBankHolder[0]
                        + ", accountType=" + accountType + ", ifsc=" + ifsc);
            }

            if (containerCard.getVisibility() == View.VISIBLE) {
                // FIX 2: Validate against rawCardNumber (always pure digits),
                // NOT etCardPayment.getText() which may be masked and include spaces
                if (rawCardNumber.length() != 16) {
                    etCardPayment.setError("Card number must be 16 digits");
                    return;
                }
            }

            dialog.dismiss();
            Toast.makeText(context, "Payment Processed!", Toast.LENGTH_LONG).show();

            if (paymentListener != null) {
                paymentListener.onTaxPaid(position);
            }
        });

        dialog.show();
    }

    @Override
    public int getItemCount() { return taxList.size(); }

    private static android.graphics.drawable.Drawable getBankItemBackground(Context context) {
        android.graphics.drawable.ColorDrawable white =
                new android.graphics.drawable.ColorDrawable(android.graphics.Color.WHITE);
        int[] attrs = new int[]{android.R.attr.selectableItemBackground};
        android.content.res.TypedArray ta = context.obtainStyledAttributes(attrs);
        android.graphics.drawable.Drawable ripple = ta.getDrawable(0);
        ta.recycle();
        if (ripple == null) return white;
        android.graphics.drawable.LayerDrawable layered =
                new android.graphics.drawable.LayerDrawable(new android.graphics.drawable.Drawable[]{white, ripple});
        return layered;
    }



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