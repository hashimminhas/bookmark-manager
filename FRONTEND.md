# Frontend Implementation Guide

## Overview
This document explains the minimal frontend implementation for the Bookmark Manager, how it integrates with the REST API, and how Javalin serves static files.

---

## File Structure

```
src/main/resources/public/
├── index.html      # Main HTML page with form and table
├── app.js          # JavaScript with fetch() API calls
└── styles.css      # CSS styling (responsive design)
```

---

## How Javalin Serves Static Files

### Configuration in Main.java

```java
// Create and configure Javalin app
Javalin app = Javalin.create(javalinConfig -> {
    // Static files for frontend
    javalinConfig.staticFiles.add("/public", Location.CLASSPATH);
    
    // ... other configuration
}).start(config.getServerPort());

// Root endpoint redirects to index.html
app.get("/", ctx -> ctx.redirect("/index.html"));
```

### How It Works

1. **Static File Location**: Files are placed in `src/main/resources/public/`
2. **CLASSPATH Location**: `Location.CLASSPATH` tells Javalin to look in the classpath (resources folder)
3. **URL Mapping**: Files are accessible at the root URL:
   - `http://localhost:8888/index.html` → serves `public/index.html`
   - `http://localhost:8888/app.js` → serves `public/app.js`
   - `http://localhost:8888/styles.css` → serves `public/styles.css`
4. **Root Redirect**: Accessing `http://localhost:8888/` redirects to `/index.html`

### Build Process

When you run `./gradlew build`:
1. Resources are copied to `build/resources/main/public/`
2. When running the JAR, files are served from inside the JAR file
3. During development (`./gradlew run`), files are served from the source directory

---

## Frontend Files

### 1. index.html

**Purpose**: Main HTML page with UI elements

**Key Features**:
- **Form Fields**: url (with auto-https), title (1-120 chars), tags (max 200), notes, status
- **Search & Filters**: Search box (q parameter), tag filter, status dropdown
- **Bookmarks List**: Dynamically rendered from API
- **Actions**: Edit, Delete, Mark Done/Inbox buttons

**Form Validation**:
```html
<input type="text" id="url" required placeholder="example.com or https://example.com" />
<input type="text" id="title" required maxlength="120" />
<input type="text" id="tags" placeholder="java, spring, tutorial" maxlength="200" />
```

---

### 2. app.js

**Purpose**: JavaScript to interact with REST API using fetch()

**Key Functions**:

#### Load Bookmarks with Filters
```javascript
async function loadBookmarks() {
    const params = new URLSearchParams();
    const search = searchInput.value.trim();
    const status = filterStatus.value;
    const tag = tagFilter.value.trim();
    
    if (search) params.append('q', search);
    if (status) params.append('status', status);
    if (tag) params.append('tag', tag);
    
    const url = params.toString() ? `${API_URL}?${params}` : API_URL;
    const response = await fetch(url);
    bookmarks = await response.json();
    renderBookmarks();
}
```

#### Create Bookmark
```javascript
async function handleSubmit(e) {
    e.preventDefault();
    const data = {
        url: document.getElementById('url').value.trim(),
        title: document.getElementById('title').value.trim(),
        tags: document.getElementById('tags').value.trim(),
        notes: document.getElementById('notes').value.trim()
    };
    
    const response = await fetch(API_URL, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    });
}
```

#### Update Bookmark
```javascript
// PUT for full update
const response = await fetch(`${API_URL}/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ url, title, tags, notes, status })
});
```

#### Toggle Status (PATCH)
```javascript
async function toggleStatus(id, currentStatus) {
    const newStatus = currentStatus === 'INBOX' ? 'DONE' : 'INBOX';
    const response = await fetch(`${API_URL}/${id}/status`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status: newStatus })
    });
}
```

#### Delete Bookmark
```javascript
async function deleteBookmark(id) {
    if (!confirm('Are you sure?')) return;
    await fetch(`${API_URL}/${id}`, { method: 'DELETE' });
    loadBookmarks();
}
```

**Security Features**:
- XSS Prevention: All user input is escaped using `escapeHtml()` before rendering
- Input validation before API calls
- HTTPS link normalization hint in UI

---

### 3. styles.css

**Purpose**: Responsive, modern styling

**Key Features**:
- CSS Variables for theming
- Responsive design (mobile-friendly)
- Card-based layout for bookmarks
- Status badges (Inbox = yellow, Done = green)
- Toast notifications (success, error, info)
- Smooth animations and transitions

**Color Scheme**:
```css
--primary-color: #3b82f6;     /* Blue */
--success-color: #10b981;     /* Green */
--danger-color: #ef4444;      /* Red */
--warning-color: #f59e0b;     /* Orange */
```

---

## API Integration

### Endpoints Used by Frontend

| Method | Endpoint | Purpose | Request Body |
|--------|----------|---------|--------------|
| GET | `/api/bookmarks` | List all bookmarks | - |
| GET | `/api/bookmarks?q=search&status=INBOX&tag=java` | Filtered list | - |
| GET | `/api/bookmarks/:id` | Get single bookmark | - |
| POST | `/api/bookmarks` | Create bookmark | `{url, title, tags, notes}` |
| PUT | `/api/bookmarks/:id` | Update bookmark | `{url, title, tags, notes, status}` |
| PATCH | `/api/bookmarks/:id/status` | Update status only | `{status}` |
| DELETE | `/api/bookmarks/:id` | Delete bookmark | - |

### Error Handling

All API calls handle errors consistently:

```javascript
try {
    const response = await fetch(url);
    
    if (!response.ok) {
        const error = await response.json();
        throw new Error(error.error?.message || 'Request failed');
    }
    
    const data = await response.json();
    // Handle success
    
} catch (error) {
    showToast('Error: ' + error.message, 'error');
}
```

---

## User Experience Features

### 1. Real-time Search & Filtering
- **Debounced search**: 300ms delay to avoid excessive API calls
- **Multiple filters**: Search query + status + tag (combined)
- **Auto-refresh**: Results update immediately on filter change

### 2. Form Management
- **Add Mode**: Status hidden (defaults to INBOX on backend)
- **Edit Mode**: Status dropdown visible, pre-filled with current values
- **Client-side validation**: Length checks before API call
- **Loading states**: Button shows "Adding..." or "Updating..." during request

### 3. Bookmark Cards
- **Visual status**: Color-coded left border (yellow=INBOX, green=DONE)
- **Tag display**: Comma-separated tags rendered as badges
- **Notes preview**: Displayed in gray box if present
- **Timestamp**: Shows created/updated dates
- **Quick actions**: Toggle status, edit, delete buttons

### 4. Notifications
- **Toast messages**: Bottom-right corner notifications
- **Auto-dismiss**: Disappear after 3 seconds
- **Color-coded**: Green=success, Red=error, Blue=info

---

## Development Workflow

### Running Locally

```bash
# Terminal 1: Start backend
./gradlew run

