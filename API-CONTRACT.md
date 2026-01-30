# Bookmark Manager - REST API Contract

## Base URL
```
http://localhost:7070/api
```

## Response Format

All responses use JSON with consistent error handling.

### Success Response
```json
{
  "data": { /* resource or array */ }
}
```
Or directly return the resource/array for simpler APIs.

### Error Response (Consistent Shape)
```json
{
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable error message",
    "details": {} // Optional: validation errors or additional context
  }
}
```

---

## Endpoints

### 1. Create Bookmark

**POST** `/api/bookmarks`

Creates a new bookmark with default status `INBOX`.

**Request Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "url": "https://example.com",           // Required, valid HTTP/HTTPS URL
  "title": "Example Website",             // Required, max 255 chars
  "tags": "dev,javascript,tutorial",      // Optional, comma-separated
  "notes": "Great resource for learning"  // Optional, any text
}
```

**Success Response: `201 Created`**
```json
{
  "id": 1,
  "url": "https://example.com",
  "title": "Example Website",
  "tags": "dev,javascript,tutorial",
  "notes": "Great resource for learning",
  "status": "INBOX",
  "createdAt": "2026-01-30T10:30:00Z",
  "updatedAt": "2026-01-30T10:30:00Z"
}
```

**Error Responses:**

**`400 Bad Request`** - Validation error
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid input data",
    "details": {
      "url": "URL cannot be empty",
      "title": "Title cannot exceed 255 characters"
    }
  }
}
```

**`409 Conflict`** - Duplicate URL (if UNIQUE constraint enabled)
```json
{
  "error": {
    "code": "DUPLICATE_URL",
    "message": "A bookmark with this URL already exists",
    "details": {
      "existingId": 5,
      "existingUrl": "https://example.com"
    }
  }
}
```

---

### 2. Get All Bookmarks (with filters)

**GET** `/api/bookmarks`

Retrieves bookmarks with optional search and filtering.

**Query Parameters:**
- `q` (string, optional) - Search in title, url, notes, tags
- `status` (string, optional) - Filter by status: `INBOX` or `DONE`
- `tag` (string, optional) - Filter by tag (matches any tag in comma-separated list)
- `limit` (integer, optional) - Max results, default 100
- `offset` (integer, optional) - Pagination offset, default 0
- `sort` (string, optional) - Sort field: `created_at`, `updated_at`, `title`. Default: `created_at`
- `order` (string, optional) - Sort order: `asc` or `desc`. Default: `desc`

**Example Requests:**
```
GET /api/bookmarks
GET /api/bookmarks?status=INBOX
GET /api/bookmarks?q=javascript
GET /api/bookmarks?tag=dev
GET /api/bookmarks?status=INBOX&tag=dev&sort=title&order=asc
GET /api/bookmarks?q=tutorial&limit=20&offset=0
```

**Success Response: `200 OK`**
```json
{
  "data": [
    {
      "id": 1,
      "url": "https://example.com",
      "title": "Example Website",
      "tags": "dev,javascript,tutorial",
      "notes": "Great resource for learning",
      "status": "INBOX",
      "createdAt": "2026-01-30T10:30:00Z",
      "updatedAt": "2026-01-30T10:30:00Z"
    },
    {
      "id": 2,
      "url": "https://github.com",
      "title": "GitHub",
      "tags": "dev,tools,git",
      "notes": "Code hosting platform",
      "status": "DONE",
      "createdAt": "2026-01-29T15:20:00Z",
      "updatedAt": "2026-01-30T09:15:00Z"
    }
  ],
  "meta": {
    "total": 2,
    "limit": 100,
    "offset": 0
  }
}
```

**Alternative Simple Response** (without wrapper):
```json
[
  { /* bookmark 1 */ },
  { /* bookmark 2 */ }
]
```

**Error Responses:**

