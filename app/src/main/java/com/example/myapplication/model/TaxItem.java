package com.example.myapplication.model;

public class TaxItem {
    private final String taxName;
    private final String dueDate;
    private final String displayAmount;
    private final double numericAmount;
    private boolean isPaid;

    public TaxItem(String taxName, String dueDate, String displayAmount, double numericAmount, boolean isPaid) {
        this.taxName = taxName;
        this.dueDate = dueDate;
        this.displayAmount = displayAmount;
        this.numericAmount = numericAmount;
        this.isPaid = isPaid;
    }

    public String getTaxName() { return taxName; }
    public String getDueDate() { return dueDate; }
    public String getDisplayAmount() { return displayAmount; }
    public double getNumericAmount() { return numericAmount; }
    public boolean isPaid() { return isPaid; }
    public void setPaid(boolean paid) { isPaid = paid; }

}