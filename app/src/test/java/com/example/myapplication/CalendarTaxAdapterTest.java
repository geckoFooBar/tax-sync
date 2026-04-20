package com.example.myapplication;

import org.junit.Test;
import static org.junit.Assert.*;

public class CalendarTaxAdapterTest {

    // Replicating the logic from your adapter
    private String getMonthText(String dueDate) {
        String[] months = {"JAN","FEB","MAR","APR","MAY","JUN",
                "JUL","AUG","SEP","OCT","NOV","DEC"};
        String[] dateParts = dueDate.split("/");
        int monthIndex = Integer.parseInt(dateParts[1]) - 1;
        return months[monthIndex];
    }

    private String getDayText(String dueDate) {
        return dueDate.split("/")[0];
    }

    @Test
    public void testMonthConversion_june() {
        assertEquals("JUN", getMonthText("15/06/2026"));
    }

    @Test
    public void testMonthConversion_january() {
        assertEquals("JAN", getMonthText("01/01/2026"));
    }

    @Test
    public void testMonthConversion_december() {
        assertEquals("DEC", getMonthText("31/12/2026"));
    }

    @Test
    public void testDayExtraction() {
        assertEquals("15", getDayText("15/06/2026"));
    }

    @Test
    public void testDayExtraction_singleDigitDay() {
        assertEquals("01", getDayText("01/04/2026"));
    }
}