**`400 Bad Request`** - Invalid query parameters
```json
{
  "error": {
    "code": "INVALID_PARAMETER",
    "message": "Invalid query parameter",
    "details": {
      "status": "Status must be INBOX or DONE",
      "sort": "Sort field must be one of: created_at, updated_at, title"
    }
  }
}
```

---

### 3. Get Single Bookmark

**GET** `/api/bookmarks/:id`

Retrieves a specific bookmark by ID.

**Path Parameters:**
- `id` (integer, required) - Bookmark ID

**Example Request:**
```
GET /api/bookmarks/1
```

**Success Response: `200 OK`**
```json
{
  "id": 1,
  "url": "https://example.com",
  "title": "Example Website",
  "tags": "dev,javascript,tutorial",
  "notes": "Great resource for learning",
  "status": "INBOX",
  "createdAt": "2026-01-30T10:30:00Z",
  "updatedAt": "2026-01-30T10:30:00Z"
}
```

**Error Responses:**

**`404 Not Found`** - Bookmark does not exist
```json
{
  "error": {
    "code": "NOT_FOUND",
    "message": "Bookmark not found with id: 1",
    "details": {
      "resourceType": "Bookmark",
      "id": 1
    }
  }
}
```

**`400 Bad Request`** - Invalid ID format
```json
{
  "error": {
    "code": "INVALID_ID",
    "message": "Invalid bookmark ID format",
    "details": {
      "id": "abc"
    }
  }
}
```

---

### 4. Update Bookmark (Full)

**PUT** `/api/bookmarks/:id`

Completely updates a bookmark (all fields required).

**Path Parameters:**
- `id` (integer, required) - Bookmark ID

**Request Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "url": "https://example.com",           // Required
  "title": "Updated Title",               // Required
  "tags": "dev,updated",                  // Required (can be empty string)
  "notes": "Updated notes",               // Required (can be empty string)
  "status": "DONE"                        // Required: INBOX or DONE
}
```

**Success Response: `200 OK`**
```json
{
  "id": 1,
  "url": "https://example.com",
  "title": "Updated Title",
  "tags": "dev,updated",
  "notes": "Updated notes",
  "status": "DONE",
  "createdAt": "2026-01-30T10:30:00Z",
  "updatedAt": "2026-01-30T11:45:00Z"
}
```

**Error Responses:**

**`404 Not Found`** - Bookmark does not exist
```json
{
  "error": {
    "code": "NOT_FOUND",
    "message": "Bookmark not found with id: 999",
    "details": {
      "resourceType": "Bookmark",
      "id": 999
    }
  }
}
```

**`400 Bad Request`** - Validation error
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid input data",
    "details": {
      "url": "Invalid URL format",
      "title": "Title cannot be empty",
      "status": "Status must be INBOX or DONE"
    }
  }
}
```

**`409 Conflict`** - URL conflict (if UNIQUE constraint)
```json
{
  "error": {
    "code": "DUPLICATE_URL",
    "message": "A bookmark with this URL already exists",
    "details": {
      "existingId": 5,
      "existingUrl": "https://example.com"
    }
  }
}
```

---

### 5. Update Bookmark Status (Partial)

**PATCH** `/api/bookmarks/:id/status`

Updates only the status field (workflow: INBOX → DONE or DONE → INBOX).

**Path Parameters:**
- `id` (integer, required) - Bookmark ID

**Request Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "status": "DONE"  // Required: INBOX or DONE
}
```

**Success Response: `200 OK`**
```json
{
  "id": 1,
  "url": "https://example.com",
  "title": "Example Website",
  "tags": "dev,javascript,tutorial",
  "notes": "Great resource for learning",
  "status": "DONE",
  "createdAt": "2026-01-30T10:30:00Z",
  "updatedAt": "2026-01-30T11:50:00Z"
}
```

**Error Responses:**

**`404 Not Found`** - Bookmark does not exist
```json
{
  "error": {
    "code": "NOT_FOUND",
    "message": "Bookmark not found with id: 1",
    "details": {
      "resourceType": "Bookmark",
      "id": 1
    }
  }
}
```

**`400 Bad Request`** - Invalid status value
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid status value",
    "details": {
      "status": "Status must be INBOX or DONE",
      "provided": "PENDING"
    }
  }
}
```

