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
        String port = System.getenv("SERVER_PORT");
        if (port != null) {
            properties.setProperty("server.port", port);
        }
        
        String dbUrl = System.getenv("DATABASE_URL");
        if (dbUrl != null) {
            properties.setProperty("database.url", dbUrl);
        }
    }

    public int getServerPort() {
        return Integer.parseInt(properties.getProperty("server.port", "7070"));
    }

    public String getDatabaseUrl() {
        return properties.getProperty("database.url", "jdbc:sqlite:bookmarks.db");
    }
}
