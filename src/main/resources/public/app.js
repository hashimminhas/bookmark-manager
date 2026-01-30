// API Base URL
const API_URL = '/api/bookmarks';

// State
let bookmarks = [];
let isEditing = false;
let editingId = null;

// DOM Elements
const searchInput = document.getElementById('searchInput');
const filterStatus = document.getElementById('filterStatus');
const addBookmarkBtn = document.getElementById('addBookmarkBtn');
const bookmarkFormContainer = document.getElementById('bookmarkFormContainer');
const bookmarkForm = document.getElementById('bookmarkForm');
const bookmarksList = document.getElementById('bookmarksList');
const cancelBtn = document.getElementById('cancelBtn');
const formTitle = document.getElementById('formTitle');
const statusGroup = document.getElementById('statusGroup');

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

// Utility: Debounce function
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

// Utility: Escape HTML
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Utility: Format date
function formatDate(dateString) {
    const date = new Date(dateString);
    const now = new Date();
    const diffInSeconds = Math.floor((now - date) / 1000);
    
    if (diffInSeconds < 60) {
        return 'just now';
    } else if (diffInSeconds < 3600) {
        const minutes = Math.floor(diffInSeconds / 60);
        return `${minutes} minute${minutes > 1 ? 's' : ''} ago`;
    } else if (diffInSeconds < 86400) {
        const hours = Math.floor(diffInSeconds / 3600);
        return `${hours} hour${hours > 1 ? 's' : ''} ago`;
    } else if (diffInSeconds < 604800) {
        const days = Math.floor(diffInSeconds / 86400);
        return `${days} day${days > 1 ? 's' : ''} ago`;
    } else {
        return date.toLocaleDateString();
    }
}
