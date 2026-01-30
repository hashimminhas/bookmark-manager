package com.hashim.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hashim.model.Bookmark;
import com.hashim.model.BookmarkStatus;

public class BookmarkRepository {
    private static final Logger logger = LoggerFactory.getLogger(BookmarkRepository.class);
    private final DatabaseInitializer databaseInitializer;

    public BookmarkRepository(DatabaseInitializer databaseInitializer) {
        this.databaseInitializer = databaseInitializer;
    }

    public Bookmark create(Bookmark bookmark) {
        String sql = "INSERT INTO bookmarks (url, title, tags, notes, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = databaseInitializer.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            LocalDateTime now = LocalDateTime.now();
            pstmt.setString(1, bookmark.getUrl());
            pstmt.setString(2, bookmark.getTitle());
            pstmt.setString(3, bookmark.getTags() != null ? bookmark.getTags() : "");
            pstmt.setString(4, bookmark.getNotes() != null ? bookmark.getNotes() : "");
            pstmt.setString(5, bookmark.getStatus().name());
            pstmt.setString(6, now.toString());
            pstmt.setString(7, now.toString());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating bookmark failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    bookmark.setId(generatedKeys.getLong(1));
                    bookmark.setCreatedAt(now);
                    bookmark.setUpdatedAt(now);
                } else {
                    throw new SQLException("Creating bookmark failed, no ID obtained.");
                }
            }
            
            logger.info("Created bookmark with id: {}", bookmark.getId());
            return bookmark;
            
        } catch (SQLException e) {
            logger.error("Error creating bookmark", e);
            throw new RuntimeException("Failed to create bookmark", e);
        }
    }

    public List<Bookmark> findAll() {
        String sql = "SELECT * FROM bookmarks ORDER BY created_at DESC";
        return executeQuery(sql);
    }

    public Optional<Bookmark> findById(Long id) {
        String sql = "SELECT * FROM bookmarks WHERE id = ?";
        
        try (Connection conn = databaseInitializer.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToBookmark(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error finding bookmark by id: {}", id, e);
            throw new RuntimeException("Failed to find bookmark", e);
        }
        
        return Optional.empty();
    }

    public List<Bookmark> findByStatus(BookmarkStatus status) {
        String sql = "SELECT * FROM bookmarks WHERE status = ? ORDER BY created_at DESC";
        
        try (Connection conn = databaseInitializer.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status.name());
            
            List<Bookmark> bookmarks = new ArrayList<>();
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    bookmarks.add(mapResultSetToBookmark(rs));
                }
            }
            
            return bookmarks;
            
        } catch (SQLException e) {
            logger.error("Error finding bookmarks by status: {}", status, e);
            throw new RuntimeException("Failed to find bookmarks by status", e);
        }
    }

    public List<Bookmark> search(String query) {
        String sql = "SELECT * FROM bookmarks WHERE url LIKE ? OR title LIKE ? OR tags LIKE ? OR notes LIKE ? ORDER BY created_at DESC";
        
        try (Connection conn = databaseInitializer.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + query + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            pstmt.setString(4, searchPattern);
            
            List<Bookmark> bookmarks = new ArrayList<>();
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    bookmarks.add(mapResultSetToBookmark(rs));
                }
            }
            
            return bookmarks;
            
        } catch (SQLException e) {
            logger.error("Error searching bookmarks with query: {}", query, e);
            throw new RuntimeException("Failed to search bookmarks", e);
        }
    }

    public Bookmark update(Bookmark bookmark) {
        String sql = "UPDATE bookmarks SET url = ?, title = ?, tags = ?, notes = ?, status = ?, updated_at = ? WHERE id = ?";
        
        try (Connection conn = databaseInitializer.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            LocalDateTime now = LocalDateTime.now();
            pstmt.setString(1, bookmark.getUrl());
            pstmt.setString(2, bookmark.getTitle());
            pstmt.setString(3, bookmark.getTags() != null ? bookmark.getTags() : "");
            pstmt.setString(4, bookmark.getNotes() != null ? bookmark.getNotes() : "");
            pstmt.setString(5, bookmark.getStatus().name());
            pstmt.setString(6, now.toString());
            pstmt.setLong(7, bookmark.getId());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Updating bookmark failed, no rows affected.");
            }
            
            bookmark.setUpdatedAt(now);
            logger.info("Updated bookmark with id: {}", bookmark.getId());
            return bookmark;
            
        } catch (SQLException e) {
            logger.error("Error updating bookmark with id: {}", bookmark.getId(), e);
            throw new RuntimeException("Failed to update bookmark", e);
        }
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM bookmarks WHERE id = ?";
        
        try (Connection conn = databaseInitializer.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("Deleted bookmark with id: {}", id);
                return true;
            }
            
            return false;
            
        } catch (SQLException e) {
            logger.error("Error deleting bookmark with id: {}", id, e);
            throw new RuntimeException("Failed to delete bookmark", e);
        }
    }

    private List<Bookmark> executeQuery(String sql) {
        try (Connection conn = databaseInitializer.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            List<Bookmark> bookmarks = new ArrayList<>();
            while (rs.next()) {
                bookmarks.add(mapResultSetToBookmark(rs));
            }
            
            return bookmarks;
            
        } catch (SQLException e) {
            logger.error("Error executing query: {}", sql, e);
            throw new RuntimeException("Failed to execute query", e);
        }
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
