package com.hashim.util;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import com.hashim.exception.ValidationException;

public class UrlValidator {
    
    public static void validate(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new ValidationException("URL cannot be empty");
        }
        
        try {
            URL urlObj = new URL(url);
            urlObj.toURI(); // Additional validation
            
            String protocol = urlObj.getProtocol();
            if (!protocol.equals("http") && !protocol.equals("https")) {
                throw new ValidationException("URL must use HTTP or HTTPS protocol");
            }
            
        } catch (MalformedURLException e) {
            throw new ValidationException("Invalid URL format: " + e.getMessage());
        } catch (URISyntaxException e) {
            throw new ValidationException("Invalid URL syntax: " + e.getMessage());
        }
    }
    
    public static boolean isValid(String url) {
        try {
            validate(url);
            return true;
        } catch (ValidationException e) {
            return false;
        }
    }
}
