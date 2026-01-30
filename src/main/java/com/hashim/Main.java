package com.hashim;

import java.lang.reflect.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hashim.config.AppConfig;
import com.hashim.controller.BookmarkController;
import com.hashim.repository.BookmarkQueryRepository;
import com.hashim.repository.BookmarkRepository;
import com.hashim.repository.DatabaseInitializer;
import com.hashim.service.BookmarkService;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.json.JsonMapper;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        // Load configuration (reads PORT, DB_URL env vars with defaults)
        AppConfig config = new AppConfig();
        
        logger.info("Starting Bookmark Manager...");
        logger.info("Port: {}", config.getServerPort());
        logger.info("Database: {}", config.getDatabaseUrl());
        
        // Initialize database
        DatabaseInitializer databaseInitializer = new DatabaseInitializer(config.getDatabaseUrl());
        databaseInitializer.initialize();
        
        // Initialize layers
        BookmarkRepository bookmarkRepository = new BookmarkRepository(databaseInitializer);
        BookmarkQueryRepository queryRepository = new BookmarkQueryRepository(databaseInitializer);
        BookmarkService bookmarkService = new BookmarkService(bookmarkRepository, queryRepository);
        BookmarkController bookmarkController = new BookmarkController(bookmarkService);
        
        // Configure Gson for JSON serialization
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .registerTypeAdapter(java.time.LocalDateTime.class, 
                    (com.google.gson.JsonSerializer<java.time.LocalDateTime>) (src, typeOfSrc, context) -> 
                        new com.google.gson.JsonPrimitive(src.toString()))
                .registerTypeAdapter(java.time.LocalDateTime.class,
                    (com.google.gson.JsonDeserializer<java.time.LocalDateTime>) (json, typeOfT, context) ->
                        java.time.LocalDateTime.parse(json.getAsString()))
                .create();
        
        // Create and configure Javalin app
        Javalin app = Javalin.create(javalinConfig -> {
            // Static files for frontend
            javalinConfig.staticFiles.add("/public", Location.CLASSPATH);
            
            // Default content type
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
        
        // Enable CORS for local development
        app.before(ctx -> {
            ctx.header("Access-Control-Allow-Origin", "*");
            ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
            ctx.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
        });
        
        // Handle OPTIONS requests for CORS preflight
        app.options("/*", ctx -> ctx.status(204));
        
        // Register routes
        bookmarkController.registerRoutes(app);
        
        // Root endpoint redirects to index.html
        app.get("/", ctx -> ctx.redirect("/index.html"));
        
        logger.info("✅ Bookmark Manager started successfully");
        logger.info("🌐 Access the application at http://localhost:{}", config.getServerPort());
        logger.info("🏥 Health check at http://localhost:{}/health", config.getServerPort());
        logger.info("📚 API docs at http://localhost:{}/api/bookmarks", config.getServerPort());
    }
}
