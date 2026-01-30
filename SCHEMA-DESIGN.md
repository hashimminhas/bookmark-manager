# SQLite Schema Design - Bookmark Manager

## 1. CREATE TABLE + Indexes

```sql
CREATE TABLE bookmarks (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    url TEXT NOT NULL,
    title TEXT NOT NULL,
    tags TEXT DEFAULT '',                           -- Comma-separated tags
    notes TEXT DEFAULT '',                          -- User notes/description
    status TEXT NOT NULL DEFAULT 'INBOX'
        CHECK (status IN ('INBOX', 'DONE')),       -- Enforce enum constraint
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    
    -- Optional: UNIQUE(url)
);

-- Performance indexes
CREATE INDEX idx_bookmarks_status ON bookmarks(status);
CREATE INDEX idx_bookmarks_created_at ON bookmarks(created_at DESC);
CREATE INDEX idx_bookmarks_tags ON bookmarks(tags);
CREATE INDEX idx_bookmarks_status_created ON bookmarks(status, created_at DESC);
```

## 2. Sample Data (5 Rows)

```sql
INSERT INTO bookmarks (url, title, tags, notes, status) VALUES
('https://github.com', 'GitHub', 'dev,tools,git', 
 'Code hosting and collaboration platform. Main workspace for all projects.', 'INBOX'),

('https://stackoverflow.com', 'Stack Overflow', 'dev,qa,programming', 
 'Q&A site for developers. Great for troubleshooting and learning.', 'INBOX'),

('https://developer.mozilla.org', 'MDN Web Docs', 'dev,javascript,docs,reference', 
 'Comprehensive web development documentation. Best resource for JS/CSS/HTML.', 'DONE'),

('https://www.postgresql.org/docs/', 'PostgreSQL Documentation', 'database,sql,docs', 
 'Official PostgreSQL documentation. Useful for advanced SQL queries.', 'INBOX'),

('https://martinfowler.com', 'Martin Fowler', 'architecture,blog,patterns', 
 'Software architecture blog. Great insights on design patterns and refactoring.', 'DONE');
```

## 3. UNIQUE Constraint on URL - Tradeoffs

### ✅ **WITH UNIQUE Constraint**: `UNIQUE(url)`

**Pros:**
- Prevents duplicate bookmarks automatically at database level
- Data integrity guaranteed
- Simple to implement

**Cons:**
- Cannot bookmark the same URL twice (even with different notes/tags)
- User might want to save same URL with different contexts
- Requires handling `UNIQUE constraint failed` errors in application code
- Update operations need special handling

**When to use:**
- Single-user applications where duplicates are truly unwanted
- Bookmarks serve as a "read later" list (one entry per article)
- Storage space is a concern

### ❌ **WITHOUT UNIQUE Constraint** (Recommended)

**Pros:**
- Flexibility: same URL can be saved multiple times with different notes/tags
- Better user experience: no confusing duplicate errors
- Easier to implement updates (no constraint violations)
- User controls duplicates via UI (show warning, not block)

**Cons:**
- Application must handle duplicate detection in code if desired
- Potential for accidental duplicates
- Slightly more storage space

**Recommendation:** **Don't use UNIQUE constraint**. Handle duplicates in the application layer:
- Show warning in UI: "You've already bookmarked this URL"
- Offer to edit existing bookmark or create new one
- Gives user control over their data

## 4. Index Strategy

| Index | Purpose | Query Pattern |
|-------|---------|---------------|
| `idx_bookmarks_status` | Filter by INBOX/DONE | `WHERE status = 'INBOX'` |
| `idx_bookmarks_created_at` | Sort by date | `ORDER BY created_at DESC` |
| `idx_bookmarks_tags` | Search by tags | `WHERE tags LIKE '%dev%'` |
| `idx_bookmarks_status_created` | Combined filter+sort | `WHERE status = 'INBOX' ORDER BY created_at` |

**Note on tags index:** Comma-separated tags have limited search efficiency. For production apps with heavy tag usage, consider a separate `tags` table with many-to-many relationship.

## 5. Simple Migration Strategy

### Approach 1: Versioned Schema Files (Recommended)

```
db/
├── schema.sql              # Current complete schema
├── migrations/
│   ├── 001_initial.sql    # Initial table creation
│   ├── 002_add_tags.sql   # Added tags column
│   └── 003_add_indexes.sql # Added performance indexes
└── README.md              # Migration instructions
```

**Migration Process:**
1. Each migration file is numbered sequentially
2. Track current version in a `schema_version` table:
   ```sql
   CREATE TABLE schema_version (
       version INTEGER PRIMARY KEY,
       applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   );
   ```
3. On startup, check current version and apply pending migrations
4. Simple Java code to run migrations:
   ```java
   public void migrate() {
       int currentVersion = getCurrentVersion();
       List<String> pendingMigrations = getMigrationsAfter(currentVersion);
       for (String migration : pendingMigrations) {
           executeSqlFile(migration);
           updateVersion(++currentVersion);
       }
   }
   ```

### Approach 2: Recreate on Schema Change (Simple Development)

```
db/
└── schema.sql             # Complete schema with DROP IF EXISTS
```

**Process:**
1. Use `DROP TABLE IF EXISTS` at the top of schema.sql
2. Recreate entire database on startup
3. **Warning:** This loses all data! Only for development/testing

**Implementation:**
```java
public void initializeDatabase() {
    String schema = readFile("db/schema.sql");
    executeSql(schema); // Drops and recreates tables
}
```

### Approach 3: ALTER TABLE for Production

For production with existing data:

```sql
-- Example migration: Add tags column to existing table
ALTER TABLE bookmarks ADD COLUMN tags TEXT DEFAULT '';

-- Example migration: Add index
CREATE INDEX IF NOT EXISTS idx_bookmarks_tags ON bookmarks(tags);
```

**Best Practice:**
- Use Approach 1 (versioned migrations) for production
- Use Approach 2 (recreate) for local development
- Always backup before migrations in production

## 6. Updated DatabaseInitializer.java

```java
public class DatabaseInitializer {
    public void initialize() {
        // Check if tables exist
        if (!tablesExist()) {
            // Run complete schema
            executeSqlFile("db/schema.sql");
        } else {
            // Run pending migrations
            runPendingMigrations();
        }
    }
    
    private void runPendingMigrations() {
        int currentVersion = getCurrentSchemaVersion();
        // Apply migrations newer than currentVersion
        // e.g., db/migrations/002_add_tags.sql
    }
}
```

## Summary

**File Location:** `db/schema.sql` (created)

**Key Decisions:**
- ✅ NO UNIQUE constraint on URL (user flexibility)
- ✅ 4 indexes for common queries (status, date, tags, combined)
- ✅ CHECK constraint on status enum
- ✅ Comma-separated tags (simple, good enough for most cases)
- ✅ Versioned migrations for production, recreate for dev

**Next Steps:**
1. Update `DatabaseInitializer.java` to use new schema
2. Update `Bookmark.java` model to include `tags` and `notes` fields
3. Update REST API to accept/return tags and notes
4. Update frontend UI to show/edit tags and notes
