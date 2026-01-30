# Validation Rules & URL Normalization

## Overview
Enhanced validation with automatic URL normalization, field sanitization, and comprehensive error responses.

---

## Validation Rules

### 1. URL Validation & Normalization

**Rules:**
- ✅ Accept `http://` and `https://` schemes only
- ✅ Auto-add `https://` if no scheme present
- ✅ Trim whitespace and collapse multiple spaces
- ❌ Reject unsupported schemes (ftp://, file://, etc.)
- ❌ Reject empty or invalid URLs

**Normalization Examples:**
```
Input                      → Output
─────────────────────────────────────────────────────
"example.com"             → "https://example.com"
"  example.com  "         → "https://example.com"
"http://example.com"      → "http://example.com"
"https://example.com"     → "https://example.com"
"example.com/path"        → "https://example.com/path"
"  https://  example.com" → "https:// example.com" (invalid, throws error)
```

**Implementation:**
```java
// UrlValidator.java
String normalized = UrlValidator.normalizeAndValidate(userInput);
// Returns: normalized URL or throws ValidationException
```

---

### 2. Title Validation

**Rules:**
- ✅ Length: 1-120 characters (after trimming)
- ✅ Trim whitespace
- ✅ Collapse multiple spaces to single space
- ❌ Reject empty titles
- ❌ Reject titles > 120 chars

**Examples:**
```
Input                          → Output
──────────────────────────────────────────────────────────
"  My Bookmark  "             → "My Bookmark"
"Example    with   spaces"    → "Example with spaces"
"A"                           → "A"
""                            → ValidationException: "Title cannot be empty"
"x".repeat(121)               → ValidationException: "Title cannot exceed 120 characters"
```

**Implementation:**
```java
// ValidationUtils.java
String normalized = ValidationUtils.validateAndNormalizeTitle(title);
```

---

### 3. Tags Validation

**Rules:**
- ✅ Max length: 200 characters total
- ✅ Trim whitespace
- ✅ Collapse multiple spaces
- ✅ Empty tags allowed
- ❌ Reject tags > 200 chars

**Examples:**
```
Input                              → Output
─────────────────────────────────────────────────────────
"  java, spring, tutorial  "      → "java, spring, tutorial"
"work,  personal,  important"     → "work, personal, important"
""                                → ""
null                              → ""
"tag1, tag2, ...".length(201)     → ValidationException: "Tags cannot exceed 200 characters"
```

**Implementation:**
```java
String normalized = ValidationUtils.validateAndNormalizeTags(tags);
```

---

### 4. Notes Validation

**Rules:**
- ✅ Max length: 2000 characters
- ✅ Trim whitespace
- ✅ Empty notes allowed
- ❌ Reject notes > 2000 chars

**Examples:**
```
Input                           → Output
────────────────────────────────────────────────────────
"  Some notes here  "          → "Some notes here"
""                             → ""
null                           → ""
"x".repeat(2001)               → ValidationException: "Notes cannot exceed 2000 characters"
```

---

### 5. Status Validation

**Rules:**
- ✅ Must be exactly "INBOX" or "DONE" (case-insensitive)
- ❌ Reject any other value
- ❌ Reject empty status

**Examples:**
```
Input       → Output
──────────────────────────
"INBOX"    → Valid
"DONE"     → Valid
"inbox"    → Valid (normalized to "INBOX")
"done"     → Valid (normalized to "DONE")
""         → ValidationException: "Status cannot be empty"
"PENDING"  → ValidationException: "Status must be INBOX or DONE"
```

---

### 6. Pagination Parameters

**Rules:**
- `limit`: Must be positive integer (1-1000), default: 100
- `offset`: Must be non-negative integer, default: 0

**Examples:**
```
Input               → Result
───────────────────────────────────────────
limit=50           → Valid, use 50
limit=1500         → Valid, capped at 1000
limit=-10          → ValidationException: "limit must be positive"
offset=0           → Valid
offset=100         → Valid
offset=-5          → ValidationException: "offset must be non-negative"
```

---

## Integration in Service Layer

### Before (Old Approach):
```java
public Bookmark createBookmark(CreateBookmarkRequest request) {
    validateCreateRequest(request);  // Manual validation
    
    Bookmark bookmark = new Bookmark();
    bookmark.setUrl(request.getUrl());  // No normalization
    bookmark.setTitle(request.getTitle());
    // ...
}
```

### After (New Approach):
```java
public Bookmark createBookmark(CreateBookmarkRequest request) {
    // Validate and normalize in one step
    String normalizedUrl = UrlValidator.normalizeAndValidate(request.getUrl());
    String normalizedTitle = ValidationUtils.validateAndNormalizeTitle(request.getTitle());
    String normalizedTags = ValidationUtils.validateAndNormalizeTags(request.getTags());
    String normalizedNotes = ValidationUtils.validateAndNormalizeNotes(request.getNotes());
    
    Bookmark bookmark = new Bookmark();
    bookmark.setUrl(normalizedUrl);
    bookmark.setTitle(normalizedTitle);
    bookmark.setTags(normalizedTags);
    bookmark.setNotes(normalizedNotes);
    bookmark.setStatus(BookmarkStatus.INBOX);
    
    return bookmarkRepository.create(bookmark);
}
```

---

## Error Response Format

All validation errors return HTTP 400 with this JSON structure:

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Detailed error message here",
    "details": {}
  }
}
```

---

## Invalid Input Examples & Expected Errors

### Example 1: Empty URL
**Request:**
```bash
POST /api/bookmarks
{
  "url": "",
  "title": "My Bookmark"
}
```

**Response:** `400 Bad Request`
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "URL cannot be empty",
    "details": {}
  }
}
```

---

