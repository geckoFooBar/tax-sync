package com.example.myapplication.fragments;

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
    private TextView tvCapGainsAmount, tvCryptoTaxAmount, tvPropertyTaxAmount, tvCessAmount;
    private PieChart taxChart;
    private MaterialButton btnAddAssets;

    // State Variables to store the user's explicit asset inputs from the bottom sheet
    private double userCapGainsProfit = 0;
    private double userCryptoProfit = 0;
    private double userPropertyTax = 0;

    // Helper Class for Tax Breakdown
    private class TaxBreakdown {
        double baseIncomeTax;
        double surcharge;
        double capitalGainsTax;
        double cryptoTax;
        double propertyTax;
        double professionalTax;
        double cess;
        double totalDeductions;

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

        // 2. Initial Setups
        setupPieChart();

        // 3. Listeners
        btnAddAssets.setOnClickListener(v -> showAssetBottomSheet());

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
    }

    // Unified method to recalculate everything whenever ANY data changes
    /*
    private void refreshDashboard() {
        String incomeStr = editIncome.getText().toString();
        if (!incomeStr.trim().isEmpty()) {
            try {
                double grossIncome = Double.parseDouble(incomeStr);
                TaxBreakdown breakdown = calculateComprehensiveTax(grossIncome);

                NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
                format.setMaximumFractionDigits(0);

                double totalLiability = breakdown.getTotalLiability();
                // Take home is Total Incomes (Salary + Cap Gains + Crypto) minus Total Taxes Owed
                double takeHome = (grossIncome + userCapGainsProfit + userCryptoProfit) - totalLiability;

                // Update the Top 4 Cards
                tvIncomeAmount.setText(format.format(grossIncome));
                tvTaxPayableAmount.setText(format.format(totalLiability));
                tvTaxPaidAmount.setText(format.format(0)); // Static pending a new feature
                tvSavingsAmount.setText(format.format(takeHome));

                // Update the Bottom 4 Details Cards
                tvCapGainsAmount.setText(format.format(breakdown.capitalGainsTax));
                tvCryptoTaxAmount.setText(format.format(breakdown.cryptoTax));
                tvPropertyTaxAmount.setText(format.format(breakdown.propertyTax));
                tvCessAmount.setText(format.format(breakdown.cess + breakdown.professionalTax));

                // Update Chart & Insights
                updateInsight(grossIncome);
                updateChartData(grossIncome, breakdown, takeHome);

            } catch (NumberFormatException e) {
                // Safely ignore invalid characters
            }
        } else {
            refreshDashboard();
        }
    }*/

    private void showAssetBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheetView = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_assets, null);
        dialog.setContentView(sheetView);

        EditText editCapGains = sheetView.findViewById(R.id.editCapGains);
        EditText editCrypto = sheetView.findViewById(R.id.editCrypto);
        EditText editProperty = sheetView.findViewById(R.id.editProperty);
        MaterialButton btnSave = sheetView.findViewById(R.id.btnSaveAssets);

        // Pre-fill existing values if they entered them before
        if (userCapGainsProfit > 0) editCapGains.setText(String.valueOf((int)userCapGainsProfit));
        if (userCryptoProfit > 0) editCrypto.setText(String.valueOf((int)userCryptoProfit));
        if (userPropertyTax > 0) editProperty.setText(String.valueOf((int)userPropertyTax));

        btnSave.setOnClickListener(v -> {
            userCapGainsProfit = parseInput(editCapGains.getText().toString());
            userCryptoProfit = parseInput(editCrypto.getText().toString());
            userPropertyTax = parseInput(editProperty.getText().toString());

            dialog.dismiss();
            refreshDashboard(); // Recalculate everything with new bottom-sheet inputs!
        });

        dialog.show();
    }

    private double parseInput(String input) {
        if (input == null || input.trim().isEmpty()) return 0;
        try { return Double.parseDouble(input); }
        catch (NumberFormatException e) { return 0; }
    }

    private TaxBreakdown calculateComprehensiveTax(double income) {
        TaxBreakdown tb = new TaxBreakdown();

        // 1. Deductions & Taxable Base
        tb.totalDeductions = Math.min(income * 0.15, 250000); // Standard/80C dummy logic
        double taxableIncome = Math.max(0, income - tb.totalDeductions);

        // 2. Base Income Tax
        if (taxableIncome <= 300000) tb.baseIncomeTax = 0;
        else if (taxableIncome <= 600000) tb.baseIncomeTax = (taxableIncome - 300000) * 0.05;
        else if (taxableIncome <= 900000) tb.baseIncomeTax = 15000 + (taxableIncome - 600000) * 0.10;
        else if (taxableIncome <= 1200000) tb.baseIncomeTax = 45000 + (taxableIncome - 900000) * 0.15;
        else if (taxableIncome <= 1500000) tb.baseIncomeTax = 90000 + (taxableIncome - 1200000) * 0.20;
        else tb.baseIncomeTax = 150000 + (taxableIncome - 1500000) * 0.30;

        // 3. Surcharge & State Taxes
        tb.surcharge = (income > 5000000) ? tb.baseIncomeTax * 0.10 : 0;
        tb.professionalTax = (income > 300000) ? 2500 : 0;

        // 4. Custom User Inputs applied to Tax Brackets
        tb.capitalGainsTax = userCapGainsProfit * 0.10; // 10% LTCG assumed
        tb.cryptoTax = userCryptoProfit * 0.30;         // 30% Flat Tax
        tb.propertyTax = userPropertyTax;               // Direct pass-through

        // 5. Cess (4% on Income Tax + Surcharge + CapGains Tax)
        tb.cess = (tb.baseIncomeTax + tb.surcharge + tb.capitalGainsTax) * 0.04;

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

        // If nothing is entered at all, show gray placeholder
        if (grossIncome <= 0 && userCapGainsProfit <= 0 && userCryptoProfit <= 0) {
            entries.add(new PieEntry(100f, "Awaiting Data"));
            colors.add(Color.parseColor("#E5E7EB"));
        } else {
            // Only add slices if the user actually owes that specific tax
            if (breakdown.baseIncomeTax > 0) {
                entries.add(new PieEntry((float) breakdown.baseIncomeTax, "Income Tax"));
                colors.add(Color.parseColor("#EF4444")); // Red
            }
            if (breakdown.surcharge > 0) {
                entries.add(new PieEntry((float) breakdown.surcharge, "Surcharge"));
                colors.add(Color.parseColor("#B91C1C")); // Darker Red
            }
            if (breakdown.capitalGainsTax > 0) {
                entries.add(new PieEntry((float) breakdown.capitalGainsTax, "Cap Gains Tax"));
                colors.add(Color.parseColor("#F59E0B")); // Amber
            }
            if (breakdown.cryptoTax > 0) {
                entries.add(new PieEntry((float) breakdown.cryptoTax, "Crypto Tax"));
                colors.add(Color.parseColor("#EC4899")); // Pink
            }
            if (breakdown.propertyTax > 0) {
                entries.add(new PieEntry((float) breakdown.propertyTax, "Property Tax"));
                colors.add(Color.parseColor("#8B5CF6")); // Purple
            }
            if (breakdown.cess > 0 || breakdown.professionalTax > 0) {
                entries.add(new PieEntry((float) (breakdown.cess + breakdown.professionalTax), "Cess & Prof. Tax"));
                colors.add(Color.parseColor("#6366F1")); // Indigo
            }

            // Add Take-Home Pay
            if (takeHome > 0) {
                entries.add(new PieEntry((float) takeHome, "Take-Home Pay"));
                colors.add(Color.parseColor("#3B82F6")); // Blue
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

    // Unified bulletproof method to recalculate everything
    private void refreshDashboard() {
        String incomeStr = editIncome.getText().toString();
        double grossIncome = 0; // Default to 0 if the box is empty

        // Safely try to read the number
        if (!incomeStr.trim().isEmpty()) {
            try {
                grossIncome = Double.parseDouble(incomeStr);
            } catch (NumberFormatException e) {
                // Safely catch if the user types weird characters like multiple decimals
                grossIncome = 0;
            }
        }

        // Calculate math (Even if grossIncome is 0, this creates a safe, non-null breakdown)
        TaxBreakdown breakdown = calculateComprehensiveTax(grossIncome);

        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        format.setMaximumFractionDigits(0);

        double totalLiability = breakdown.getTotalLiability();
        double takeHome = (grossIncome + userCapGainsProfit + userCryptoProfit) - totalLiability;

        // Safely update the Top 4 Cards (The 'if != null' prevents crashes if your XML is out of sync)
        if (tvIncomeAmount != null) tvIncomeAmount.setText(format.format(grossIncome));
        if (tvTaxPayableAmount != null) tvTaxPayableAmount.setText(format.format(totalLiability));
        if (tvTaxPaidAmount != null) tvTaxPaidAmount.setText(format.format(0));
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

    private void updateInsight(double income) {
        if (income == 0) tvTaxInsight.setText("Enter your income to calculate your exact tax liability and discover deductions.");
        else if (income < 700000) tvTaxInsight.setText("Great news! You may qualify for a full tax rebate under Section 87A.");
        else if (income < 1500000) tvTaxInsight.setText("Your portfolio is active. Consider increasing equity investments to leverage LTCG limits.");
        else tvTaxInsight.setText("High bracket detected. Be aware of the 30% flat tax on VDA/Crypto assets and municipal property levies.");
    }
}