package com.hashim.dto;

import java.util.HashMap;
import java.util.Map;

public class ErrorResponse {
    private ErrorDetail error;

    public ErrorResponse(String code, String message) {
        this.error = new ErrorDetail(code, message, new HashMap<>());
    }

    public ErrorResponse(String code, String message, Map<String, String> details) {
        this.error = new ErrorDetail(code, message, details);
    }

    public ErrorDetail getError() {
        return error;
    }

    public void setError(ErrorDetail error) {
        this.error = error;
    }

    public static class ErrorDetail {
        private String code;
        private String message;
        private Map<String, String> details;

        public ErrorDetail(String code, String message, Map<String, String> details) {
            this.code = code;
            this.message = message;
            this.details = details;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Map<String, String> getDetails() {
            return details;
        }

        public void setDetails(Map<String, String> details) {
            this.details = details;
        }
    }
}