### Example 2: Unsupported URL Scheme
**Request:**
```bash
POST /api/bookmarks
{
  "url": "ftp://files.example.com",
  "title": "FTP Site"
}
```

**Response:** `400 Bad Request`
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "URL must use HTTP or HTTPS protocol, got: ftp",
    "details": {}
  }
}
```

---

### Example 3: Title Too Long
**Request:**
```bash
POST /api/bookmarks
{
  "url": "example.com",
  "title": "This is an extremely long title that exceeds the maximum allowed length of one hundred and twenty characters which will cause a validation error"
}
```

**Response:** `400 Bad Request`
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Title cannot exceed 120 characters (got 145)",
    "details": {}
  }
}
```

---

### Example 4: Empty Title
**Request:**
```bash
POST /api/bookmarks
{
  "url": "example.com",
  "title": "   "
}
```

**Response:** `400 Bad Request`
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Title cannot be empty",
    "details": {}
  }
}
```

---

### Example 5: Tags Too Long
**Request:**
```bash
POST /api/bookmarks
{
  "url": "example.com",
  "title": "Example",
  "tags": "tag1, tag2, tag3, tag4, tag5, tag6, tag7, tag8, tag9, tag10, tag11, tag12, tag13, tag14, tag15, tag16, tag17, tag18, tag19, tag20, tag21, tag22, tag23, tag24, tag25, tag26, tag27, tag28, tag29, tag30"
}
```

**Response:** `400 Bad Request`
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Tags cannot exceed 200 characters (got 234)",
    "details": {}
  }
}
```

---

### Example 6: Invalid Status
**Request:**
```bash
PATCH /api/bookmarks/1/status
{
  "status": "PENDING"
}
```

**Response:** `400 Bad Request`
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Status must be INBOX or DONE, got: PENDING",
    "details": {}
  }
}
```

---

### Example 7: Invalid URL Format
**Request:**
```bash
POST /api/bookmarks
{
  "url": "not a valid url at all",
  "title": "Invalid"
}
```

**Response:** `400 Bad Request`
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid URL format: no protocol: https://not a valid url at all",
    "details": {}
  }
}
```

---

### Example 8: Negative Pagination Parameter
**Request:**
```bash
GET /api/bookmarks?limit=-10
```

**Response:** `400 Bad Request`
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "limit must be positive, got: -10",
    "details": {}
  }
}
```

---

### Example 9: Invalid Sort Field
**Request:**
```bash
GET /api/bookmarks?sort=invalid_field
```

**Response:** `400 Bad Request`
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Sort field must be one of: created_at, updated_at, title",
    "details": {}
  }
}
```

---

## Valid Input Examples (Success Cases)

### Example 1: URL Normalization Success
**Request:**
```bash
POST /api/bookmarks
{
  "url": "example.com",
  "title": "Example Site"
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "url": "https://example.com",
  "title": "Example Site",
  "tags": "",
  "notes": "",
  "status": "INBOX",
  "createdAt": "2026-01-30T10:00:00",
  "updatedAt": "2026-01-30T10:00:00"
}
```

---

### Example 2: Whitespace Normalization
**Request:**
```bash
POST /api/bookmarks
{
  "url": "  example.com  ",
  "title": "  Multiple    Spaces   Here  ",
  "tags": "  java,  spring,  tutorial  "
}
```

**Response:** `201 Created`
```json
{
  "id": 2,
  "url": "https://example.com",
  "title": "Multiple Spaces Here",
  "tags": "java, spring, tutorial",
  "notes": "",
  "status": "INBOX",
  "createdAt": "2026-01-30T10:01:00",
  "updatedAt": "2026-01-30T10:01:00"
}
```

---

## Testing the Validation

### Using curl:

```bash
# Test URL normalization
curl -X POST http://localhost:8888/api/bookmarks \
  -H "Content-Type: application/json" \
  -d '{"url": "example.com", "title": "Test"}'

# Test empty title
curl -X POST http://localhost:8888/api/bookmarks \
  -H "Content-Type: application/json" \
  -d '{"url": "example.com", "title": ""}'

# Test title too long
curl -X POST http://localhost:8888/api/bookmarks \
  -H "Content-Type: application/json" \
  -d '{"url": "example.com", "title": "'$(printf 'x%.0s' {1..121})'"}'

# Test invalid status
curl -X PATCH http://localhost:8888/api/bookmarks/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "PENDING"}'

# Test negative pagination
curl "http://localhost:8888/api/bookmarks?limit=-10"
```

---

## Validation Utilities Summary

### UrlValidator.java
```java
// Normalize and validate URL (adds https:// if needed)
String normalized = UrlValidator.normalizeAndValidate(url);

// Just validate (backward compatibility)
UrlValidator.validate(url);

// Check if valid (returns boolean)
boolean valid = UrlValidator.isValid(url);
```

### ValidationUtils.java
```java
// Title validation (1-120 chars)
String title = ValidationUtils.validateAndNormalizeTitle(input);

// Tags validation (max 200 chars)
String tags = ValidationUtils.validateAndNormalizeTags(input);

// Notes validation (max 2000 chars)
String notes = ValidationUtils.validateAndNormalizeNotes(input);

// Status validation (INBOX or DONE)
ValidationUtils.validateStatus(status);

// Pagination validation
ValidationUtils.validatePositive(limit, "limit");
ValidationUtils.validateNonNegative(offset, "offset");
```

---

## Constants

```java
// From ValidationUtils.java
TITLE_MIN_LENGTH = 1
TITLE_MAX_LENGTH = 120
TAGS_MAX_LENGTH = 200
NOTES_MAX_LENGTH = 2000

// Pagination limits (from BookmarkService.java)
DEFAULT_LIMIT = 100
MAX_LIMIT = 1000
DEFAULT_OFFSET = 0
```
