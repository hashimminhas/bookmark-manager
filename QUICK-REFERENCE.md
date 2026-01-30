# 🚀 Bookmark Manager - Quick Reference

## Application Access
- **URL:** http://localhost:8888
- **Status:** ✅ Running
- **Port:** 8888

## Run Commands

### Start Application
```powershell
# Default port (7070)
./gradlew run

# Custom port (8888)
$env:SERVER_PORT='8888'
./gradlew run
```

### Build JAR
```powershell
./gradlew jar
java -jar build/libs/bookmark-manager-1.0-SNAPSHOT.jar
```

## API Quick Test

### Create Bookmark (PowerShell)
```powershell
$body = @{
    title = "GitHub"
    url = "https://github.com"
    description = "Code hosting platform"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8888/api/bookmarks" `
    -Method POST `
    -Body $body `
    -ContentType "application/json"
```

### Get All Bookmarks
```powershell
Invoke-RestMethod -Uri "http://localhost:8888/api/bookmarks"
```

### Search Bookmarks
```powershell
Invoke-RestMethod -Uri "http://localhost:8888/api/bookmarks?search=github"
```

### Filter by Status
```powershell
Invoke-RestMethod -Uri "http://localhost:8888/api/bookmarks?status=INBOX"
```

## All Endpoints

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/api/bookmarks` | Get all or filter/search |
| GET | `/api/bookmarks/{id}` | Get specific bookmark |
| POST | `/api/bookmarks` | Create bookmark |
| PUT | `/api/bookmarks/{id}` | Update bookmark & status |
| DELETE | `/api/bookmarks/{id}` | Delete bookmark |

## Key Features
✅ CRUD operations  
✅ Search (title, URL, description)  
✅ Filter by status (INBOX/DONE)  
✅ URL validation  
✅ Responsive UI  
✅ Toast notifications  

## Documents
- [SPEC.md](SPEC.md) - Technical specification
- [README.md](README.md) - Comprehensive documentation
- [QUICKSTART.md](QUICKSTART.md) - Quick start guide

---

**Current Status:** Application is running on http://localhost:8888
