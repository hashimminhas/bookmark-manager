package com.hashim.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hashim.model.Bookmark;
import com.hashim.model.BookmarkStatus;

public class BookmarkQueryRepository {
    private static final Logger logger = LoggerFactory.getLogger(BookmarkQueryRepository.class);
    private final DatabaseInitializer databaseInitializer;

    public BookmarkQueryRepository(DatabaseInitializer databaseInitializer) {
        this.databaseInitializer = databaseInitializer;
    }

    public List<Bookmark> findWithFilters(String searchQuery, BookmarkStatus status, String tag, 
                                          String sortBy, String order, int limit, int offset) {
        StringBuilder sql = new StringBuilder("SELECT * FROM bookmarks WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        // Add search filter
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            sql.append(" AND (url LIKE ? OR title LIKE ? OR tags LIKE ? OR notes LIKE ?)");
            String pattern = "%" + searchQuery + "%";
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }
        
        // Add status filter
        if (status != null) {
            sql.append(" AND status = ?");
            params.add(status.name());
        }
        
        // Add tag filter
        if (tag != null && !tag.trim().isEmpty()) {
            sql.append(" AND tags LIKE ?");
            params.add("%" + tag + "%");
        }
        
        // Add sorting
        String sortField = getSortField(sortBy);
        String sortOrder = "desc".equalsIgnoreCase(order) ? "DESC" : "ASC";
        sql.append(" ORDER BY ").append(sortField).append(" ").append(sortOrder);
        
        // Add pagination
        sql.append(" LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        
        try (Connection conn = databaseInitializer.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            // Set parameters
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            
            List<Bookmark> bookmarks = new ArrayList<>();
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    bookmarks.add(mapResultSetToBookmark(rs));
                }
            }
            
            return bookmarks;
            
        } catch (SQLException e) {
            logger.error("Error executing filtered query", e);
            throw new RuntimeException("Failed to query bookmarks", e);
        }
    }
    
    private String getSortField(String sortBy) {
        if (sortBy == null) {
            return "created_at";
        }
        return switch (sortBy.toLowerCase()) {
            case "updated_at" -> "updated_at";
            case "title" -> "title";
            default -> "created_at";
        };
    }
    
    private Bookmark mapResultSetToBookmark(ResultSet rs) throws SQLException {
        Bookmark bookmark = new Bookmark();
        bookmark.setId(rs.getLong("id"));
        bookmark.setUrl(rs.getString("url"));
        bookmark.setTitle(rs.getString("title"));
        bookmark.setTags(rs.getString("tags"));
        bookmark.setNotes(rs.getString("notes"));
        bookmark.setStatus(BookmarkStatus.valueOf(rs.getString("status")));
        bookmark.setCreatedAt(LocalDateTime.parse(rs.getString("created_at")));
        bookmark.setUpdatedAt(LocalDateTime.parse(rs.getString("updated_at")));
        return bookmark;
    }
}
