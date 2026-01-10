package com.example.kuet_transportation_and_schedueling_system.util;

/**
 * Custom Exception for Invalid Login
 * Demonstrates: Exception Handling, Custom Exceptions
 */
public class InvalidLoginException extends Exception {
    public InvalidLoginException(String message) {
        super(message);
    }

    public InvalidLoginException(String message, Throwable cause) {
        super(message, cause);
    }
}

