package com.example.myapplication;

import org.junit.Test;
import static org.junit.Assert.*;

public class ProfileInitialsTest {

    // Replicating the logic from your ProfileFragment
    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "U";
        String[] nameParts = name.trim().split("\\s+");
        if (nameParts.length > 1) {
            return (String.valueOf(nameParts[0].charAt(0))
                    + String.valueOf(nameParts[1].charAt(0))).toUpperCase();
        } else if (!nameParts[0].isEmpty()) {
            return String.valueOf(nameParts[0].charAt(0)).toUpperCase();
        }
        return "U";
    }

    @Test
    public void testFullName_returnsTwoInitials() {
        assertEquals("JD", getInitials("John Doe"));
    }

    @Test
    public void testSingleName_returnsOneInitial() {
        assertEquals("J", getInitials("John"));
    }

    @Test
    public void testNullName_returnsU() {
        assertEquals("U", getInitials(null));
    }

    @Test
    public void testEmptyName_returnsU() {
        assertEquals("U", getInitials("   "));
    }

    @Test
    public void testLowerCaseName_returnsUpperCaseInitials() {
        assertEquals("JD", getInitials("john doe"));
    }

    @Test
    public void testExtraSpaces_handledCorrectly() {
        assertEquals("JD", getInitials("John   Doe"));
    }
}