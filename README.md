# Bookmark Manager

A lightweight bookmark management application with a REST API and web UI for organizing and searching bookmarks.

## Features

- Create, read, update, and delete bookmarks
- Search by keyword and filter by status (INBOX/DONE)
- URL normalization and validation
- Tag and note support
- Responsive web interface
- RESTful API with JSON responses

## Tech Stack

- **Backend**: Java 21, Javalin 6.1.3
- **Database**: SQLite 3.45.0
- **Frontend**: Vanilla JavaScript, HTML5, CSS3
- **Build Tool**: Gradle 9.0
- **Deployment**: Docker (multi-stage build)

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
## API Overview

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/bookmarks` | List all bookmarks |
| GET | `/api/bookmarks?status=INBOX` | Filter by status |
| GET | `/api/bookmarks?search=keyword` | Search bookmarks |
| GET | `/api/bookmarks/{id}` | Get specific bookmark |
| POST | `/api/bookmarks` | Create bookmark |
| PUT | `/api/bookmarks/{id}` | Update bookmark |
| DELETE | `/api/bookmarks/{id}` | Delete bookmark |

## Running Locally

### Prerequisites
- Java 21 or higher
- Gradle (wrapper included)

### Steps

1. Clone and navigate to the project:
   ```bash
   git clone <repository-url>
   cd bookmark-manager
   ```

2. Run the application:
   ```bash
   ./gradlew run
   ```
   
   Windows:
   ```bash
   gradlew.bat run
   ```

3. Access the application at `http://localhost:8888`

### Custom Configuration

Set environment variables:
```bash
# Linux/Mac
export PORT=9000
export DB_URL=jdbc:sqlite:data/bookmarks.db
./gradlew run

# Windows PowerShell
$env:PORT='9000'
$env:DB_URL='jdbc:sqlite:data/bookmarks.db'
.\gradlew.bat run
```

## Running with Docker

### Build and Run

```bash
# Build image
docker build -t bookmark-manager:latest .

# Run with persistent storage
docker run -d \
  --name bookmark-manager \
  -p 8888:8888 \
  -v bookmark-data:/app/data \
  bookmark-manager:latest
```

### Custom Configuration

```bash
docker run -d \
  --name bookmark-manager \
  -p 9000:9000 \
  -e PORT=9000 \
  -e DB_URL=jdbc:sqlite:/app/data/bookmarks.db \
  -v bookmark-data:/app/data \
  bookmark-manager:latest
```

### Management

```bash
# View logs
docker logs -f bookmark-manager

# Stop
docker stop bookmark-manager

# Remove
docker rm bookmark-manager
```

## Using the UI

1. **Add Bookmark**: Click "Add Bookmark" button, fill in URL and title (tags/notes optional)
2. **Search**: Type in the search box to filter bookmarks by keyword
3. **Filter**: Use status dropdown to show INBOX or DONE bookmarks
4. **Edit**: Click "Edit" button on any bookmark card
5. **Delete**: Click "Delete" button (confirmation required)
6. **Mark Done**: Edit bookmark and change status to DONE

## Example cURL Commands

### Create a bookmark
```bash
curl -X POST http://localhost:8888/api/bookmarks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "GitHub",
    "url": "https://github.com",
    "tags": "development,git",
    "notes": "Source code hosting"
  }'
```

### Get all bookmarks
```bash
curl http://localhost:8888/api/bookmarks
```

### Search bookmarks
```bash
curl "http://localhost:8888/api/bookmarks?search=github"
```

### Filter by status
```bash
curl "http://localhost:8888/api/bookmarks?status=INBOX"
```

### Update bookmark
```bash
curl -X PUT http://localhost:8888/api/bookmarks/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "GitHub - Updated",
    "url": "https://github.com",
    "tags": "dev,git,code",
    "notes": "Updated notes",
    "status": "DONE"
  }'
```

### Delete bookmark
```bash
curl -X DELETE http://localhost:8888/api/bookmarks/1
```

## License

MIT License - see LICENSE file for details.
