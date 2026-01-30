# Bookmark Manager - Technical Specification

## 1. Entity Model

### Bookmark
```java
{
  id: Long (auto-generated)
  title: String (required, max 255 chars)
  url: String (required, valid HTTP/HTTPS URL)
  description: String (optional)
  status: BookmarkStatus (enum: INBOX, DONE)
  createdAt: LocalDateTime (auto-generated)
  updatedAt: LocalDateTime (auto-generated)
}
```

### BookmarkStatus (Enum)
```java
INBOX   // Default for new bookmarks
DONE    // Marked as completed/read
```

## 2. REST API Endpoints

### Base URL: `/api/bookmarks`

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| GET | `/api/bookmarks` | Get all bookmarks | - | `200` + `Bookmark[]` |
| GET | `/api/bookmarks?status=INBOX` | Filter by status | - | `200` + `Bookmark[]` |
| GET | `/api/bookmarks?search=keyword` | Search in title/url/desc | - | `200` + `Bookmark[]` |
| GET | `/api/bookmarks/{id}` | Get specific bookmark | - | `200` + `Bookmark` or `404` |
| POST | `/api/bookmarks` | Create new bookmark | `CreateBookmarkRequest` | `201` + `Bookmark` |
| PUT | `/api/bookmarks/{id}` | Update bookmark + status | `UpdateBookmarkRequest` | `200` + `Bookmark` or `404` |
| DELETE | `/api/bookmarks/{id}` | Delete bookmark | - | `204` or `404` |

### Request DTOs

**CreateBookmarkRequest:**
```json
{
  "title": "string (required)",
  "url": "string (required)",
  "description": "string (optional)"
}
```

**UpdateBookmarkRequest:**
```json
{
  "title": "string (required)",
  "url": "string (required)",
  "description": "string (optional)",
  "status": "INBOX|DONE (required)"
}
```

## 3. Validation Rules

| Field | Rule | Error Message |
|-------|------|---------------|
| **title** | Required, not empty | "Title cannot be empty" |
| **title** | Max 255 characters | "Title cannot exceed 255 characters" |
| **url** | Required, not empty | "URL cannot be empty" |
| **url** | Valid HTTP/HTTPS format | "Invalid URL format" |
| **url** | Valid URL syntax | "Invalid URL syntax" |
| **status** | Must be INBOX or DONE | "Invalid status: {value}. Must be INBOX or DONE" |
| **id** | Must exist for GET/PUT/DELETE | "Bookmark not found with id: {id}" |

### URL Validation Logic
```java
1. Check not null/empty
2. Parse as java.net.URL
3. Convert to URI (additional validation)
4. Verify protocol is http or https
```

## 4. Error Response Format

### Standard Error Response
```json
{
  "error": "Error message description"
}
```

### HTTP Status Codes
- `200 OK` - Successful GET/PUT
- `201 Created` - Successful POST
- `204 No Content` - Successful DELETE
- `400 Bad Request` - Validation error
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Unexpected error

### Examples

**400 Bad Request:**
```json
{
  "error": "Title cannot be empty"
}
```

**404 Not Found:**
```json
{
  "error": "Bookmark not found with id: 123"
}
```

**500 Internal Server Error:**
```json
{
  "error": "Internal server error"
}
```

## 5. Project Folder Structure

```
src/main/java/com/hashim/
├── Main.java                          # Application entry point
├── config/
│   └── AppConfig.java                 # Configuration (port, DB URL)
├── controller/
│   └── BookmarkController.java        # REST endpoints (routes + handlers)
├── service/
│   └── BookmarkService.java           # Business logic + validation
├── repository/
│   ├── BookmarkRepository.java        # Data access (JDBC CRUD)
│   └── DatabaseInitializer.java       # DB schema setup
├── model/
│   ├── Bookmark.java                  # Domain entity
│   └── BookmarkStatus.java            # Status enum
├── dto/
│   ├── CreateBookmarkRequest.java     # POST request DTO
│   └── UpdateBookmarkRequest.java     # PUT request DTO
├── exception/
│   ├── ValidationException.java       # 400 errors
│   └── NotFoundException.java         # 404 errors
└── util/
    └── UrlValidator.java              # URL validation helper

src/main/resources/
├── application.properties             # Config file (port, DB)
├── logback.xml                        # Logging configuration
└── public/                            # Static frontend files
    ├── index.html                     # UI
    ├── app.js                         # Frontend logic
    └── styles.css                     # Styling
```

