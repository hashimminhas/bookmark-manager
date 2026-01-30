package com.hashim.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppConfig {
    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);
    private final Properties properties;

    public AppConfig() {
        properties = new Properties();
        loadProperties();
    }

    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                properties.load(input);
                logger.info("Configuration loaded successfully");
            } else {
                logger.warn("application.properties not found, using defaults");
                setDefaults();
            }
        } catch (IOException e) {
            logger.error("Error loading configuration", e);
            setDefaults();
        }
        
        // Override with environment variables if present
        overrideWithEnvVars();
    }

    private void setDefaults() {
        properties.setProperty("server.port", "7070");
        properties.setProperty("database.url", "jdbc:sqlite:bookmarks.db");
    }

    private void overrideWithEnvVars() {
        // Check PORT env var (common in cloud platforms)
        String port = System.getenv("PORT");
        if (port != null) {
            properties.setProperty("server.port", port);
        }
        
        // Check SERVER_PORT env var
        String serverPort = System.getenv("SERVER_PORT");
        if (serverPort != null) {
            properties.setProperty("server.port", serverPort);
        }
        
        // Check DB_URL env var
        String dbUrl = System.getenv("DB_URL");
        if (dbUrl != null) {
            properties.setProperty("database.url", dbUrl);
        }
        
        // Check DATABASE_URL env var
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl != null) {
            properties.setProperty("database.url", databaseUrl);
        }
    }

    public int getServerPort() {
        return Integer.parseInt(properties.getProperty("server.port", "7070"));
    }

    public String getDatabaseUrl() {
        return properties.getProperty("database.url", "jdbc:sqlite:bookmarks.db");
    }
}
