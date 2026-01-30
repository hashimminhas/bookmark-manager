package com.hashim.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);
    private final String databaseUrl;

    public DatabaseInitializer(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    public void initialize() {
        try (Connection conn = DriverManager.getConnection(databaseUrl);
             Statement stmt = conn.createStatement()) {
            
            // Create bookmarks table with updated schema
            String createTableSql = """
                CREATE TABLE IF NOT EXISTS bookmarks (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    url TEXT NOT NULL,
                    title TEXT NOT NULL,
                    tags TEXT DEFAULT '',
                    notes TEXT DEFAULT '',
                    status TEXT NOT NULL DEFAULT 'INBOX' CHECK (status IN ('INBOX', 'DONE')),
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """;
            
            stmt.execute(createTableSql);
            
            // Create indexes for performance
            String createStatusIndex = "CREATE INDEX IF NOT EXISTS idx_bookmarks_status ON bookmarks(status)";
            stmt.execute(createStatusIndex);
            
            String createCreatedAtIndex = "CREATE INDEX IF NOT EXISTS idx_bookmarks_created_at ON bookmarks(created_at DESC)";
            stmt.execute(createCreatedAtIndex);
            
            String createTagsIndex = "CREATE INDEX IF NOT EXISTS idx_bookmarks_tags ON bookmarks(tags)";
            stmt.execute(createTagsIndex);
            
            String createCompositeIndex = "CREATE INDEX IF NOT EXISTS idx_bookmarks_status_created ON bookmarks(status, created_at DESC)";
            stmt.execute(createCompositeIndex);
            
            logger.info("Database initialized successfully");
            
        } catch (SQLException e) {
            logger.error("Failed to initialize database", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(databaseUrl);
    }
}