## 6. One-Day Implementation Plan

### Phase 1: Setup (30 min)
- [ ] Create Gradle project with dependencies (Javalin, SQLite JDBC, Gson, Logback)
- [ ] Configure `application.properties` (server.port, database.url)
- [ ] Setup `logback.xml` for logging

### Phase 2: Database Layer (45 min)
- [ ] Create `DatabaseInitializer` with SQLite schema
  - Table: bookmarks (id, title, url, description, status, created_at, updated_at)
  - Index: idx_bookmarks_status
- [ ] Create `BookmarkRepository` with JDBC methods:
  - `create()`, `findAll()`, `findById()`, `findByStatus()`, `search()`, `update()`, `delete()`

### Phase 3: Domain & DTOs (30 min)
- [ ] Create `Bookmark` entity class
- [ ] Create `BookmarkStatus` enum (INBOX, DONE)
- [ ] Create `CreateBookmarkRequest` DTO
- [ ] Create `UpdateBookmarkRequest` DTO

### Phase 4: Business Logic (45 min)
- [ ] Create `ValidationException` and `NotFoundException`
- [ ] Create `UrlValidator` utility
- [ ] Create `BookmarkService` with validation:
  - `createBookmark()` - validate + save (default INBOX)
  - `getAllBookmarks()` - fetch all
  - `getBookmarkById()` - fetch one or 404
  - `getBookmarksByStatus()` - filter by status
  - `searchBookmarks()` - search by keyword
  - `updateBookmark()` - validate + update + status change
  - `deleteBookmark()` - delete or 404

### Phase 5: API Layer (45 min)
- [ ] Create `BookmarkController` with Javalin routes:
  - GET `/api/bookmarks` (with query params: status, search)
  - GET `/api/bookmarks/{id}`
  - POST `/api/bookmarks`
  - PUT `/api/bookmarks/{id}`
  - DELETE `/api/bookmarks/{id}`
- [ ] Add global exception handlers (ValidationException → 400, NotFoundException → 404, Exception → 500)

### Phase 6: Application Setup (30 min)
- [ ] Create `AppConfig` to load properties + env vars
- [ ] Create `Main.java`:
  - Initialize config
  - Initialize database
  - Configure Gson JSON mapper
  - Create Javalin app with static files
  - Register routes
  - Start server

### Phase 7: Frontend (2 hours)
- [ ] Create `index.html` with:
  - Search input
  - Status filter dropdown
  - Add bookmark button + form
  - Bookmarks list display
- [ ] Create `app.js` with:
  - Load/refresh bookmarks
  - Create bookmark (POST)
  - Edit bookmark (PUT) with status change
  - Delete bookmark (DELETE)
  - Search functionality (debounced)
  - Filter by status
  - Toast notifications
- [ ] Create `styles.css` with responsive design

### Phase 8: Testing & Polish (1 hour)
- [ ] Manual testing of all CRUD operations
- [ ] Test validation (empty title, invalid URL, wrong status)
- [ ] Test search and filter
- [ ] Test error responses (400, 404, 500)
- [ ] Check logs are working
- [ ] Test responsive UI on different screen sizes

### Phase 9: Docker & Documentation (45 min)
- [ ] Create `Dockerfile` (multi-stage build)
- [ ] Create `.dockerignore`
- [ ] Test Docker build and run
- [ ] Write `README.md` with usage instructions

---

## Technology Stack
- **Backend:** Java 21, Javalin 6.x
- **Database:** SQLite + JDBC
- **JSON:** Gson
- **Logging:** SLF4J + Logback
- **Frontend:** HTML5, Vanilla JS, CSS3
- **Build:** Gradle

## Database Schema
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

## Configuration
```properties
# application.properties
server.port=7070
database.url=jdbc:sqlite:bookmarks.db
```

## Environment Variables (Override)
- `SERVER_PORT` - Server port (default: 7070)
- `DATABASE_URL` - Database connection string

---

**Total Time:** ~7-8 hours (fits in one working day)  
**Complexity:** Medium  
**Result:** Production-ready REST API + responsive UI