# Access frontend at:
# http://localhost:8888/
```

### Making Changes

1. **HTML Changes**: Edit `src/main/resources/public/index.html`
2. **JavaScript Changes**: Edit `src/main/resources/public/app.js`
3. **CSS Changes**: Edit `src/main/resources/public/styles.css`
4. **Rebuild**: `./gradlew build` (or let Gradle watch auto-rebuild)
5. **Refresh browser**: Changes are live

### Testing API Calls

Open browser DevTools (F12) → Network tab to see:
- All fetch() requests
- Request/response headers
- Response bodies
- Error messages

---

## Production Deployment

### Building for Production

```bash
# Build JAR with all static files
./gradlew clean build

# JAR location
build/libs/bookmark-manager.jar
```

### Running Production JAR

```bash
java -jar build/libs/bookmark-manager.jar

# With environment variables
export PORT=8080
export DB_URL="jdbc:sqlite:/data/bookmarks.db"
java -jar build/libs/bookmark-manager.jar
```

### Docker Deployment

Static files are automatically included in the Docker image:

```dockerfile
# Dockerfile already includes:
COPY build/libs/*.jar app.jar
# This JAR contains src/main/resources/public/* files
```

---

## Security Considerations

### 1. XSS Prevention
```javascript
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Used in rendering:
${escapeHtml(bookmark.title)}
${escapeHtml(bookmark.url)}
${escapeHtml(bookmark.tags)}
${escapeHtml(bookmark.notes)}
```

### 2. CORS Configuration
```java
// In Main.java
app.before(ctx -> {
    ctx.header("Access-Control-Allow-Origin", "*");
    ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
    ctx.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
});
```

### 3. Input Validation
- Client-side: Length checks, required fields
- Server-side: Complete validation in ValidationUtils.java
- URL normalization: Auto-adds https:// on backend

---

## Browser Compatibility

**Tested and working on**:
- Chrome 90+
- Firefox 88+
- Safari 14+
- Edge 90+

**Required Features**:
- ES6+ (arrow functions, async/await, template literals)
- Fetch API
- CSS Grid & Flexbox

---

## Troubleshooting

### Issue: Static files not loading

**Solution**:
1. Check files are in `src/main/resources/public/`
2. Rebuild: `./gradlew clean build`
3. Verify Javalin config: `javalinConfig.staticFiles.add("/public", Location.CLASSPATH)`

### Issue: API calls fail with CORS error

**Solution**:
1. Check CORS headers in Main.java
2. Verify `app.before()` is called before routes
3. Add OPTIONS handler: `app.options("/*", ctx -> ctx.status(204))`

### Issue: 404 on index.html

**Solution**:
1. Check root redirect: `app.get("/", ctx -> ctx.redirect("/index.html"))`
2. Access directly: `http://localhost:8888/index.html`

### Issue: Changes not reflecting

**Solution**:
1. Hard refresh browser: Ctrl+Shift+R (Windows/Linux) or Cmd+Shift+R (Mac)
2. Clear browser cache
3. Rebuild: `./gradlew clean build`

---

## File Contents Summary

### index.html (87 lines)
- Semantic HTML5 structure
- Form with 5 fields: url, title, tags, notes, status
- Search box and 2 filter inputs
- Container for dynamic bookmark list
- Toast notification element

### app.js (345 lines)
- No framework dependencies (vanilla JavaScript)
- Uses fetch() API for all HTTP requests
- Handles all CRUD operations + PATCH for status
- Real-time filtering with debounce
- XSS protection via escapeHtml()
- Toast notifications

### styles.css (435 lines)
- CSS Variables for theming
- Responsive design (mobile-first)
- Card-based bookmark layout
- Status badges with colors
- Smooth animations
- Toast notification styles

---

## Summary

✅ **Simple**: No build tools, no frameworks, just HTML/CSS/JS  
✅ **Complete**: Full CRUD + search + filters + status toggle  
✅ **Secure**: XSS prevention, input validation, CORS configured  
✅ **Responsive**: Works on desktop, tablet, and mobile  
✅ **Production-ready**: Served from classpath, included in JAR  

Access your bookmark manager at: **http://localhost:8888/**
