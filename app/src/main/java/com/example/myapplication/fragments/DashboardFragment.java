package com.example.myapplication.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    // UI Elements
    private EditText editIncome;
    private TextView tvIncomeAmount, tvTaxPayableAmount, tvTaxPaidAmount, tvSavingsAmount, tvTaxInsight;

    private TextView tvGreeting;
    private TextView tvCapGainsAmount, tvCryptoTaxAmount, tvPropertyTaxAmount, tvCessAmount;
    private PieChart taxChart;
    private MaterialButton btnAddAssets, btnClearData;

    private double userCapGainsProfit = 0;
    private double userCryptoProfit = 0;
    private double userPropertyTax = 0;

    // SharedPreferences name
    private static final String PREF_NAME = "TaxAppPrefs";

    // Helper Class for Tax Breakdown
    private class TaxBreakdown {
        double baseIncomeTax, surcharge, capitalGainsTax, cryptoTax, propertyTax, professionalTax, cess, totalDeductions;

        double getTotalLiability() {
            return baseIncomeTax + surcharge + capitalGainsTax + cryptoTax + propertyTax + professionalTax + cess;
        }
    }

    public DashboardFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Bind UI Elements
        editIncome = view.findViewById(R.id.editIncome);
        tvIncomeAmount = view.findViewById(R.id.tvIncomeAmount);
        tvTaxPayableAmount = view.findViewById(R.id.tvTaxPayableAmount);
        tvTaxPaidAmount = view.findViewById(R.id.tvTaxPaidAmount);
        tvSavingsAmount = view.findViewById(R.id.tvSavingsAmount);
        tvTaxInsight = view.findViewById(R.id.tvTaxInsight);

        tvCapGainsAmount = view.findViewById(R.id.tvCapGainsAmount);
        tvCryptoTaxAmount = view.findViewById(R.id.tvCryptoTaxAmount);
        tvPropertyTaxAmount = view.findViewById(R.id.tvPropertyTaxAmount);
        tvCessAmount = view.findViewById(R.id.tvCessAmount);

        taxChart = view.findViewById(R.id.taxChart);
        btnAddAssets = view.findViewById(R.id.btnAddAssets);
        btnClearData = view.findViewById(R.id.btnClearData);

        tvGreeting = view.findViewById(R.id.tvGreeting);

        SharedPreferences authPrefs = requireActivity().getSharedPreferences("auth", Context.MODE_PRIVATE);
        String fullName = authPrefs.getString("userName", "User");

        if (tvGreeting != null) {
            try {
                // Grab just the first word (First Name)
                String firstName = fullName.trim().split("\\s+")[0];
                tvGreeting.setText("Welcome 👋, " + firstName);
            } catch (Exception e) {
                tvGreeting.setText("Welcome 👋, User");
            }
        }

        setupPieChart();

        // 2. Load Saved Data from previous sessions/tabs
        loadSavedData();

        // 3. Button Listeners
        btnAddAssets.setOnClickListener(v -> showAssetBottomSheet());

        btnClearData.setOnClickListener(v -> {
            // Wipe variables
            userCapGainsProfit = 0;
            userCryptoProfit = 0;
            userPropertyTax = 0;

            // Clear the main text box
            editIncome.setText("");
            editIncome.clearFocus();

            // Wipe from phone storage too
            SharedPreferences prefs = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            prefs.edit().clear().apply();
        });

        editIncome.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                refreshDashboard();
            }
        });

        loadSavedData();
    }

    // Loads the data the moment the fragment is created
    private void loadSavedData() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        userCapGainsProfit = prefs.getFloat("capGains", 0f);
        userCryptoProfit = prefs.getFloat("cryptoProfit", 0f);
        userPropertyTax = prefs.getFloat("propertyTax", 0f);

        String savedIncome = prefs.getString("grossIncome", "");
        if (!savedIncome.isEmpty()) {
            editIncome.setText(savedIncome); // This automatically triggers refreshDashboard()
        }
        refreshDashboard();
    }

    // Unified bulletproof method to recalculate AND auto-save everything
    private void refreshDashboard() {
        String incomeStr = editIncome.getText().toString();
        double grossIncome = 0;

        // Safely try to read the number
        if (!incomeStr.trim().isEmpty()) {
            try {
                grossIncome = Double.parseDouble(incomeStr);
            } catch (NumberFormatException e) {
                grossIncome = 0;
            }
        }

        // AUTO-SAVE to phone storage instantly
        if (getActivity() != null) {
            SharedPreferences.Editor editor = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
            editor.putString("grossIncome", incomeStr);
            editor.putFloat("capGains", (float) userCapGainsProfit);
            editor.putFloat("cryptoProfit", (float) userCryptoProfit);
            editor.putFloat("propertyTax", (float) userPropertyTax);
            editor.apply();
        }

        // Calculate math
        TaxBreakdown breakdown = calculateComprehensiveTax(grossIncome);

        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        format.setMaximumFractionDigits(0);

        double totalLiability = breakdown.getTotalLiability();
        double takeHome = (grossIncome + userCapGainsProfit + userCryptoProfit) - totalLiability;

        SharedPreferences prefs = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        double totalCleared = prefs.getFloat("totalTaxesPaid", 0f);
        // Safely update the Top 4 Cards
        if (tvIncomeAmount != null) tvIncomeAmount.setText(format.format(grossIncome));
        if (tvTaxPayableAmount != null) tvTaxPayableAmount.setText(format.format(totalLiability));
        if (tvTaxPaidAmount != null) tvTaxPaidAmount.setText(format.format(totalCleared));
        if (tvSavingsAmount != null) tvSavingsAmount.setText(format.format(takeHome));

        // Safely update the Bottom 4 Details Cards
        if (tvCapGainsAmount != null) tvCapGainsAmount.setText(format.format(breakdown.capitalGainsTax));
        if (tvCryptoTaxAmount != null) tvCryptoTaxAmount.setText(format.format(breakdown.cryptoTax));
        if (tvPropertyTaxAmount != null) tvPropertyTaxAmount.setText(format.format(breakdown.propertyTax));
        if (tvCessAmount != null) tvCessAmount.setText(format.format(breakdown.cess + breakdown.professionalTax));

        // Safely update Chart & Insights
        if (tvTaxInsight != null) updateInsight(grossIncome);
        if (taxChart != null) updateChartData(grossIncome, breakdown, takeHome);
    }

    private void showAssetBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheetView = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_assets, null);
        dialog.setContentView(sheetView);

        EditText editCapGains = sheetView.findViewById(R.id.editCapGains);
        EditText editCrypto = sheetView.findViewById(R.id.editCrypto);
        EditText editProperty = sheetView.findViewById(R.id.editProperty);
        MaterialButton btnSave = sheetView.findViewById(R.id.btnSaveAssets);

        editCapGains.setText(formatForInput(userCapGainsProfit));
        editCrypto.setText(formatForInput(userCryptoProfit));
        editProperty.setText(formatForInput(userPropertyTax));

        btnSave.setOnClickListener(v -> {
            userCapGainsProfit = parseInput(editCapGains.getText().toString());
            userCryptoProfit = parseInput(editCrypto.getText().toString());
            userPropertyTax = parseInput(editProperty.getText().toString());

            dialog.dismiss();
            refreshDashboard(); // This will recalculate AND auto-save to storage
        });

        dialog.show();
    }

    private String formatForInput(double value) {
        if (value == 0) return "";
        if (value == (long) value) {
            return String.format(Locale.US, "%d", (long) value);
        }
        return String.valueOf(value);
    }

    private double parseInput(String input) {
        if (input == null || input.trim().isEmpty()) return 0;
        try { return Double.parseDouble(input); }
        catch (NumberFormatException e) { return 0; }
    }

    private TaxBreakdown calculateComprehensiveTax(double income) {
        TaxBreakdown tb = new TaxBreakdown();

        // 1. Standard Deduction (₹75,000)
        tb.totalDeductions = Math.min(income, 75000);
        double taxableIncome = Math.max(0, income - tb.totalDeductions);

        // 2. Base Income Tax (Strictly applying the New Regime Slabs you provided)
        if (taxableIncome <= 400000) {
            tb.baseIncomeTax = 0;
        } else if (taxableIncome <= 800000) {
            tb.baseIncomeTax = (taxableIncome - 400000) * 0.05;
        } else if (taxableIncome <= 1200000) {
            tb.baseIncomeTax = 20000 + (taxableIncome - 800000) * 0.10;
        } else if (taxableIncome <= 1600000) {
            tb.baseIncomeTax = 60000 + (taxableIncome - 1200000) * 0.15;
        } else if (taxableIncome <= 2000000) {
            tb.baseIncomeTax = 120000 + (taxableIncome - 1600000) * 0.20;
        } else if (taxableIncome <= 2400000) {
            tb.baseIncomeTax = 200000 + (taxableIncome - 2000000) * 0.25;
        } else {
            tb.baseIncomeTax = 300000 + (taxableIncome - 2400000) * 0.30;
        }

        // NOTE: Section 87A Rebate logic has been removed so the user sees the true slab calculation.

        // 3. Custom User Inputs
        tb.capitalGainsTax = userCapGainsProfit * 0.125;
        tb.cryptoTax = userCryptoProfit * 0.30;
        tb.propertyTax = userPropertyTax;

        // 4. Tiered Surcharge
        double totalIncome = taxableIncome + userCapGainsProfit + userCryptoProfit;
        if (totalIncome > 20000000) tb.surcharge = tb.baseIncomeTax * 0.25;
        else if (totalIncome > 10000000) tb.surcharge = tb.baseIncomeTax * 0.15;
        else if (totalIncome > 5000000) tb.surcharge = tb.baseIncomeTax * 0.10;
        else tb.surcharge = 0;

        // 5. Professional Tax Disabled
        tb.professionalTax = 0;

        // 6. Health & Education Cess (4%)
        tb.cess = (tb.baseIncomeTax + tb.surcharge + tb.capitalGainsTax + tb.cryptoTax) * 0.04;

        return tb;
    }

    private void setupPieChart() {
        taxChart.setUsePercentValues(true);
        taxChart.getDescription().setEnabled(false);
        taxChart.setExtraOffsets(5, 10, 5, 5);
        taxChart.setDrawHoleEnabled(true);
        taxChart.setHoleColor(Color.WHITE);
        taxChart.setTransparentCircleColor(Color.WHITE);
        taxChart.setTransparentCircleAlpha(110);
        taxChart.setHoleRadius(65f);
        taxChart.setTransparentCircleRadius(70f);
        taxChart.setDrawCenterText(true);
        taxChart.setCenterText("Overview");
        taxChart.setCenterTextSize(16f);
        taxChart.setCenterTextColor(Color.parseColor("#111827"));
        taxChart.setRotationEnabled(false);
        taxChart.setHighlightPerTapEnabled(true);

        Legend l = taxChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setWordWrapEnabled(true);
        l.setXEntrySpace(12f);
        l.setYEntrySpace(6f);
        l.setYOffset(10f);
        l.setTextSize(10f);
        l.setTextColor(Color.parseColor("#6B7280"));

        updateChartData(0, null, 0);
    }

    private void updateChartData(double grossIncome, TaxBreakdown breakdown, double takeHome) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        if (grossIncome <= 0 && userCapGainsProfit <= 0 && userCryptoProfit <= 0) {
            entries.add(new PieEntry(100f, "Awaiting Data"));
            colors.add(Color.parseColor("#E5E7EB"));
        } else {
            if (breakdown.baseIncomeTax > 0) {
                entries.add(new PieEntry((float) breakdown.baseIncomeTax, "Income Tax"));
                colors.add(Color.parseColor("#EF4444"));
            }
            if (breakdown.surcharge > 0) {
                entries.add(new PieEntry((float) breakdown.surcharge, "Surcharge"));
                colors.add(Color.parseColor("#B91C1C"));
            }
            if (breakdown.capitalGainsTax > 0) {
                entries.add(new PieEntry((float) breakdown.capitalGainsTax, "Cap Gains Tax"));
                colors.add(Color.parseColor("#F59E0B"));
            }
            if (breakdown.cryptoTax > 0) {
                entries.add(new PieEntry((float) breakdown.cryptoTax, "Crypto Tax"));
                colors.add(Color.parseColor("#EC4899"));
            }
            if (breakdown.propertyTax > 0) {
                entries.add(new PieEntry((float) breakdown.propertyTax, "Property Tax"));
                colors.add(Color.parseColor("#8B5CF6"));
            }
            if (breakdown.cess > 0 || breakdown.professionalTax > 0) {
                entries.add(new PieEntry((float) (breakdown.cess + breakdown.professionalTax), "Cess & Prof. Tax"));
                colors.add(Color.parseColor("#6366F1"));
            }
            if (takeHome > 0) {
                entries.add(new PieEntry((float) takeHome, "Take-Home Pay"));
                colors.add(Color.parseColor("#3B82F6"));
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(2f);
        dataSet.setSelectionShift(5f);
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(9f);
        data.setValueTextColor(Color.WHITE);

        if (grossIncome == 0 && userCapGainsProfit == 0 && userCryptoProfit == 0) {
            data.setDrawValues(false);
        }

        taxChart.setData(data);
        taxChart.invalidate();
    }

    private void updateInsight(double income) {
        if (income == 0) tvTaxInsight.setText("Enter your income to calculate your exact tax liability and discover deductions.");
        else if (income < 700000) tvTaxInsight.setText("Great news! You may qualify for a full tax rebate under Section 87A.");
        else if (income < 1500000) tvTaxInsight.setText("Your portfolio is active. Consider increasing equity investments to leverage LTCG limits.");
        else tvTaxInsight.setText("High bracket detected. Be aware of the 30% flat tax on VDA/Crypto assets and municipal property levies.");
    }
}