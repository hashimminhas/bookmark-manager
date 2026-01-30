// API Base URL
const API_URL = '/api/bookmarks';

// State
let bookmarks = [];
let isEditing = false;
let editingId = null;

// DOM Elements
const searchInput = document.getElementById('searchInput');
const tagFilter = document.getElementById('tagFilter');
const filterStatus = document.getElementById('filterStatus');
const addBookmarkBtn = document.getElementById('addBookmarkBtn');
const bookmarkFormContainer = document.getElementById('bookmarkFormContainer');
const bookmarkForm = document.getElementById('bookmarkForm');
const bookmarksList = document.getElementById('bookmarksList');
const cancelBtn = document.getElementById('cancelBtn');
const formTitle = document.getElementById('formTitle');
const statusGroup = document.getElementById('statusGroup');
const submitBtn = document.getElementById('submitBtn');

// Event Listeners
document.addEventListener('DOMContentLoaded', () => {
    loadBookmarks();
    
    searchInput.addEventListener('input', debounce(handleFilter, 300));
    tagFilter.addEventListener('input', debounce(handleFilter, 300));
    filterStatus.addEventListener('change', handleFilter);
    addBookmarkBtn.addEventListener('click', showAddForm);
    bookmarkForm.addEventListener('submit', handleSubmit);
    cancelBtn.addEventListener('click', hideForm);
});

// Load bookmarks with filters
async function loadBookmarks() {
    try {
        showLoading();
        
        // Build query params
        const params = new URLSearchParams();
        const search = searchInput.value.trim();
        const status = filterStatus.value;
        const tag = tagFilter.value.trim();
        
        if (search) params.append('q', search);
        if (status) params.append('status', status);
        if (tag) params.append('tag', tag);
        
        const queryString = params.toString();
        const url = queryString ? `${API_URL}?${queryString}` : API_URL;
        
        const response = await fetch(url);
        
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error?.message || 'Failed to fetch bookmarks');
        }
        
        bookmarks = await response.json();
        renderBookmarks();
    } catch (error) {
        showToast('Error: ' + error.message, 'error');
        bookmarksList.innerHTML = '<p class="error-message">Failed to load bookmarks</p>';
    }
}

// Handle filter changes
function handleFilter() {
    loadBookmarks();
}

// Render bookmarks
function renderBookmarks() {
    if (bookmarks.length === 0) {
        bookmarksList.innerHTML = '<p class="empty-state">No bookmarks found. Add your first bookmark!</p>';
        return;
    }
    
    const html = bookmarks.map(bookmark => `
        <div class="bookmark-card ${bookmark.status.toLowerCase()}">
            <div class="bookmark-header">
                <h3 class="bookmark-title">
                    <a href="${escapeHtml(bookmark.url)}" target="_blank" rel="noopener noreferrer">
                        ${escapeHtml(bookmark.title)}
                    </a>
                </h3>
                <span class="bookmark-status status-${bookmark.status.toLowerCase()}">${bookmark.status}</span>
            </div>
            
            <div class="bookmark-url">
                <span class="url-icon">🔗</span>
                <a href="${escapeHtml(bookmark.url)}" target="_blank" rel="noopener noreferrer">
                    ${escapeHtml(bookmark.url)}
                </a>
            </div>
            
            ${bookmark.tags ? `
                <div class="bookmark-tags">
                    ${bookmark.tags.split(',').map(tag => 
                        `<span class="tag">${escapeHtml(tag.trim())}</span>`
                    ).join('')}
                </div>
            ` : ''}
            
            ${bookmark.notes ? `
                <div class="bookmark-notes">
                    <p>${escapeHtml(bookmark.notes)}</p>
                </div>
            ` : ''}
            
            <div class="bookmark-meta">
                <small>Created: ${formatDate(bookmark.createdAt)}</small>
                ${bookmark.updatedAt !== bookmark.createdAt ? 
                    `<small>Updated: ${formatDate(bookmark.updatedAt)}</small>` : ''}
            </div>
            
            <div class="bookmark-actions">
                <button onclick="toggleStatus(${bookmark.id}, '${bookmark.status}')" class="btn-action">
                    ${bookmark.status === 'INBOX' ? '✓ Mark Done' : '↶ Move to Inbox'}
                </button>
                <button onclick="editBookmark(${bookmark.id})" class="btn-action">✎ Edit</button>
                <button onclick="deleteBookmark(${bookmark.id})" class="btn-danger">🗑 Delete</button>
            </div>
        </div>
    `).join('');
    
    bookmarksList.innerHTML = html;
}

// Show add form
function showAddForm() {
    isEditing = false;
    editingId = null;
    formTitle.textContent = 'Add New Bookmark';
    submitBtn.textContent = 'Add Bookmark';
    statusGroup.style.display = 'none';
    bookmarkForm.reset();
    bookmarkFormContainer.classList.remove('hidden');
    document.getElementById('url').focus();
}

