package com.hashim.util;

import com.hashim.exception.ValidationException;

/**
 * Utility class for common validation operations.
 * Provides methods to validate and sanitize input fields.
 */
public class ValidationUtils {
    
    // Validation constants
    public static final int TITLE_MIN_LENGTH = 1;
    public static final int TITLE_MAX_LENGTH = 120;
    public static final int TAGS_MAX_LENGTH = 200;
    public static final int NOTES_MAX_LENGTH = 2000;
    
    /**
     * Validates and normalizes a title string.
     * - Trims whitespace
     * - Collapses multiple spaces to single space
     * - Validates length constraints (1-120 chars)
     * 
     * @param title The title to validate
     * @return Normalized title
     * @throws ValidationException if validation fails
     */
    public static String validateAndNormalizeTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new ValidationException("Title cannot be empty");
        }
        
        // Trim and collapse whitespace
        String normalized = title.trim().replaceAll("\\s+", " ");
        
        if (normalized.length() < TITLE_MIN_LENGTH) {
            throw new ValidationException("Title must be at least " + TITLE_MIN_LENGTH + " character");
        }
        
        if (normalized.length() > TITLE_MAX_LENGTH) {
            throw new ValidationException("Title cannot exceed " + TITLE_MAX_LENGTH + " characters (got " + normalized.length() + ")");
        }
        
        return normalized;
    }
    
    /**
     * Validates and normalizes tags string.
     * - Trims whitespace
     * - Collapses multiple spaces
     * - Validates max length (200 chars)
     * - Allows empty tags
     * 
     * @param tags The tags string (comma-separated)
     * @return Normalized tags string
     * @throws ValidationException if validation fails
     */
    public static String validateAndNormalizeTags(String tags) {
        if (tags == null) {
            return "";
        }
        
        // Trim and collapse whitespace
        String normalized = tags.trim().replaceAll("\\s+", " ");
        
        if (normalized.length() > TAGS_MAX_LENGTH) {
            throw new ValidationException("Tags cannot exceed " + TAGS_MAX_LENGTH + " characters (got " + normalized.length() + ")");
        }
        
        return normalized;
    }
    
    /**
     * Validates and normalizes notes string.
     * - Trims whitespace
     * - Validates max length
     * - Allows empty notes
     * 
     * @param notes The notes text
     * @return Normalized notes string
     * @throws ValidationException if validation fails
     */
    public static String validateAndNormalizeNotes(String notes) {
        if (notes == null) {
            return "";
        }
        
        String normalized = notes.trim();
        
        if (normalized.length() > NOTES_MAX_LENGTH) {
            throw new ValidationException("Notes cannot exceed " + NOTES_MAX_LENGTH + " characters (got " + normalized.length() + ")");
        }
        
        return normalized;
    }
    
    /**
     * Validates bookmark status value.
     * 
     * @param status The status string to validate
     * @throws ValidationException if status is invalid
     */
    public static void validateStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new ValidationException("Status cannot be empty");
        }
        
        String normalizedStatus = status.trim().toUpperCase();
        if (!normalizedStatus.equals("INBOX") && !normalizedStatus.equals("DONE")) {
            throw new ValidationException("Status must be INBOX or DONE, got: " + status);
        }
    }
    
    /**
     * Validates integer is positive.
     * 
     * @param value The value to check
     * @param fieldName The field name for error messages
     * @throws ValidationException if value is not positive
     */
    public static void validatePositive(Integer value, String fieldName) {
        if (value != null && value <= 0) {
            throw new ValidationException(fieldName + " must be positive, got: " + value);
        }
    }
    
    /**
     * Validates integer is non-negative.
     * 
     * @param value The value to check
     * @param fieldName The field name for error messages
     * @throws ValidationException if value is negative
     */
    public static void validateNonNegative(Integer value, String fieldName) {
        if (value != null && value < 0) {
            throw new ValidationException(fieldName + " must be non-negative, got: " + value);
        }
    }
}
