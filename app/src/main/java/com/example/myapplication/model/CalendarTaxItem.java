package com.example.myapplication.model;

public class CalendarTaxItem {

    private String taxName;
    private String dueDate;
    private String status; // Upcoming / Overdue

    public CalendarTaxItem(String taxName, String dueDate, String status) {
        this.taxName = taxName;
        this.dueDate = dueDate;
        this.status = status;
    }

    public String getTaxName() {
        return taxName;
    }

    public String getDueDate() {
        return dueDate;
    }

    public String getStatus() {
        return status;
    }
}