// Show edit form
function editBookmark(id) {
    const bookmark = bookmarks.find(b => b.id === id);
    if (!bookmark) return;
    
    isEditing = true;
    editingId = id;
    formTitle.textContent = 'Edit Bookmark';
    submitBtn.textContent = 'Update Bookmark';
    statusGroup.style.display = 'block';
    
    document.getElementById('bookmarkId').value = bookmark.id;
    document.getElementById('url').value = bookmark.url;
    document.getElementById('title').value = bookmark.title;
    document.getElementById('tags').value = bookmark.tags || '';
    document.getElementById('notes').value = bookmark.notes || '';
    document.getElementById('status').value = bookmark.status;
    
    bookmarkFormContainer.classList.remove('hidden');
    bookmarkFormContainer.scrollIntoView({ behavior: 'smooth' });
    document.getElementById('url').focus();
}

// Hide form
function hideForm() {
    bookmarkFormContainer.classList.add('hidden');
    bookmarkForm.reset();
    isEditing = false;
    editingId = null;
}

// Handle form submit
async function handleSubmit(e) {
    e.preventDefault();
    
    const url = document.getElementById('url').value.trim();
    const title = document.getElementById('title').value.trim();
    const tags = document.getElementById('tags').value.trim();
    const notes = document.getElementById('notes').value.trim();
    const status = document.getElementById('status').value;
    
    // Client-side validation
    if (!url || !title) {
        showToast('URL and Title are required', 'error');
        return;
    }
    
    if (title.length > 120) {
        showToast('Title cannot exceed 120 characters', 'error');
        return;
    }
    
    if (tags.length > 200) {
        showToast('Tags cannot exceed 200 characters', 'error');
        return;
    }
    
    const data = { url, title, tags, notes };
    
    try {
        submitBtn.disabled = true;
        submitBtn.textContent = isEditing ? 'Updating...' : 'Adding...';
        
        let response;
        if (isEditing) {
            // PUT request for update
            data.status = status;
            response = await fetch(`${API_URL}/${editingId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });
        } else {
            // POST request for create
            response = await fetch(API_URL, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });
        }
        
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error?.message || 'Failed to save bookmark');
        }
        
        showToast(isEditing ? 'Bookmark updated!' : 'Bookmark added!', 'success');
        hideForm();
        loadBookmarks();
        
    } catch (error) {
        showToast('Error: ' + error.message, 'error');
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = isEditing ? 'Update Bookmark' : 'Add Bookmark';
    }
}

// Toggle bookmark status
async function toggleStatus(id, currentStatus) {
    const newStatus = currentStatus === 'INBOX' ? 'DONE' : 'INBOX';
    
    try {
        const response = await fetch(`${API_URL}/${id}/status`, {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ status: newStatus })
        });
        
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error?.message || 'Failed to update status');
        }
        
        showToast(`Status updated to ${newStatus}`, 'success');
        loadBookmarks();
        
    } catch (error) {
        showToast('Error: ' + error.message, 'error');
    }
}

// Delete bookmark
async function deleteBookmark(id) {
    if (!confirm('Are you sure you want to delete this bookmark?')) {
        return;
    }
    
    try {
        const response = await fetch(`${API_URL}/${id}`, {
            method: 'DELETE'
        });
        
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error?.message || 'Failed to delete bookmark');
        }
        
        showToast('Bookmark deleted', 'success');
        loadBookmarks();
        
    } catch (error) {
        showToast('Error: ' + error.message, 'error');
    }
}

// Show loading state
function showLoading() {
    bookmarksList.innerHTML = '<p class="loading">Loading bookmarks...</p>';
}

// Show toast notification
function showToast(message, type = 'info') {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.className = `toast ${type}`;
    toast.classList.remove('hidden');
    
    setTimeout(() => {
        toast.classList.add('hidden');
    }, 3000);
}

// Format date
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

// Escape HTML to prevent XSS
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Debounce function
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}


// Event Listeners
document.addEventListener('DOMContentLoaded', () => {
    loadBookmarks();
    
    searchInput.addEventListener('input', debounce(handleSearch, 300));
    filterStatus.addEventListener('change', handleFilter);
    addBookmarkBtn.addEventListener('click', showAddForm);
    bookmarkForm.addEventListener('submit', handleSubmit);
    cancelBtn.addEventListener('click', hideForm);
});

// Load bookmarks from API
async function loadBookmarks(queryParams = '') {
    try {
        showLoading();
        const response = await fetch(`${API_URL}${queryParams}`);
        
        if (!response.ok) {
            throw new Error('Failed to fetch bookmarks');
        }
        
        bookmarks = await response.json();
        renderBookmarks();
    } catch (error) {
        showToast('Error loading bookmarks: ' + error.message, 'error');
        bookmarksList.innerHTML = '<p class="loading">Failed to load bookmarks</p>';
    }
}

// Render bookmarks
function renderBookmarks() {
    if (bookmarks.length === 0) {
        bookmarksList.innerHTML = `
            <div class="empty-state">
                <h3>No bookmarks found</h3>
                <p>Start by adding your first bookmark!</p>
            </div>
        `;
        return;
    }
    
    bookmarksList.innerHTML = bookmarks.map(bookmark => `
        <div class="bookmark-card" data-id="${bookmark.id}">
            <div class="bookmark-header">
                <div>
                    <h3 class="bookmark-title">${escapeHtml(bookmark.title)}</h3>
                    <a href="${escapeHtml(bookmark.url)}" target="_blank" rel="noopener noreferrer" class="bookmark-url">
                        ${escapeHtml(bookmark.url)}
                    </a>
                    ${bookmark.description ? `<p class="bookmark-description">${escapeHtml(bookmark.description)}</p>` : ''}
                </div>
            </div>
            <div class="bookmark-meta">
                <div>
                    <span class="bookmark-status status-${bookmark.status}">${bookmark.status}</span>
                    <span class="bookmark-date">${formatDate(bookmark.createdAt)}</span>
                </div>
                <div class="bookmark-actions">
                    <button class="btn-edit" onclick="editBookmark(${bookmark.id})">Edit</button>
                    <button class="btn-delete" onclick="deleteBookmark(${bookmark.id})">Delete</button>
                </div>
            </div>
        </div>
    `).join('');
}

// Show add form
function showAddForm() {
    isEditing = false;
    editingId = null;
    formTitle.textContent = 'Add New Bookmark';
    statusGroup.style.display = 'none';
    bookmarkForm.reset();
    bookmarkFormContainer.classList.remove('hidden');
    document.getElementById('title').focus();
}

// Show edit form
function editBookmark(id) {
    const bookmark = bookmarks.find(b => b.id === id);
    if (!bookmark) return;
    
    isEditing = true;
    editingId = id;
    formTitle.textContent = 'Edit Bookmark';
    statusGroup.style.display = 'block';
    
    document.getElementById('bookmarkId').value = bookmark.id;
    document.getElementById('title').value = bookmark.title;
    document.getElementById('url').value = bookmark.url;
    document.getElementById('description').value = bookmark.description || '';
    document.getElementById('status').value = bookmark.status;
    
    bookmarkFormContainer.classList.remove('hidden');
    bookmarkFormContainer.scrollIntoView({ behavior: 'smooth' });
}

// Hide form
function hideForm() {
    bookmarkFormContainer.classList.add('hidden');
    bookmarkForm.reset();
    isEditing = false;
    editingId = null;
}

// Handle form submit
async function handleSubmit(e) {
    e.preventDefault();
    
    const formData = {
        title: document.getElementById('title').value.trim(),
        url: document.getElementById('url').value.trim(),
        description: document.getElementById('description').value.trim()
    };
    
    if (isEditing) {
        formData.status = document.getElementById('status').value;
        await updateBookmark(editingId, formData);
    } else {
        await createBookmark(formData);
    }
}

// Create bookmark
async function createBookmark(data) {
    try {
        const response = await fetch(API_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });
        
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Failed to create bookmark');
        }
        
        showToast('Bookmark created successfully!', 'success');
        hideForm();
        await loadBookmarks();
    } catch (error) {
        showToast('Error: ' + error.message, 'error');
    }
}

// Update bookmark
async function updateBookmark(id, data) {
    try {
        const response = await fetch(`${API_URL}/${id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });
        
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Failed to update bookmark');
        }
        
        showToast('Bookmark updated successfully!', 'success');
        hideForm();
        await loadBookmarks();
    } catch (error) {
        showToast('Error: ' + error.message, 'error');
    }
}

