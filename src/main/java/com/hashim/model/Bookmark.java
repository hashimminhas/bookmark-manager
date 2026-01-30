package com.hashim.model;

import java.time.LocalDateTime;

public class Bookmark {
    private Long id;
    private String url;
    private String title;
    private String tags;
    private String notes;
    private BookmarkStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Bookmark() {
    }

    public Bookmark(Long id, String url, String title, String tags, String notes,
                    BookmarkStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.url = url;
        this.title = title;
        this.tags = tags;
        this.notes = notes;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public BookmarkStatus getStatus() {
        return status;
    }

    public void setStatus(BookmarkStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Bookmark{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", title='" + title + '\'' +
                ", tags='" + tags + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}
