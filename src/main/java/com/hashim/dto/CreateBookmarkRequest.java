package com.hashim.dto;

public class CreateBookmarkRequest {
    private String url;
    private String title;
    private String tags;
    private String notes;

    public CreateBookmarkRequest() {
    }

    public CreateBookmarkRequest(String url, String title, String tags, String notes) {
        this.url = url;
        this.title = title;
        this.tags = tags;
        this.notes = notes;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
