package com.example.myapplication.fragments;

import android.annotation.SuppressLint;
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
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private EditText editIncome;
    private TextView tvIncomeAmount, tvTaxPayableAmount, tvTaxPaidAmount, tvSavingsAmount, tvTaxInsight;
    private TextView tvCapGainsAmount, tvCryptoTaxAmount, tvPropertyTaxAmount, tvCessAmount;
    private PieChart taxChart;
    private double userCapGainsProfit = 0;
    private double userCryptoProfit = 0;
    private double userPropertyTax = 0;
    private static final String PREF_NAME = "TaxAppPrefs";

    private static class TaxBreakdown {
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

    @SuppressLint("SetTextI18n")
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
        MaterialButton btnAddAssets = view.findViewById(R.id.btnAddAssets);
        MaterialButton btnClearData = view.findViewById(R.id.btnClearData);

        TextView tvGreeting = view.findViewById(R.id.tvGreeting);

        SharedPreferences authPrefs = requireActivity().getSharedPreferences("auth", Context.MODE_PRIVATE);
        String fullName = authPrefs.getString("userName", "User");

        if (tvGreeting != null) {
            try {
                // Grab just the first word (First Name)
                assert fullName != null;
                String firstName = fullName.trim().split("\\s+")[0];
                tvGreeting.setText("Welcome 👋, " + firstName);
            } catch (Exception e) {
                tvGreeting.setText("Welcome 👋, User");
            }
        }

        setupPieChart();

        loadSavedData();

        btnAddAssets.setOnClickListener(v -> showAssetBottomSheet());

        btnClearData.setOnClickListener(v -> {
            userCapGainsProfit = 0;
            userCryptoProfit = 0;
            userPropertyTax = 0;

            editIncome.setText("");
            editIncome.clearFocus();

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

    private void loadSavedData() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        userCapGainsProfit = prefs.getFloat("capGains", 0f);
        userCryptoProfit = prefs.getFloat("cryptoProfit", 0f);
        userPropertyTax = prefs.getFloat("propertyTax", 0f);

        String savedIncome = prefs.getString("grossIncome", "");
        assert savedIncome != null;
        if (!savedIncome.isEmpty()) {
            editIncome.setText(savedIncome);
        }
        refreshDashboard();
    }

    private void refreshDashboard() {
        String incomeStr = editIncome.getText().toString();
        double grossIncome = 0;

        if (!incomeStr.trim().isEmpty()) {
            try {
                grossIncome = Double.parseDouble(incomeStr);
            } catch (NumberFormatException e) {
                grossIncome = 0;
            }
        }

        if (getActivity() != null) {
            SharedPreferences.Editor editor = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
            editor.putString("grossIncome", incomeStr);
            editor.putFloat("capGains", (float) userCapGainsProfit);
            editor.putFloat("cryptoProfit", (float) userCryptoProfit);
            editor.putFloat("propertyTax", (float) userPropertyTax);
            editor.apply();
        }

        TaxBreakdown breakdown = calculateComprehensiveTax(grossIncome);

        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        format.setMaximumFractionDigits(0);

        double totalLiability = breakdown.getTotalLiability();
        double takeHome = (grossIncome + userCapGainsProfit + userCryptoProfit) - totalLiability;

        SharedPreferences prefs = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        double totalCleared = prefs.getFloat("totalTaxesPaid", 0f);

        if (tvIncomeAmount != null) tvIncomeAmount.setText(format.format(grossIncome));
        if (tvTaxPayableAmount != null) tvTaxPayableAmount.setText(format.format(totalLiability));
        if (tvTaxPaidAmount != null) tvTaxPaidAmount.setText(format.format(totalCleared));
        if (tvSavingsAmount != null) tvSavingsAmount.setText(format.format(takeHome));

        if (tvCapGainsAmount != null) tvCapGainsAmount.setText(format.format(breakdown.capitalGainsTax));
        if (tvCryptoTaxAmount != null) tvCryptoTaxAmount.setText(format.format(breakdown.cryptoTax));
        if (tvPropertyTaxAmount != null) tvPropertyTaxAmount.setText(format.format(breakdown.propertyTax));
        if (tvCessAmount != null) tvCessAmount.setText(format.format(breakdown.cess + breakdown.professionalTax));

        if (tvTaxInsight != null) updateInsight(grossIncome);
        if (taxChart != null) updateChartData(grossIncome, breakdown);
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
            refreshDashboard();
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

    private double calculateBaseTaxOnly(double taxableIncome) {
        if (taxableIncome <= 400000) return 0;
        if (taxableIncome <= 800000) return (taxableIncome - 400000) * 0.05;
        if (taxableIncome <= 1200000) return 20000 + (taxableIncome - 800000) * 0.10;
        if (taxableIncome <= 1600000) return 60000 + (taxableIncome - 1200000) * 0.15;
        if (taxableIncome <= 2000000) return 120000 + (taxableIncome - 1600000) * 0.20;
        if (taxableIncome <= 2400000) return 200000 + (taxableIncome - 2000000) * 0.25;
        return 300000 + (taxableIncome - 2400000) * 0.30;
    }

    private TaxBreakdown calculateComprehensiveTax(double income) {
        TaxBreakdown tb = new TaxBreakdown();

        tb.totalDeductions = Math.min(income, 75000);
        double taxableIncome = Math.max(0, income - tb.totalDeductions);

        tb.baseIncomeTax = calculateBaseTaxOnly(taxableIncome);

        if (taxableIncome <= 1200000) {
            tb.baseIncomeTax = 0;
        } else {
            double excessIncome = taxableIncome - 1200000;
            if (tb.baseIncomeTax > excessIncome) {
                tb.baseIncomeTax = excessIncome;
            }
        }

        tb.capitalGainsTax = userCapGainsProfit * 0.125;
        tb.cryptoTax = userCryptoProfit * 0.30;
        tb.propertyTax = userPropertyTax;

        // 5. Tiered Surcharge & Surcharge Marginal Relief
        double totalIncome = taxableIncome + userCapGainsProfit + userCryptoProfit;

        if (totalIncome > 5000000) {
            double surchargeRate;
            double threshold;

            if (totalIncome > 20000000) {
                surchargeRate = 0.25;
                threshold = 20000000;
            } else if (totalIncome > 10000000) {
                surchargeRate = 0.15;
                threshold = 10000000;
            } else {
                surchargeRate = 0.10;
                threshold = 5000000;
            }

            double calculatedSurcharge = tb.baseIncomeTax * surchargeRate;
            double totalTaxWithSurcharge = tb.baseIncomeTax + calculatedSurcharge;

            double taxAtThreshold = calculateBaseTaxOnly(threshold - 75000);
            double maxFairTax = taxAtThreshold + (totalIncome - threshold);

            if (totalTaxWithSurcharge > maxFairTax) {
                calculatedSurcharge = maxFairTax - tb.baseIncomeTax;
            }

            tb.surcharge = Math.max(0, calculatedSurcharge);
        } else {
            tb.surcharge = 0;
        }

        tb.professionalTax = 0;

        tb.cess = (tb.baseIncomeTax + tb.surcharge + tb.capitalGainsTax + tb.cryptoTax) * 0.04;

        return tb;
    }

    private void setupPieChart() {
        SharedPreferences modePrefs = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean isDarkMode = modePrefs.getBoolean("isDarkMode", false);

        String color = !isDarkMode ? "#111827" : "#E8EAF0";
        String legendColor= !isDarkMode ? "#111827" : "#E8EAF0";

        taxChart.setUsePercentValues(false);
        taxChart.getDescription().setEnabled(false);
        taxChart.setExtraOffsets(20f, 0f, 20f, 0f);
        taxChart.setDrawHoleEnabled(true);
        taxChart.setHoleRadius(52f);
        taxChart.setTransparentCircleRadius(57f);
        taxChart.setHoleColor(Color.TRANSPARENT);
        taxChart.setRotationEnabled(false);
        taxChart.setHighlightPerTapEnabled(true);
        taxChart.setDrawEntryLabels(false);
        taxChart.setCenterText("Tax\nBreakdown");
        taxChart.setCenterTextSize(14f);
        taxChart.setCenterTextColor(Color.parseColor(color));

        Legend l = taxChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setWordWrapEnabled(true);
        l.setXEntrySpace(12f);
        l.setYEntrySpace(6f);
        l.setYOffset(10f);
        l.setTextSize(11f);
        l.setTextColor(Color.parseColor(legendColor));

        updateChartData(0, null);
    }

    private void updateChartData(double grossIncome, TaxBreakdown breakdown) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        boolean hasNoData = grossIncome <= 0 && userCapGainsProfit <= 0 && userCryptoProfit <= 0;

        if (hasNoData || breakdown == null) {
            entries.add(new PieEntry(100f, "No Data"));
            colors.add(Color.parseColor("#E5E7EB"));
            taxChart.setCenterText("Tax\nBreakdown");
        } else {
            double totalTax = breakdown.getTotalLiability();

            if (totalTax <= 0) {

                entries.add(new PieEntry(100f, "Zero Tax 🎉"));
                colors.add(Color.parseColor("#22C55E"));
                taxChart.setCenterText("₹0\nTax Due");
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
                    entries.add(new PieEntry((float) breakdown.capitalGainsTax, "Cap Gains"));
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
                if (breakdown.cess + breakdown.professionalTax > 0) {
                    entries.add(new PieEntry((float) (breakdown.cess + breakdown.professionalTax), "Cess & P.Tax"));
                    colors.add(Color.parseColor("#6366F1"));
                }

                NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
                fmt.setMaximumFractionDigits(0);
                taxChart.setCenterText("Total Tax\n" + fmt.format(totalTax));
            }
        }

        // Read dark mode preference so value labels and lines are always visible
        SharedPreferences modePrefs = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean isDarkMode = modePrefs.getBoolean("isDarkMode", false);
        int valueTextColor = isDarkMode
                ? Color.parseColor("#F0F2F8")   // bright white-blue for dark bg
                : Color.parseColor("#111827");   // near-black for light bg
        int valueLineColor = isDarkMode
                ? Color.parseColor("#9CA6BA")    // muted blue-grey, visible on dark
                : Color.parseColor("#6B7280");   // grey for light

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(8f);
        dataSet.setColors(colors);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setValueLinePart1OffsetPercentage(80f);
        dataSet.setValueLinePart1Length(0.4f);
        dataSet.setValueLinePart2Length(0.2f);
        dataSet.setValueLineColor(valueLineColor);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(12f);
        data.setValueTextColor(valueTextColor);
        data.setValueFormatter(new PercentFormatter(taxChart));

        if (hasNoData || breakdown == null) {
            data.setDrawValues(false);
        }

        taxChart.setData(data);
        taxChart.invalidate();
    }

    private void updateInsight(double income) {
        if (income == 0) tvTaxInsight.setText("Enter your income to calculate your exact tax liability and discover deductions.");
        else if (income <= 1275000) tvTaxInsight.setText("Great news! You qualify for a full tax rebate under Section 87A. Your base tax is zero!");
        else if (income < 1500000) tvTaxInsight.setText("Your portfolio is active. Consider increasing equity investments to leverage LTCG limits.");
        else tvTaxInsight.setText("High bracket detected. Be aware of the 30% flat tax on VDA/Crypto assets and municipal property levies.");
    }
}