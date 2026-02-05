package com.example.myapplication.model;

public class TaxItem {

    String taxName;
    String dueDate;
    String amount;

    public TaxItem(String taxName, String dueDate, String amount) {
        this.taxName = taxName;
        this.dueDate = dueDate;
        this.amount = amount;
    }

    public String getTaxName() {
        return taxName;
    }

    public String getDueDate() {
        return dueDate;
    }

    public String getAmount() {
        return amount;
    }
}
