# 📚 Bookmark Manager

A production-ready bookmark management application built with Java, Javalin, and SQLite. Features a clean REST API and a responsive frontend for organizing and managing your bookmarks efficiently.

## 🚀 Features

- **CRUD Operations**: Create, read, update, and delete bookmarks
- **Status Workflow**: Organize bookmarks with INBOX/DONE statuses
- **Search & Filter**: Find bookmarks by keyword or filter by status
- **URL Validation**: Automatic validation of bookmark URLs
- **Responsive UI**: Clean, modern interface that works on all devices
- **RESTful API**: Well-structured REST endpoints
- **Error Handling**: Comprehensive error handling and validation
- **Docker Support**: Containerized deployment ready

## 🏗️ Architecture

```
bookmark-manager/
├── src/main/java/com/hashim/
│   ├── Main.java                      # Application entry point
│   ├── config/
│   │   └── AppConfig.java             # Configuration management
│   ├── model/
│   │   ├── Bookmark.java              # Domain model
│   │   └── BookmarkStatus.java        # Status enum (INBOX/DONE)
│   ├── repository/
│   │   ├── BookmarkRepository.java    # Data access layer
│   │   └── DatabaseInitializer.java   # Database setup
│   ├── service/
│   │   └── BookmarkService.java       # Business logic
│   ├── controller/
│   │   └── BookmarkController.java    # REST API endpoints
│   ├── dto/
│   │   ├── CreateBookmarkRequest.java
│   │   └── UpdateBookmarkRequest.java
│   ├── exception/
│   │   ├── ValidationException.java
│   │   └── NotFoundException.java
│   └── util/
│       └── UrlValidator.java          # URL validation utility
└── src/main/resources/
    ├── public/
    │   ├── index.html                 # Frontend UI
    │   ├── app.js                     # JavaScript logic
    │   └── styles.css                 # Styling
    └── application.properties         # Configuration file
```

## 🛠️ Technology Stack

**Backend:**
- Java 21
- Javalin 6.x - Lightweight web framework
- SQLite - Embedded database
- JDBC - Database connectivity
- Gson - JSON serialization
- SLF4J + Logback - Logging

**Frontend:**
- HTML5
- Vanilla JavaScript (ES6+)
- CSS3
- Fetch API

## 📋 Prerequisites

- Java 21 or higher
- Gradle 8.5 or higher (or use included wrapper)
- Docker (optional, for containerized deployment)

## 🚀 Getting Started

### Local Development

1. **Clone the repository**
   ```bash
   cd bookmark-manager
   ```

2. **Build the project**
   ```bash
   ./gradlew build
   ```
   
   On Windows:
   ```bash
   gradlew.bat build
   ```

3. **Run the application**
   ```bash
   ./gradlew run
   ```
   
   Or using Java directly:
   ```bash
   java -jar build/libs/bookmark-manager-1.0-SNAPSHOT.jar
   ```

4. **Access the application**
   
   Open your browser and navigate to: `http://localhost:7070`

### Docker Deployment

1. **Build the Docker image**
   ```bash
   docker build -t bookmark-manager .
   ```

2. **Run the container**
   ```bash
   docker run -d \
     -p 7070:7070 \
     -v $(pwd)/data:/app/data \
     --name bookmark-manager \
     bookmark-manager
   ```

3. **View logs**
   ```bash
   docker logs -f bookmark-manager
   ```

4. **Stop the container**
   ```bash
   docker stop bookmark-manager
   docker rm bookmark-manager
   ```

## ⚙️ Configuration

### Environment Variables

- `SERVER_PORT` - Server port (default: 7070)
- `DATABASE_URL` - Database connection string (default: jdbc:sqlite:bookmarks.db)

### Application Properties

Edit `src/main/resources/application.properties`:

```properties
server.port=7070
database.url=jdbc:sqlite:bookmarks.db
```

## 📡 API Endpoints

