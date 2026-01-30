# Test Validation Rules & URL Normalization
# Run the application first: ./gradlew run
# Then run this script in another terminal

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Testing Bookmark Manager Validation" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:8888/api/bookmarks"

# Test 1: URL Normalization - Success
Write-Host "[Test 1] URL Normalization: 'example.com' -> 'https://example.com'" -ForegroundColor Yellow
$response = Invoke-WebRequest -Uri $baseUrl -Method POST -ContentType "application/json" `
    -Body '{"url": "example.com", "title": "Test Bookmark"}' -UseBasicParsing -ErrorAction SilentlyContinue
if ($response.StatusCode -eq 201) {
    Write-Host "✓ SUCCESS: " -ForegroundColor Green -NoNewline
    Write-Host $response.Content
} else {
    Write-Host "✗ FAILED" -ForegroundColor Red
}
Write-Host ""

# Test 2: Empty URL
Write-Host "[Test 2] Empty URL (should fail)" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri $baseUrl -Method POST -ContentType "application/json" `
        -Body '{"url": "", "title": "Test"}' -UseBasicParsing -ErrorAction Stop
} catch {
    Write-Host "✓ EXPECTED ERROR: " -ForegroundColor Green -NoNewline
    Write-Host $_.Exception.Response.StatusCode - $_.ErrorDetails.Message
}
Write-Host ""

# Test 3: Unsupported URL Scheme
Write-Host "[Test 3] Unsupported scheme 'ftp://' (should fail)" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri $baseUrl -Method POST -ContentType "application/json" `
        -Body '{"url": "ftp://files.example.com", "title": "FTP Site"}' -UseBasicParsing -ErrorAction Stop
} catch {
    Write-Host "✓ EXPECTED ERROR: " -ForegroundColor Green -NoNewline
    Write-Host $_.Exception.Response.StatusCode - $_.ErrorDetails.Message
}
Write-Host ""

# Test 4: Title Too Long
Write-Host "[Test 4] Title exceeds 120 characters (should fail)" -ForegroundColor Yellow
$longTitle = "x" * 121
try {
    $response = Invoke-WebRequest -Uri $baseUrl -Method POST -ContentType "application/json" `
        -Body "{`"url`": `"example.com`", `"title`": `"$longTitle`"}" -UseBasicParsing -ErrorAction Stop
} catch {
    Write-Host "✓ EXPECTED ERROR: " -ForegroundColor Green -NoNewline
    Write-Host $_.Exception.Response.StatusCode - $_.ErrorDetails.Message
}
Write-Host ""

# Test 5: Empty Title
Write-Host "[Test 5] Empty title (should fail)" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri $baseUrl -Method POST -ContentType "application/json" `
        -Body '{"url": "example.com", "title": "   "}' -UseBasicParsing -ErrorAction Stop
} catch {
    Write-Host "✓ EXPECTED ERROR: " -ForegroundColor Green -NoNewline
    Write-Host $_.Exception.Response.StatusCode - $_.ErrorDetails.Message
}
Write-Host ""

# Test 6: Tags Too Long
Write-Host "[Test 6] Tags exceed 200 characters (should fail)" -ForegroundColor Yellow
$longTags = "tag1, tag2, tag3, " * 20  # Creates string > 200 chars
try {
    $response = Invoke-WebRequest -Uri $baseUrl -Method POST -ContentType "application/json" `
        -Body "{`"url`": `"example.com`", `"title`": `"Test`", `"tags`": `"$longTags`"}" -UseBasicParsing -ErrorAction Stop
} catch {
    Write-Host "✓ EXPECTED ERROR: " -ForegroundColor Green -NoNewline
    Write-Host $_.Exception.Response.StatusCode - $_.ErrorDetails.Message
}
Write-Host ""

# Test 7: Whitespace Normalization - Success
Write-Host "[Test 7] Whitespace normalization (should succeed)" -ForegroundColor Yellow
$response = Invoke-WebRequest -Uri $baseUrl -Method POST -ContentType "application/json" `
    -Body '{"url": "  github.com  ", "title": "  Multiple    Spaces   ", "tags": "  java,  spring  "}' -UseBasicParsing -ErrorAction SilentlyContinue
if ($response.StatusCode -eq 201) {
    Write-Host "✓ SUCCESS: " -ForegroundColor Green -NoNewline
    Write-Host $response.Content
} else {
    Write-Host "✗ FAILED" -ForegroundColor Red
}
Write-Host ""

# Test 8: Invalid Status
Write-Host "[Test 8] Invalid status 'PENDING' (should fail)" -ForegroundColor Yellow
# First create a bookmark to patch
$createResponse = Invoke-WebRequest -Uri $baseUrl -Method POST -ContentType "application/json" `
    -Body '{"url": "example.com", "title": "Status Test"}' -UseBasicParsing -ErrorAction SilentlyContinue
$bookmark = $createResponse.Content | ConvertFrom-Json
$bookmarkId = $bookmark.id

try {
    $response = Invoke-WebRequest -Uri "$baseUrl/$bookmarkId/status" -Method PATCH -ContentType "application/json" `
        -Body '{"status": "PENDING"}' -UseBasicParsing -ErrorAction Stop
} catch {
    Write-Host "✓ EXPECTED ERROR: " -ForegroundColor Green -NoNewline
    Write-Host $_.Exception.Response.StatusCode - $_.ErrorDetails.Message
}
Write-Host ""

# Test 9: Valid Status Change
Write-Host "[Test 9] Valid status change to 'DONE' (should succeed)" -ForegroundColor Yellow
$response = Invoke-WebRequest -Uri "$baseUrl/$bookmarkId/status" -Method PATCH -ContentType "application/json" `
    -Body '{"status": "DONE"}' -UseBasicParsing -ErrorAction SilentlyContinue
if ($response.StatusCode -eq 200) {
    Write-Host "✓ SUCCESS: " -ForegroundColor Green -NoNewline
    Write-Host $response.Content
} else {
    Write-Host "✗ FAILED" -ForegroundColor Red
}
Write-Host ""

# Test 10: Negative Pagination Parameter
Write-Host "[Test 10] Negative limit parameter (should fail)" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl?limit=-10" -Method GET -UseBasicParsing -ErrorAction Stop
} catch {
    Write-Host "✓ EXPECTED ERROR: " -ForegroundColor Green -NoNewline
    Write-Host $_.Exception.Response.StatusCode - $_.ErrorDetails.Message
}
Write-Host ""

# Test 11: Valid Pagination
Write-Host "[Test 11] Valid pagination (should succeed)" -ForegroundColor Yellow
$response = Invoke-WebRequest -Uri "$baseUrl?limit=5&offset=0" -Method GET -UseBasicParsing -ErrorAction SilentlyContinue
if ($response.StatusCode -eq 200) {
    Write-Host "✓ SUCCESS: Retrieved bookmarks with pagination" -ForegroundColor Green
} else {
    Write-Host "✗ FAILED" -ForegroundColor Red
}
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Validation Testing Complete!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
