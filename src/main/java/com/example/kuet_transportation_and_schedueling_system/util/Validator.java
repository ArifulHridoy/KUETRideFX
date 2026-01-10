package com.example.kuet_transportation_and_schedueling_system.util;

import java.util.regex.Pattern;

/**
 * Validator Utility Class
 * Demonstrates: Static Methods, Regular Expressions
 */
public class Validator {

    // Username: 3-20 characters, alphanumeric and underscore only
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");

    // Password: minimum 6 characters
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^.{6,}$");

    // Student ID: Format like 1801001, 1902045, etc.
    private static final Pattern STUDENT_ID_PATTERN = Pattern.compile("^[0-9]{7,10}$");

    // Bus Number: Format like KA-2023, KUET-01, etc.
    private static final Pattern BUS_NUMBER_PATTERN = Pattern.compile("^[A-Z0-9-]{2,15}$");

    /**
     * Validate username
     */
    public static boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }

    /**
     * Validate password
     */
    public static boolean isValidPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Validate student ID
     */
    public static boolean isValidStudentId(String studentId) {
        return studentId != null && STUDENT_ID_PATTERN.matcher(studentId).matches();
    }

    /**
     * Validate bus number
     */
    public static boolean isValidBusNumber(String busNumber) {
        return busNumber != null && BUS_NUMBER_PATTERN.matcher(busNumber).matches();
    }

    /**
     * Validate capacity (should be positive)
     */
    public static boolean isValidCapacity(int capacity) {
        return capacity > 0 && capacity <= 100;
    }

    /**
     * Validate if string is not empty
     */
    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * Get username validation message
     */
    public static String getUsernameValidationMessage() {
        return "Username must be 3-20 characters (letters, numbers, underscore only)";
    }

    /**
     * Get password validation message
     */
    public static String getPasswordValidationMessage() {
        return "Password must be at least 6 characters";
    }

    /**
     * Get student ID validation message
     */
    public static String getStudentIdValidationMessage() {
        return "Student ID must be 7-10 digits";
    }

    /**
     * Get bus number validation message
     */
    public static String getBusNumberValidationMessage() {
        return "Bus number must be 2-15 characters (uppercase letters, numbers, hyphens)";
    }
}