---

### 6. Delete Bookmark

**DELETE** `/api/bookmarks/:id`

Permanently deletes a bookmark.

**Path Parameters:**
- `id` (integer, required) - Bookmark ID

**Example Request:**
```
DELETE /api/bookmarks/1
```

**Success Response: `204 No Content`**
```
(empty body)
```

**Error Responses:**

**`404 Not Found`** - Bookmark does not exist
```json
{
  "error": {
    "code": "NOT_FOUND",
    "message": "Bookmark not found with id: 1",
    "details": {
      "resourceType": "Bookmark",
      "id": 1
    }
  }
}
```

**`400 Bad Request`** - Invalid ID format
```json
{
  "error": {
    "code": "INVALID_ID",
    "message": "Invalid bookmark ID format",
    "details": {
      "id": "abc"
    }
  }
}
```

---

## Status Codes Summary

| Code | Meaning | Usage |
|------|---------|-------|
| `200 OK` | Success | GET, PUT, PATCH |
| `201 Created` | Resource created | POST |
| `204 No Content` | Success, no body | DELETE |
| `400 Bad Request` | Validation error | Invalid input, missing fields |
| `404 Not Found` | Resource not found | GET/PUT/PATCH/DELETE non-existent ID |
| `409 Conflict` | Resource conflict | Duplicate URL (if UNIQUE constraint) |
| `500 Internal Server Error` | Server error | Unexpected errors |

---

## Error Code Reference

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `VALIDATION_ERROR` | 400 | Input validation failed |
| `INVALID_PARAMETER` | 400 | Invalid query parameter |
| `INVALID_ID` | 400 | Invalid ID format |
| `NOT_FOUND` | 404 | Resource not found |
| `DUPLICATE_URL` | 409 | URL already exists (if UNIQUE) |
| `INTERNAL_ERROR` | 500 | Unexpected server error |

---

## Validation Rules

| Field | Rule | Error Message |
|-------|------|---------------|
| `url` | Required, not empty | "URL cannot be empty" |
| `url` | Valid HTTP/HTTPS format | "Invalid URL format" |
| `url` | Valid URL syntax | "Invalid URL syntax" |
| `title` | Required, not empty | "Title cannot be empty" |
| `title` | Max 255 characters | "Title cannot exceed 255 characters" |
| `tags` | Optional, any string | - |
| `notes` | Optional, any string | - |
| `status` | Must be INBOX or DONE | "Status must be INBOX or DONE" |
| `id` | Must be valid integer | "Invalid bookmark ID format" |

---

## Query Parameter Validation

| Parameter | Valid Values | Error Message |
|-----------|--------------|---------------|
| `status` | `INBOX`, `DONE` | "Status must be INBOX or DONE" |
| `sort` | `created_at`, `updated_at`, `title` | "Sort field must be one of: created_at, updated_at, title" |
| `order` | `asc`, `desc` | "Order must be asc or desc" |
| `limit` | 1-1000 | "Limit must be between 1 and 1000" |
| `offset` | >= 0 | "Offset must be non-negative" |

---

## Example cURL Commands

```bash
# Create bookmark
curl -X POST http://localhost:7070/api/bookmarks \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://github.com",
    "title": "GitHub",
    "tags": "dev,tools",
    "notes": "Code hosting"
  }'

# Get all bookmarks
curl http://localhost:7070/api/bookmarks

# Search bookmarks
curl "http://localhost:7070/api/bookmarks?q=javascript&status=INBOX"

# Get single bookmark
curl http://localhost:7070/api/bookmarks/1

# Update bookmark
curl -X PUT http://localhost:7070/api/bookmarks/1 \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://github.com",
    "title": "GitHub - Updated",
    "tags": "dev,tools,git",
    "notes": "Updated notes",
    "status": "DONE"
  }'

# Update status only
curl -X PATCH http://localhost:7070/api/bookmarks/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "DONE"}'

# Delete bookmark
curl -X DELETE http://localhost:7070/api/bookmarks/1
```

