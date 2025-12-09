package com.harmonycare.app.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for password hashing and validation
 */
public class PasswordHelper {
    private static final String ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 16;
    
    /**
     * Hash a password with SHA-256 and salt
     * @param password Plain text password
     * @return Hashed password with salt (format: salt:hash)
     */
    public static String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            return null;
        }
        
        try {
            // Generate random salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);
            
            // Hash password with salt
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes());
            
            // Combine salt and hash
            String saltBase64 = Base64.getEncoder().encodeToString(salt);
            String hashBase64 = Base64.getEncoder().encodeToString(hashedPassword);
            
            return saltBase64 + ":" + hashBase64;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Verify a password against a stored hash
     * @param password Plain text password to verify
     * @param storedHash Stored hash (format: salt:hash)
     * @return true if password matches, false otherwise
     */
    public static boolean verifyPassword(String password, String storedHash) {
        if (password == null || storedHash == null || storedHash.isEmpty()) {
            return false;
        }
        
        try {
            // Split salt and hash
            String[] parts = storedHash.split(":");
            if (parts.length != 2) {
                // Legacy plain text password support (for migration)
                return password.equals(storedHash);
            }
            
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] storedHashBytes = Base64.getDecoder().decode(parts[1]);
            
            // Hash the provided password with the stored salt
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes());
            
            // Compare hashes
            return MessageDigest.isEqual(hashedPassword, storedHashBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Check password strength
     * @param password Password to check
     * @return Strength level: 0=weak, 1=medium, 2=strong
     */
    public static int getPasswordStrength(String password) {
        if (password == null || password.length() < 6) {
            return 0; // Weak
        }
        
        int strength = 0;
        
        // Length check
        if (password.length() >= 8) strength++;
        if (password.length() >= 12) strength++;
        
        // Character variety checks
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*[0-9].*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");
        
        int varietyCount = 0;
        if (hasUpper) varietyCount++;
        if (hasLower) varietyCount++;
        if (hasDigit) varietyCount++;
        if (hasSpecial) varietyCount++;
        
        if (varietyCount >= 3) strength++;
        if (varietyCount >= 4) strength++;
        
        return Math.min(strength, 2); // Max strength is 2
    }
    
    /**
     * Get password strength description
     * @param password Password to check
     * @return "Weak", "Medium", or "Strong"
     */
    public static String getPasswordStrengthText(String password) {
        int strength = getPasswordStrength(password);
        switch (strength) {
            case 0:
                return "Weak";
            case 1:
                return "Medium";
            case 2:
                return "Strong";
            default:
                return "Weak";
        }
    }
}

