package com.harmonycare.app.util;

import java.util.regex.Pattern;

/**
 * Utility class for input validation
 */
public class ValidationHelper {
    // Phone number patterns (supports various formats)
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[0-9]{10,15}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s]{2,50}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    /**
     * Validate phone number
     * @param phone Phone number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        // Remove spaces, dashes, and parentheses for validation
        String cleaned = phone.replaceAll("[\\s\\-\\(\\)]", "");
        return PHONE_PATTERN.matcher(cleaned).matches();
    }
    
    /**
     * Format phone number for display
     * @param phone Raw phone number
     * @return Formatted phone number
     */
    public static String formatPhone(String phone) {
        if (phone == null) return "";
        return phone.replaceAll("[\\s\\-\\(\\)]", "");
    }
    
    /**
     * Validate name
     * @param name Name to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return NAME_PATTERN.matcher(name.trim()).matches();
    }
    
    /**
     * Validate email address
     * @param email Email to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }
    
    /**
     * Validate password
     * @param password Password to validate
     * @return Validation result with message
     */
    public static ValidationResult validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return new ValidationResult(false, "Password cannot be empty");
        }
        
        if (password.length() < 6) {
            return new ValidationResult(false, "Password must be at least 6 characters");
        }
        
        if (password.length() < 8) {
            return new ValidationResult(true, "Password is acceptable but consider using 8+ characters");
        }
        
        return new ValidationResult(true, "Password is valid");
    }
    
    /**
     * Validate contact number (phone or email)
     * @param contact Contact to validate
     * @return Validation result
     */
    public static ValidationResult validateContact(String contact) {
        if (contact == null || contact.trim().isEmpty()) {
            return new ValidationResult(false, "Contact cannot be empty");
        }
        
        String trimmed = contact.trim();
        
        // Check if it's an email
        if (trimmed.contains("@")) {
            if (isValidEmail(trimmed)) {
                return new ValidationResult(true, "Valid email");
            } else {
                return new ValidationResult(false, "Invalid email format");
            }
        } else {
            // Check if it's a phone number
            if (isValidPhone(trimmed)) {
                return new ValidationResult(true, "Valid phone number");
            } else {
                return new ValidationResult(false, "Invalid phone number format");
            }
        }
    }
    
    /**
     * Validation result class
     */
    public static class ValidationResult {
        private boolean valid;
        private String message;
        
        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getMessage() {
            return message;
        }
    }
}

