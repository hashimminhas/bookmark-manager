package com.hashim.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);
    private final String databaseUrl;

    public DatabaseInitializer(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    public void initialize() {
        try (Connection conn = DriverManager.getConnection(databaseUrl);
             Statement stmt = conn.createStatement()) {
            
            // Create bookmarks table
            String createTableSql = """
                CREATE TABLE IF NOT EXISTS bookmarks (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    url TEXT NOT NULL,
                    description TEXT,
                    status TEXT NOT NULL DEFAULT 'INBOX',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;
            
            stmt.execute(createTableSql);
            
            // Create index on status for faster filtering
            String createIndexSql = "CREATE INDEX IF NOT EXISTS idx_bookmarks_status ON bookmarks(status)";
            stmt.execute(createIndexSql);
            
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
