package com.hashim.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hashim.dto.CreateBookmarkRequest;
import com.hashim.dto.UpdateBookmarkRequest;
import com.hashim.dto.UpdateStatusRequest;
import com.hashim.exception.NotFoundException;
import com.hashim.exception.ValidationException;
import com.hashim.model.Bookmark;
import com.hashim.model.BookmarkStatus;
import com.hashim.repository.BookmarkQueryRepository;
import com.hashim.repository.BookmarkRepository;
import com.hashim.util.UrlValidator;
import com.hashim.util.ValidationUtils;

public class BookmarkService {
    private static final Logger logger = LoggerFactory.getLogger(BookmarkService.class);
    private final BookmarkRepository bookmarkRepository;
    private final BookmarkQueryRepository queryRepository;

    public BookmarkService(BookmarkRepository bookmarkRepository, BookmarkQueryRepository queryRepository) {
        this.bookmarkRepository = bookmarkRepository;
        this.queryRepository = queryRepository;
    }

    public Bookmark createBookmark(CreateBookmarkRequest request) {
        // Validate and normalize all fields
        String normalizedUrl = UrlValidator.normalizeAndValidate(request.getUrl());
        String normalizedTitle = ValidationUtils.validateAndNormalizeTitle(request.getTitle());
        String normalizedTags = ValidationUtils.validateAndNormalizeTags(request.getTags());
        String normalizedNotes = ValidationUtils.validateAndNormalizeNotes(request.getNotes());
        
        Bookmark bookmark = new Bookmark();
        bookmark.setUrl(normalizedUrl);
        bookmark.setTitle(normalizedTitle);
        bookmark.setTags(normalizedTags);
        bookmark.setNotes(normalizedNotes);
        bookmark.setStatus(BookmarkStatus.INBOX); // Default status
        
        return bookmarkRepository.create(bookmark);
    }

    public List<Bookmark> getAllBookmarks() {
        return bookmarkRepository.findAll();
    }
    
    public List<Bookmark> getBookmarksWithFilters(String searchQuery, String statusStr, String tag,
                                                   String sortBy, String order, Integer limit, Integer offset) {
        // Validate and parse status
        BookmarkStatus status = null;
        if (statusStr != null && !statusStr.trim().isEmpty()) {
            try {
                status = BookmarkStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ValidationException("Invalid status: " + statusStr + ". Must be INBOX or DONE");
            }
        }
        
        // Validate sort field
        if (sortBy != null && !isValidSortField(sortBy)) {
            throw new ValidationException("Sort field must be one of: created_at, updated_at, title");
        }
        
        // Validate order
        if (order != null && !order.equalsIgnoreCase("asc") && !order.equalsIgnoreCase("desc")) {
            throw new ValidationException("Order must be asc or desc");
        }
        
        // Validate pagination parameters
        ValidationUtils.validatePositive(limit, "limit");
        ValidationUtils.validateNonNegative(offset, "offset");
        
        int actualLimit = (limit != null && limit > 0) ? Math.min(limit, 1000) : 100;
        int actualOffset = (offset != null && offset >= 0) ? offset : 0;
        
        return queryRepository.findWithFilters(searchQuery, status, tag, sortBy, order, actualLimit, actualOffset);
    }
    
    private boolean isValidSortField(String field) {
        return "created_at".equalsIgnoreCase(field) || 
               "updated_at".equalsIgnoreCase(field) || 
               "title".equalsIgnoreCase(field);
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
        // Validate and normalize all fields
        String normalizedUrl = UrlValidator.normalizeAndValidate(request.getUrl());
        String normalizedTitle = ValidationUtils.validateAndNormalizeTitle(request.getTitle());
        String normalizedTags = ValidationUtils.validateAndNormalizeTags(request.getTags());
        String normalizedNotes = ValidationUtils.validateAndNormalizeNotes(request.getNotes());
        ValidationUtils.validateStatus(request.getStatus());
        
        Bookmark bookmark = getBookmarkById(id); // Will throw NotFoundException if not found
        
        bookmark.setUrl(normalizedUrl);
        bookmark.setTitle(normalizedTitle);
        bookmark.setTags(normalizedTags);
        bookmark.setNotes(normalizedNotes);
        
        try {
            BookmarkStatus status = BookmarkStatus.valueOf(request.getStatus().toUpperCase());
            bookmark.setStatus(status);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid status: " + request.getStatus() + ". Must be INBOX or DONE");
        }
        
        return bookmarkRepository.update(bookmark);
    }
    
    public Bookmark updateBookmarkStatus(Long id, UpdateStatusRequest request) {
        ValidationUtils.validateStatus(request.getStatus());
        
        Bookmark bookmark = getBookmarkById(id);
        
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
}
