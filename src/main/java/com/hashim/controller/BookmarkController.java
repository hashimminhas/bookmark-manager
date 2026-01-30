package com.hashim.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hashim.dto.CreateBookmarkRequest;
import com.hashim.dto.UpdateBookmarkRequest;
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
        app.get("/api/bookmarks", this::getAllBookmarks);
        app.get("/api/bookmarks/{id}", this::getBookmarkById);
        app.post("/api/bookmarks", this::createBookmark);
        app.put("/api/bookmarks/{id}", this::updateBookmark);
        app.delete("/api/bookmarks/{id}", this::deleteBookmark);
        
        // Register exception handlers
        app.exception(ValidationException.class, (e, ctx) -> {
            logger.warn("Validation error: {}", e.getMessage());
            ctx.status(400).json(createErrorResponse(e.getMessage()));
        });
        
        app.exception(NotFoundException.class, (e, ctx) -> {
            logger.warn("Not found: {}", e.getMessage());
            ctx.status(404).json(createErrorResponse(e.getMessage()));
        });
        
        app.exception(Exception.class, (e, ctx) -> {
            logger.error("Unexpected error", e);
            ctx.status(500).json(createErrorResponse("Internal server error"));
        });
    }

    private void getAllBookmarks(Context ctx) {
        String status = ctx.queryParam("status");
        String search = ctx.queryParam("search");
        
        List<Bookmark> bookmarks;
        
        if (search != null && !search.trim().isEmpty()) {
            bookmarks = bookmarkService.searchBookmarks(search);
        } else if (status != null && !status.trim().isEmpty()) {
            bookmarks = bookmarkService.getBookmarksByStatus(status);
        } else {
            bookmarks = bookmarkService.getAllBookmarks();
        }
        
        ctx.json(bookmarks);
    }

    private void getBookmarkById(Context ctx) {
        Long id = ctx.pathParamAsClass("id", Long.class).get();
        Bookmark bookmark = bookmarkService.getBookmarkById(id);
        ctx.json(bookmark);
    }

    private void createBookmark(Context ctx) {
        CreateBookmarkRequest request = ctx.bodyAsClass(CreateBookmarkRequest.class);
        Bookmark bookmark = bookmarkService.createBookmark(request);
        ctx.status(201).json(bookmark);
    }

    private void updateBookmark(Context ctx) {
        Long id = ctx.pathParamAsClass("id", Long.class).get();
        UpdateBookmarkRequest request = ctx.bodyAsClass(UpdateBookmarkRequest.class);
        Bookmark bookmark = bookmarkService.updateBookmark(id, request);
        ctx.json(bookmark);
    }

    private void deleteBookmark(Context ctx) {
        Long id = ctx.pathParamAsClass("id", Long.class).get();
        bookmarkService.deleteBookmark(id);
        ctx.status(204);
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}
