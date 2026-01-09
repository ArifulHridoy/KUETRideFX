package com.example.kuet_transportation_and_schedueling_system.test;

import com.example.kuet_transportation_and_schedueling_system.dao.UserDAO;
import com.example.kuet_transportation_and_schedueling_system.model.User;

public class TestLogin {
    public static void main(String[] args) {
        System.out.println("================================================");
        System.out.println("Testing Login for ariful51");
        System.out.println("================================================");
        System.out.println();

        UserDAO userDAO = new UserDAO();

        // Test 1: Admin login
        System.out.println("Test 1: Admin Login (admin/admin123)");
        try {
            User admin = userDAO.login("admin", "admin123");
            System.out.println("✓ Admin login successful!");
            System.out.println("  User ID: " + admin.getId());
            System.out.println("  Username: " + admin.getUsername());
            System.out.println("  Role: " + admin.getRole());
        } catch (Exception e) {
            System.out.println("✗ Admin login failed: " + e.getMessage());
        }
        System.out.println();

        // Test 2: Student login (ariful51)
        System.out.println("Test 2: Student Login (ariful51/ariful2023)");
        try {
            User student = userDAO.login("ariful51", "ariful2023");
            System.out.println("✓ Student login successful!");
            System.out.println("  User ID: " + student.getId());
            System.out.println("  Username: " + student.getUsername());
            System.out.println("  Role: " + student.getRole());
        } catch (Exception e) {
            System.out.println("✗ Student login failed: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();

        System.out.println("================================================");
        System.out.println("Test Complete");
        System.out.println("================================================");
    }
}

