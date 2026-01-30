package com.hashim.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hashim.dto.CreateBookmarkRequest;
import com.hashim.dto.ErrorResponse;
import com.hashim.dto.UpdateBookmarkRequest;
import com.hashim.dto.UpdateStatusRequest;
import com.hashim.exception.NotFoundException;
import com.hashim.exception.ValidationException;
import com.hashim.model.Bookmark;
import com.hashim.service.BookmarkService;

import io.javalin.Javalin;
import io.javalin.http.Context;

public class BookmarkController {
    private static final Logger logger = LoggerFactory.getLogger(BookmarkController.class);
    private final BookmarkService bookmarkService;

    public BookmarkController(BookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
    }

    public void registerRoutes(Javalin app) {
        // Health check endpoint
        app.get("/health", this::health);
        
        // Bookmark CRUD endpoints
        app.get("/api/bookmarks", this::getAllBookmarks);
        app.get("/api/bookmarks/{id}", this::getBookmarkById);
        app.post("/api/bookmarks", this::createBookmark);
        app.put("/api/bookmarks/{id}", this::updateBookmark);
        app.patch("/api/bookmarks/{id}/status", this::updateBookmarkStatus);
        app.delete("/api/bookmarks/{id}", this::deleteBookmark);
        
        // Register exception handlers
        registerExceptionHandlers(app);
    }
    
    private void registerExceptionHandlers(Javalin app) {
        app.exception(ValidationException.class, (e, ctx) -> {
            logger.warn("Validation error: {}", e.getMessage());
            ErrorResponse error = new ErrorResponse("VALIDATION_ERROR", e.getMessage());
            ctx.status(400).json(error);
        });
        
        app.exception(NotFoundException.class, (e, ctx) -> {
            logger.warn("Not found: {}", e.getMessage());
            ErrorResponse error = new ErrorResponse("NOT_FOUND", e.getMessage());
            ctx.status(404).json(error);
        });
        
        app.exception(IllegalArgumentException.class, (e, ctx) -> {
            logger.warn("Invalid argument: {}", e.getMessage());
            ErrorResponse error = new ErrorResponse("INVALID_PARAMETER", e.getMessage());
            ctx.status(400).json(error);
        });
        
        app.exception(Exception.class, (e, ctx) -> {
            logger.error("Unexpected error", e);
            ErrorResponse error = new ErrorResponse("INTERNAL_ERROR", "Internal server error");
            ctx.status(500).json(error);
        });
    }
    
    private void health(Context ctx) {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        ctx.json(health);
    }

    private void getAllBookmarks(Context ctx) {
        String search = ctx.queryParam("q");
        String status = ctx.queryParam("status");
        String tag = ctx.queryParam("tag");
        String sortBy = ctx.queryParam("sort");
        String order = ctx.queryParam("order");
        Integer limit = ctx.queryParamAsClass("limit", Integer.class).allowNullable().get();
        Integer offset = ctx.queryParamAsClass("offset", Integer.class).allowNullable().get();
        
        List<Bookmark> bookmarks;
        
        // Use advanced filtering if any filter is present
        if (search != null || status != null || tag != null || sortBy != null || 
            order != null || limit != null || offset != null) {
            bookmarks = bookmarkService.getBookmarksWithFilters(search, status, tag, sortBy, order, limit, offset);
        } else {
            bookmarks = bookmarkService.getAllBookmarks();
        }
        
        ctx.json(bookmarks);
    }

    private void getBookmarkById(Context ctx) {
        Long id = parseId(ctx.pathParam("id"));
        Bookmark bookmark = bookmarkService.getBookmarkById(id);
        ctx.json(bookmark);
    }

    private void createBookmark(Context ctx) {
        CreateBookmarkRequest request = ctx.bodyAsClass(CreateBookmarkRequest.class);
        Bookmark bookmark = bookmarkService.createBookmark(request);
        ctx.status(201).json(bookmark);
    }

    private void updateBookmark(Context ctx) {
        Long id = parseId(ctx.pathParam("id"));
        UpdateBookmarkRequest request = ctx.bodyAsClass(UpdateBookmarkRequest.class);
        Bookmark bookmark = bookmarkService.updateBookmark(id, request);
        ctx.json(bookmark);
    }
    
    private void updateBookmarkStatus(Context ctx) {
        Long id = parseId(ctx.pathParam("id"));
        UpdateStatusRequest request = ctx.bodyAsClass(UpdateStatusRequest.class);
        Bookmark bookmark = bookmarkService.updateBookmarkStatus(id, request);
        ctx.json(bookmark);
    }

    private void deleteBookmark(Context ctx) {
        Long id = parseId(ctx.pathParam("id"));
        bookmarkService.deleteBookmark(id);
        ctx.status(204);
    }
    
    private Long parseId(String idStr) {
        try {
            return Long.parseLong(idStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid bookmark ID format: " + idStr);
        }
    }
}
