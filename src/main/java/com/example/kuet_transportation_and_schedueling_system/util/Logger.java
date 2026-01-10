package com.example.kuet_transportation_and_schedueling_system.util;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Logger Utility Class
 * Demonstrates: File I/O, Singleton Pattern
 */
public class Logger {
    private static Logger instance;
    private static final String LOG_FILE = "logs/system.log";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Logger() {
        // Create logs directory if it doesn't exist
        File logDir = new File("logs");
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
    }

    public static Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }

    /**
     * Log info message
     */
    public void info(String message) {
        log("INFO", message);
    }

    /**
     * Log error message
     */
    public void error(String message) {
        log("ERROR", message);
    }

    /**
     * Log warning message
     */
    public void warning(String message) {
        log("WARNING", message);
    }

    /**
     * Log admin action
     */
    public void adminAction(String username, String action) {
        log("ADMIN", username + " - " + action);
    }

    /**
     * Log student action
     */
    public void studentAction(String username, String action) {
        log("STUDENT", username + " - " + action);
    }

    /**
     * Write log to file
     */
    private void log(String level, String message) {
        String timestamp = LocalDateTime.now().format(DATE_FORMAT);
        String logEntry = String.format("[%s] [%s] %s%n", timestamp, level, message);

        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.print(logEntry);
            System.out.print(logEntry); // Also print to console
        } catch (IOException e) {
            System.err.println("Failed to write log: " + e.getMessage());
        }
    }

    /**
     * Read all logs
     */
    public String readLogs() {
        StringBuilder logs = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(LOG_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                logs.append(line).append("\n");
            }
        } catch (IOException e) {
            return "No logs available.";
        }
        return logs.toString();
    }

    /**
     * Clear logs
     */
    public void clearLogs() {
        try (PrintWriter writer = new PrintWriter(LOG_FILE)) {
            writer.print("");
            info("Logs cleared");
        } catch (FileNotFoundException e) {
            System.err.println("Failed to clear logs: " + e.getMessage());
        }
    }
}

