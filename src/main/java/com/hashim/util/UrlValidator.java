package com.hashim.util;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import com.hashim.exception.ValidationException;

public class UrlValidator {
    
    /**
     * Normalizes and validates a URL.
     * - Trims whitespace
     * - Adds https:// prefix if no scheme is present
     * - Validates URL format
     * - Ensures http or https protocol only
     * 
     * @param url The URL to normalize
     * @return Normalized URL string
     * @throws ValidationException if URL is invalid
     */
    public static String normalizeAndValidate(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new ValidationException("URL cannot be empty");
        }
        
        // Trim and collapse whitespace
        String normalized = url.trim().replaceAll("\\s+", " ");
        
        // Add https:// if no protocol is present
        if (!normalized.matches("^[a-zA-Z][a-zA-Z0-9+.-]*://.*")) {
            // Remove any leading slashes before adding protocol
            normalized = normalized.replaceAll("^/+", "");
            normalized = "https://" + normalized;
        }
        
        // Validate the normalized URL
        try {
            URL urlObj = new URL(normalized);
            urlObj.toURI(); // Additional validation for URI compliance
            
            String protocol = urlObj.getProtocol().toLowerCase();
            if (!protocol.equals("http") && !protocol.equals("https")) {
                throw new ValidationException("URL must use HTTP or HTTPS protocol, got: " + protocol);
            }
            
            // Ensure host is present
            if (urlObj.getHost() == null || urlObj.getHost().isEmpty()) {
                throw new ValidationException("URL must contain a valid host");
            }
            
            return normalized;
            
        } catch (MalformedURLException e) {
            throw new ValidationException("Invalid URL format: " + e.getMessage());
        } catch (URISyntaxException e) {
            throw new ValidationException("Invalid URL syntax: " + e.getMessage());
        }
    }
    
    /**
     * Validates URL without normalization (for backward compatibility)
     */
    public static void validate(String url) {
        normalizeAndValidate(url); // Just validate, don't return normalized version
    }
    
    public static boolean isValid(String url) {
        try {
            normalizeAndValidate(url);
            return true;
        } catch (ValidationException e) {
            return false;
        }
    }
}
