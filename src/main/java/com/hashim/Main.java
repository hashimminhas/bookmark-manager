package com.hashim;

import java.lang.reflect.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hashim.config.AppConfig;
import com.hashim.controller.BookmarkController;
import com.hashim.repository.BookmarkRepository;
import com.hashim.repository.DatabaseInitializer;
import com.hashim.service.BookmarkService;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.json.JsonMapper;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        // Load configuration
        AppConfig config = new AppConfig();
        
        // Initialize database
        DatabaseInitializer databaseInitializer = new DatabaseInitializer(config.getDatabaseUrl());
        databaseInitializer.initialize();
        
        // Initialize layers
        BookmarkRepository bookmarkRepository = new BookmarkRepository(databaseInitializer);
        BookmarkService bookmarkService = new BookmarkService(bookmarkRepository);
        BookmarkController bookmarkController = new BookmarkController(bookmarkService);
        
        // Configure Gson
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        
        // Create and configure Javalin app
        Javalin app = Javalin.create(javalinConfig -> {
            javalinConfig.staticFiles.add("/public", Location.CLASSPATH);
            javalinConfig.http.defaultContentType = "application/json";
            
            // Configure JSON mapper with Gson
            javalinConfig.jsonMapper(new JsonMapper() {
                @Override
                public String toJsonString(Object obj, Type type) {
                    return gson.toJson(obj, type);
                }

                @Override
                public <T> T fromJsonString(String json, Type targetType) {
                    return gson.fromJson(json, targetType);
                }
            });
        }).start(config.getServerPort());
        
        // Register routes
        bookmarkController.registerRoutes(app);
        
        // Root endpoint redirects to index.html
        app.get("/", ctx -> ctx.redirect("/index.html"));
        
        logger.info("Bookmark Manager started on port {}", config.getServerPort());
        logger.info("Access the application at http://localhost:{}", config.getServerPort());
    }
}