// Delete bookmark
async function deleteBookmark(id) {
    if (!confirm('Are you sure you want to delete this bookmark?')) {
        return;
    }
    
    try {
        const response = await fetch(`${API_URL}/${id}`, {
            method: 'DELETE'
        });
        
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Failed to delete bookmark');
        }
        
        showToast('Bookmark deleted successfully!', 'success');
        await loadBookmarks();
    } catch (error) {
        showToast('Error: ' + error.message, 'error');
    }
}

// Handle search
function handleSearch() {
    const query = searchInput.value.trim();
    if (query) {
        loadBookmarks(`?search=${encodeURIComponent(query)}`);
    } else if (!filterStatus.value) {
        loadBookmarks();
    } else {
        handleFilter();
    }
}

// Handle filter
function handleFilter() {
    const status = filterStatus.value;
    searchInput.value = ''; // Clear search when filtering
    
    if (status) {
        loadBookmarks(`?status=${status}`);
    } else {
        loadBookmarks();
    }
}

// Show loading state
function showLoading() {
    bookmarksList.innerHTML = '<p class="loading">Loading bookmarks...</p>';
}

// Show toast notification
function showToast(message, type = 'info') {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.className = `toast ${type}`;
    toast.classList.remove('hidden');
    
    setTimeout(() => {
        toast.classList.add('hidden');
    }, 3000);
}