---

## Example PowerShell Commands

```powershell
# Create bookmark
$body = @{
    url = "https://github.com"
    title = "GitHub"
    tags = "dev,tools"
    notes = "Code hosting"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:7070/api/bookmarks" `
    -Method POST -Body $body -ContentType "application/json"

# Get all bookmarks
Invoke-RestMethod -Uri "http://localhost:7070/api/bookmarks"

# Search with filter
Invoke-RestMethod -Uri "http://localhost:7070/api/bookmarks?q=javascript&status=INBOX"

# Get single bookmark
Invoke-RestMethod -Uri "http://localhost:7070/api/bookmarks/1"

# Update bookmark
$updateBody = @{
    url = "https://github.com"
    title = "GitHub - Updated"
    tags = "dev,tools,git"
    notes = "Updated notes"
    status = "DONE"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:7070/api/bookmarks/1" `
    -Method PUT -Body $updateBody -ContentType "application/json"

# Update status
$statusBody = @{ status = "DONE" } | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:7070/api/bookmarks/1/status" `
    -Method PATCH -Body $statusBody -ContentType "application/json"

# Delete bookmark
Invoke-RestMethod -Uri "http://localhost:7070/api/bookmarks/1" -Method DELETE
```

---

## Implementation Notes

### 1. Error Handling Best Practices
- Always return error object with consistent shape
- Include `code` for programmatic handling
- Include `message` for human readability
- Include `details` for validation errors (field-level)

### 2. Pagination Recommendation
```json
{
  "data": [ /* bookmarks */ ],
  "meta": {
    "total": 150,
    "limit": 20,
    "offset": 0,
    "hasNext": true,
    "hasPrev": false
  }
}
```

### 3. Search Implementation
Search query `q` should search in:
- `title` (case-insensitive)
- `url` (case-insensitive)
- `notes` (case-insensitive)
- `tags` (case-insensitive)

Use SQL:
```sql
WHERE (title LIKE '%query%' 
    OR url LIKE '%query%' 
    OR notes LIKE '%query%' 
    OR tags LIKE '%query%')
```

### 4. Tag Filtering
Tag filter `tag=dev` should match any bookmark where tags contain "dev":
```sql
WHERE tags LIKE '%dev%'
```

For multiple tags: `tag=dev,javascript` (match ANY):
```sql
WHERE tags LIKE '%dev%' OR tags LIKE '%javascript%'
```

### 5. CORS Headers (if needed for frontend)
```
Access-Control-Allow-Origin: *
Access-Control-Allow-Methods: GET, POST, PUT, PATCH, DELETE, OPTIONS
Access-Control-Allow-Headers: Content-Type
```

---

## Testing Checklist

- [ ] POST: Create bookmark with all fields
- [ ] POST: Create bookmark with minimal fields (only url, title)
- [ ] POST: Validation errors (empty url, invalid url, long title)
- [ ] GET: List all bookmarks
- [ ] GET: Search bookmarks (q parameter)
- [ ] GET: Filter by status (INBOX, DONE)
- [ ] GET: Filter by tag
- [ ] GET: Combined filters (status + tag + search)
- [ ] GET: Single bookmark (valid ID)
- [ ] GET: Single bookmark (invalid ID) → 404
- [ ] PUT: Update all fields
- [ ] PUT: Validation errors
- [ ] PUT: Non-existent ID → 404
- [ ] PATCH: Update status only (INBOX → DONE)
- [ ] PATCH: Invalid status → 400
- [ ] PATCH: Non-existent ID → 404
- [ ] DELETE: Valid ID → 204
- [ ] DELETE: Non-existent ID → 404
- [ ] DELETE: Invalid ID format → 400
