-- ============================================
-- Bookmark Manager - SQLite Database Schema
-- ============================================

-- Drop existing table if needed (for clean migrations)
DROP TABLE IF EXISTS bookmarks;

-- Create bookmarks table
CREATE TABLE bookmarks (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    url TEXT NOT NULL,
    title TEXT NOT NULL,
    tags TEXT DEFAULT '',                           -- Comma-separated tags (e.g., "dev,javascript,tutorial")
    notes TEXT DEFAULT '',                          -- User notes/description
    status TEXT NOT NULL DEFAULT 'INBOX'           -- INBOX or DONE
        CHECK (status IN ('INBOX', 'DONE')),       -- Enforce enum constraint
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Optional: Uncomment for UNIQUE url constraint
    -- UNIQUE(url)
);

-- ============================================
-- Indexes for Performance
-- ============================================

-- Index on status for filtering (INBOX vs DONE)
CREATE INDEX idx_bookmarks_status ON bookmarks(status);

-- Index on created_at for sorting by date
CREATE INDEX idx_bookmarks_created_at ON bookmarks(created_at DESC);

-- Index on tags for tag-based searches
-- Note: Full-text search on comma-separated tags is limited
-- For better tag search, consider a separate tags table with many-to-many relationship
CREATE INDEX idx_bookmarks_tags ON bookmarks(tags);

-- Composite index for common query pattern (status + created_at)
CREATE INDEX idx_bookmarks_status_created ON bookmarks(status, created_at DESC);

-- Optional: Full-text search index for title and notes
-- Uncomment if you need advanced text search capabilities
-- CREATE VIRTUAL TABLE bookmarks_fts USING fts5(title, notes, content=bookmarks, content_rowid=id);

-- ============================================
-- Sample Data
-- ============================================

INSERT INTO bookmarks (url, title, tags, notes, status) VALUES
('https://github.com', 'GitHub', 'dev,tools,git', 'Code hosting and collaboration platform. Main workspace for all projects.', 'INBOX'),
('https://stackoverflow.com', 'Stack Overflow', 'dev,qa,programming', 'Q&A site for developers. Great for troubleshooting and learning.', 'INBOX'),
('https://developer.mozilla.org', 'MDN Web Docs', 'dev,javascript,docs,reference', 'Comprehensive web development documentation. Best resource for JS/CSS/HTML.', 'DONE'),
('https://www.postgresql.org/docs/', 'PostgreSQL Documentation', 'database,sql,docs', 'Official PostgreSQL documentation. Useful for advanced SQL queries.', 'INBOX'),
('https://martinfowler.com', 'Martin Fowler', 'architecture,blog,patterns', 'Software architecture blog. Great insights on design patterns and refactoring.', 'DONE');

-- ============================================
-- Verification Queries
-- ============================================

-- Count bookmarks by status
-- SELECT status, COUNT(*) FROM bookmarks GROUP BY status;

-- Search by tag
-- SELECT * FROM bookmarks WHERE tags LIKE '%dev%';

-- Get recent INBOX items
-- SELECT * FROM bookmarks WHERE status = 'INBOX' ORDER BY created_at DESC LIMIT 10;
