# Bookmark Manager - Quick Start Guide

## 🚀 Quick Start

Your production-ready Bookmark Manager is complete and tested!

## Running the Application

### Option 1: Using Gradle (Recommended for Development)
```bash
# Default port (7070)
./gradlew run

# Custom port
$env:SERVER_PORT='8888'
./gradlew run
```

### Option 2: Using JAR
```bash
# Build the JAR
./gradlew jar

# Run the JAR
java -jar build/libs/bookmark-manager-1.0-SNAPSHOT.jar
```

### Option 3: Docker (Production)
```bash
# Build the image
docker build -t bookmark-manager .

# Run the container
docker run -d -p 7070:7070 -v $(pwd)/data:/app/data --name bookmark-manager bookmark-manager

# View logs
docker logs -f bookmark-manager
```

## 📁 Project Structure

```
bookmark-manager/
├── src/main/java/com/hashim/
│   ├── Main.java                      # ✅ Entry point with Gson JSON mapper
│   ├── config/
│   │   └── AppConfig.java             # ✅ Configuration (env vars + properties)
│   ├── model/
│   │   ├── Bookmark.java              # ✅ Domain model
│   │   └── BookmarkStatus.java        # ✅ INBOX/DONE enum
│   ├── repository/
│   │   ├── BookmarkRepository.java    # ✅ JDBC data access
│   │   └── DatabaseInitializer.java   # ✅ SQLite schema setup
│   ├── service/
│   │   └── BookmarkService.java       # ✅ Business logic + validation
│   ├── controller/
│   │   └── BookmarkController.java    # ✅ REST endpoints
│   ├── dto/
│   │   ├── CreateBookmarkRequest.java # ✅ Create DTO
│   │   └── UpdateBookmarkRequest.java # ✅ Update DTO
│   ├── exception/
│   │   ├── ValidationException.java   # ✅ Custom exceptions
│   │   └── NotFoundException.java     # ✅ Custom exceptions
│   └── util/
│       └── UrlValidator.java          # ✅ URL validation utility
├── src/main/resources/
│   ├── public/
│   │   ├── index.html                 # ✅ Responsive UI
│   │   ├── app.js                     # ✅ Frontend logic
│   │   └── styles.css                 # ✅ Modern styling
│   ├── application.properties         # ✅ Configuration
│   └── logback.xml                    # ✅ Logging config
├── Dockerfile                         # ✅ Multi-stage build
├── .dockerignore                      # ✅ Docker optimization
├── build.gradle.kts                   # ✅ Dependencies configured
├── test-api.ps1                       # ✅ API test script
└── README.md                          # ✅ Comprehensive docs
```

## ✨ Features Implemented

### Backend
- ✅ **Java + Javalin** - Lightweight web framework (NO Spring Boot)
- ✅ **SQLite + JDBC** - Local database, no ORM
- ✅ **Clean Architecture** - Controller → Service → Repository pattern
- ✅ **Error Handling** - Global exception handlers for 400/404/500
- ✅ **Validation** - URL format validation, title length checks
- ✅ **Configuration** - Environment variables + properties file
- ✅ **Logging** - SLF4J + Logback with file rotation
- ✅ **JSON Support** - Gson mapper configured

### Frontend
- ✅ **Responsive UI** - Works on desktop, tablet, mobile
- ✅ **CRUD Operations** - Create, Read, Update, Delete bookmarks
- ✅ **Search** - Real-time search with debouncing
- ✅ **Filter** - Filter by status (INBOX/DONE)
- ✅ **Toast Notifications** - User feedback for all actions
- ✅ **Inline Editing** - Edit without page reload
- ✅ **Modern CSS** - Clean, professional design

### DevOps
- ✅ **Dockerfile** - Multi-stage build, non-root user
- ✅ **Health Check** - Built into Docker container
- ✅ **Volume Mapping** - Persistent data storage
- ✅ **Gradle Build** - Fat JAR with all dependencies

## 🧪 Testing

### Manual Testing via Browser
1. Open http://localhost:8888 (or your configured port)
2. Click "Add Bookmark"
3. Fill in details and save
4. Test search, filter, edit, and delete

### API Testing via PowerShell
```powershell
# Create a bookmark
$body = @{
    title = "GitHub"
    url = "https://github.com"
    description = "Code hosting"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8888/api/bookmarks" -Method POST -Body $body -ContentType "application/json"

# Get all bookmarks
Invoke-RestMethod -Uri "http://localhost:8888/api/bookmarks" -Method GET

# Search
Invoke-RestMethod -Uri "http://localhost:8888/api/bookmarks?search=github" -Method GET

# Filter by status
Invoke-RestMethod -Uri "http://localhost:8888/api/bookmarks?status=INBOX" -Method GET
```

## 📊 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/bookmarks` | Get all bookmarks |
| GET | `/api/bookmarks?status=INBOX` | Filter by status |
| GET | `/api/bookmarks?search=term` | Search bookmarks |
| GET | `/api/bookmarks/{id}` | Get specific bookmark |
| POST | `/api/bookmarks` | Create bookmark |
| PUT | `/api/bookmarks/{id}` | Update bookmark |
| DELETE | `/api/bookmarks/{id}` | Delete bookmark |

## 🗄️ Database Schema

```sql
CREATE TABLE bookmarks (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    url TEXT NOT NULL,
    description TEXT,
    status TEXT NOT NULL DEFAULT 'INBOX',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_bookmarks_status ON bookmarks(status);
```

## 🔧 Configuration

### Environment Variables
- `SERVER_PORT` - Server port (default: 7070)
- `DATABASE_URL` - Database connection string (default: jdbc:sqlite:bookmarks.db)

### Application Properties
Edit `src/main/resources/application.properties`:
```properties
server.port=7070
database.url=jdbc:sqlite:bookmarks.db
```

## 📝 Build Status

✅ **Build Successful** - All dependencies resolved
✅ **Compilation Successful** - No errors
✅ **Application Started** - Running on port 8888
✅ **Database Initialized** - SQLite schema created
✅ **Frontend Accessible** - UI loading correctly
✅ **JSON Mapper Configured** - Gson working properly

## 🎯 Production Readiness Checklist

- ✅ Clean code structure (MVC pattern)
- ✅ Error handling (400, 404, 500)
- ✅ Input validation (URL format, length checks)
- ✅ Logging (file rotation, proper levels)
- ✅ Configuration management (env vars)
- ✅ Docker support (multi-stage, health check)
- ✅ Documentation (README, API docs)
- ✅ Database indexing (status column)
- ✅ Security (non-root Docker user)
- ✅ Resource cleanup (try-with-resources)

## 🚦 Next Steps

1. **Test the Application**: Open http://localhost:8888 and try all features
2. **Review the Code**: Check the clean architecture and error handling
3. **Build Docker Image**: Test containerized deployment
4. **Deploy**: Use the Dockerfile for production deployment
5. **Monitor**: Check logs in `logs/bookmark-manager.log`

## 📞 Support

If you encounter any issues:
1. Check the logs: `logs/bookmark-manager.log`
2. Verify port availability
3. Ensure Java 21+ is installed
4. Check database file permissions

---

**Status**: ✅ PRODUCTION READY
**Built with**: Java 21 + Javalin + SQLite
**Author**: Hashim Ali
