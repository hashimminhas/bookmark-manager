# Test script for Bookmark Manager API
# Run this after starting the application

$BASE_URL = "http://localhost:8080/api/bookmarks"

Write-Host "===== Bookmark Manager API Test =====" -ForegroundColor Cyan
Write-Host ""

# Test 1: Create a bookmark
Write-Host "1. Creating a bookmark..." -ForegroundColor Yellow
$createBody = @{
    title = "GitHub"
    url = "https://github.com"
    description = "Where the world builds software"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri $BASE_URL -Method POST -Body $createBody -ContentType "application/json"
Write-Host "Created bookmark with ID: $($response.id)" -ForegroundColor Green
$bookmarkId = $response.id
Write-Host ""

# Test 2: Get all bookmarks
Write-Host "2. Getting all bookmarks..." -ForegroundColor Yellow
$bookmarks = Invoke-RestMethod -Uri $BASE_URL -Method GET
Write-Host "Found $($bookmarks.Count) bookmark(s)" -ForegroundColor Green
Write-Host ""

# Test 3: Search bookmarks
Write-Host "3. Searching for 'GitHub'..." -ForegroundColor Yellow
$searchResults = Invoke-RestMethod -Uri "$BASE_URL?search=GitHub" -Method GET
Write-Host "Found $($searchResults.Count) matching bookmark(s)" -ForegroundColor Green
Write-Host ""

# Test 4: Filter by status
Write-Host "4. Filtering by status INBOX..." -ForegroundColor Yellow
$inboxResults = Invoke-RestMethod -Uri "$BASE_URL?status=INBOX" -Method GET
Write-Host "Found $($inboxResults.Count) INBOX bookmark(s)" -ForegroundColor Green
Write-Host ""

# Test 5: Update bookmark
Write-Host "5. Updating bookmark to DONE..." -ForegroundColor Yellow
$updateBody = @{
    title = "GitHub - Updated"
    url = "https://github.com"
    description = "Updated description"
    status = "DONE"
} | ConvertTo-Json

$updated = Invoke-RestMethod -Uri "$BASE_URL/$bookmarkId" -Method PUT -Body $updateBody -ContentType "application/json"
Write-Host "Updated bookmark status to: $($updated.status)" -ForegroundColor Green
Write-Host ""

# Test 6: Get specific bookmark
Write-Host "6. Getting specific bookmark..." -ForegroundColor Yellow
$specific = Invoke-RestMethod -Uri "$BASE_URL/$bookmarkId" -Method GET
Write-Host "Title: $($specific.title)" -ForegroundColor Green
Write-Host "Status: $($specific.status)" -ForegroundColor Green
Write-Host ""

# Test 7: Delete bookmark
Write-Host "7. Deleting bookmark..." -ForegroundColor Yellow
Invoke-RestMethod -Uri "$BASE_URL/$bookmarkId" -Method DELETE
Write-Host "Bookmark deleted successfully" -ForegroundColor Green
Write-Host ""

Write-Host "===== All tests completed! =====" -ForegroundColor Cyan
