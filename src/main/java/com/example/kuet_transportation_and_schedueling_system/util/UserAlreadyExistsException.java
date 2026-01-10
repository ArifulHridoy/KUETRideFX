package com.example.kuet_transportation_and_schedueling_system.util;

/**
 * Custom Exception for User Already Exists
 * Demonstrates: Exception Handling, Custom Exceptions
 */
public class UserAlreadyExistsException extends Exception {
    public UserAlreadyExistsException(String message) {
        super(message);
    }

    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}

