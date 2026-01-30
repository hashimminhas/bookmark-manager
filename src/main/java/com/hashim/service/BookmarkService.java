package com.hashim.service;

import com.hashim.dto.CreateBookmarkRequest;
import com.hashim.dto.UpdateBookmarkRequest;
import com.hashim.exception.NotFoundException;
import com.hashim.exception.ValidationException;
import com.hashim.model.Bookmark;
import com.hashim.model.BookmarkStatus;
import com.hashim.repository.BookmarkRepository;
import com.hashim.util.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class BookmarkService {
    private static final Logger logger = LoggerFactory.getLogger(BookmarkService.class);
    private final BookmarkRepository bookmarkRepository;

    public BookmarkService(BookmarkRepository bookmarkRepository) {
        this.bookmarkRepository = bookmarkRepository;
    }

    public Bookmark createBookmark(CreateBookmarkRequest request) {
        validateCreateRequest(request);
        
        Bookmark bookmark = new Bookmark();
        bookmark.setTitle(request.getTitle());
        bookmark.setUrl(request.getUrl());
        bookmark.setDescription(request.getDescription());
        bookmark.setStatus(BookmarkStatus.INBOX); // Default status
        
        return bookmarkRepository.create(bookmark);
    }

    public List<Bookmark> getAllBookmarks() {
        return bookmarkRepository.findAll();
    }

    public Bookmark getBookmarkById(Long id) {
        return bookmarkRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Bookmark not found with id: " + id));
    }

    public List<Bookmark> getBookmarksByStatus(String status) {
        try {
            BookmarkStatus bookmarkStatus = BookmarkStatus.valueOf(status.toUpperCase());
            return bookmarkRepository.findByStatus(bookmarkStatus);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid status: " + status + ". Must be INBOX or DONE");
        }
    }

    public List<Bookmark> searchBookmarks(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllBookmarks();
        }
        return bookmarkRepository.search(query);
    }

    public Bookmark updateBookmark(Long id, UpdateBookmarkRequest request) {
        validateUpdateRequest(request);
        
        Bookmark bookmark = getBookmarkById(id); // Will throw NotFoundException if not found
        
        bookmark.setTitle(request.getTitle());
        bookmark.setUrl(request.getUrl());
        bookmark.setDescription(request.getDescription());
        
        try {
            BookmarkStatus status = BookmarkStatus.valueOf(request.getStatus().toUpperCase());
            bookmark.setStatus(status);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid status: " + request.getStatus() + ". Must be INBOX or DONE");
        }
        
        return bookmarkRepository.update(bookmark);
    }

    public void deleteBookmark(Long id) {
        if (!bookmarkRepository.delete(id)) {
            throw new NotFoundException("Bookmark not found with id: " + id);
        }
    }

    private void validateCreateRequest(CreateBookmarkRequest request) {
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new ValidationException("Title cannot be empty");
        }
        
        if (request.getTitle().length() > 255) {
            throw new ValidationException("Title cannot exceed 255 characters");
        }
        
        UrlValidator.validate(request.getUrl());
    }

    private void validateUpdateRequest(UpdateBookmarkRequest request) {
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new ValidationException("Title cannot be empty");
        }
        
        if (request.getTitle().length() > 255) {
            throw new ValidationException("Title cannot exceed 255 characters");
        }
        
        if (request.getStatus() == null || request.getStatus().trim().isEmpty()) {
            throw new ValidationException("Status cannot be empty");
        }
        
        UrlValidator.validate(request.getUrl());
    }
}
