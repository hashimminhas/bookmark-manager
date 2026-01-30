package com.hashim.dto;

public class UpdateBookmarkRequest {
    private String title;
    private String url;
    private String description;
    private String status;

    public UpdateBookmarkRequest() {
    }

    public UpdateBookmarkRequest(String title, String url, String description, String status) {
        this.title = title;
        this.url = url;
        this.description = description;
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