### Get All Bookmarks
```http
GET /api/bookmarks
```

### Get Bookmarks by Status
```http
GET /api/bookmarks?status=INBOX
GET /api/bookmarks?status=DONE
```

### Search Bookmarks
```http
GET /api/bookmarks?search=github
```

### Get Bookmark by ID
```http
GET /api/bookmarks/{id}
```

### Create Bookmark
```http
POST /api/bookmarks
Content-Type: application/json

{
  "title": "GitHub",
  "url": "https://github.com",
  "description": "Code hosting platform"
}
```

### Update Bookmark
```http
PUT /api/bookmarks/{id}
Content-Type: application/json

{
  "title": "GitHub",
  "url": "https://github.com",
  "description": "Code hosting platform",
  "status": "DONE"
}
```

### Delete Bookmark
```http
DELETE /api/bookmarks/{id}
```

## 🧪 Testing the API

Using curl:

```bash
# Create a bookmark
curl -X POST http://localhost:7070/api/bookmarks \
  -H "Content-Type: application/json" \
  -d '{"title":"Example","url":"https://example.com","description":"Test bookmark"}'

# Get all bookmarks
curl http://localhost:7070/api/bookmarks

# Search bookmarks
curl http://localhost:7070/api/bookmarks?search=example

# Filter by status
curl http://localhost:7070/api/bookmarks?status=INBOX

# Update a bookmark
curl -X PUT http://localhost:7070/api/bookmarks/1 \
  -H "Content-Type: application/json" \
  -d '{"title":"Updated","url":"https://example.com","description":"Updated","status":"DONE"}'

# Delete a bookmark
curl -X DELETE http://localhost:7070/api/bookmarks/1
```

## 📝 Database Schema

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

## 🔒 Error Handling

The API returns appropriate HTTP status codes:

- `200 OK` - Successful GET/PUT requests
- `201 Created` - Successful POST requests
- `204 No Content` - Successful DELETE requests
- `400 Bad Request` - Validation errors
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server errors

Error response format:
```json
{
  "error": "Error message description"
}
```

## 🎨 Frontend Features

- **Responsive Design** - Works on desktop, tablet, and mobile
- **Real-time Search** - Debounced search with 300ms delay
- **Status Filtering** - Quick filter by INBOX/DONE
- **Toast Notifications** - User feedback for all actions
- **Inline Editing** - Edit bookmarks without page reload
- **Delete Confirmation** - Prevents accidental deletions
- **Time-based Formatting** - Relative timestamps (e.g., "2 hours ago")

## 📊 Logging

Logs are stored in `logs/bookmark-manager.log` with 30-day rotation.

Log levels:
- `INFO` - Application lifecycle and operations
- `WARN` - Validation errors, not found errors
- `ERROR` - Unexpected errors and exceptions

## 🔧 Development

### Build Commands

```bash
# Clean build
./gradlew clean build

# Run application
./gradlew run

# Build JAR
./gradlew jar

# Run tests
./gradlew test
```

### Project Structure

- **Model Layer**: Domain entities (Bookmark, BookmarkStatus)
- **Repository Layer**: Data access using JDBC
- **Service Layer**: Business logic and validation
- **Controller Layer**: REST API endpoints
- **DTO Layer**: Request/response objects
- **Exception Layer**: Custom exceptions
- **Util Layer**: Helper utilities (URL validation)

## 🐛 Troubleshooting

### Port already in use
```bash
# Change port via environment variable
export SERVER_PORT=8080
./gradlew run
```

### Database locked
```bash
# Stop all instances and delete the lock file
rm bookmarks.db-journal
```

### Build fails
```bash
# Clean and rebuild
./gradlew clean build --refresh-dependencies
```

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👤 Author

Built with ☕ by Hashim Ali

## 🤝 Contributing

Contributions, issues, and feature requests are welcome!

---

**Happy Bookmarking! 📚✨**
