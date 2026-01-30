# Docker Deployment Guide

## Building the Docker Image

```bash
docker build -t bookmark-manager:latest .
```

Build with custom tag:
```bash
docker build -t bookmark-manager:1.0.0 .
```

## Running the Container

### Basic Run (ephemeral database)
```bash
docker run -p 8888:8888 bookmark-manager:latest
```

### Run with Persistent Database (Recommended)
```bash
docker run -p 8888:8888 \
  -v bookmark-data:/app/data \
  bookmark-manager:latest
```

### Run with Custom Configuration
```bash
docker run -p 9000:9000 \
  -e PORT=9000 \
  -e DB_URL=jdbc:sqlite:/app/data/bookmarks.db \
  -v bookmark-data:/app/data \
  bookmark-manager:latest
```

### Run in Detached Mode
```bash
docker run -d \
  --name bookmark-manager \
  -p 8888:8888 \
  -v bookmark-data:/app/data \
  --restart unless-stopped \
  bookmark-manager:latest
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `PORT` | Server port | `8888` |
| `DB_URL` | SQLite database URL | `jdbc:sqlite:/app/data/bookmarks.db` |

## Volume Management

### Create Named Volume
```bash
docker volume create bookmark-data
```

### Use Bind Mount (specific directory)
```bash
docker run -p 8888:8888 \
  -v /path/on/host/data:/app/data \
  bookmark-manager:latest
```

Windows example:
```bash
docker run -p 8888:8888 \
  -v C:\data\bookmarks:/app/data \
  bookmark-manager:latest
```

### Inspect Volume
```bash
docker volume inspect bookmark-data
```

### Backup Database
```bash
# From running container
docker exec bookmark-manager cp /app/data/bookmarks.db /app/data/bookmarks.db.backup

# Copy from container to host
docker cp bookmark-manager:/app/data/bookmarks.db ./bookmarks.db.backup
```

### Restore Database
```bash
docker cp ./bookmarks.db.backup bookmark-manager:/app/data/bookmarks.db
docker restart bookmark-manager
```

## Container Management

### View Logs
```bash
docker logs bookmark-manager
docker logs -f bookmark-manager  # Follow logs
docker logs --tail 100 bookmark-manager  # Last 100 lines
```

### Stop Container
```bash
docker stop bookmark-manager
```

### Start Stopped Container
```bash
docker start bookmark-manager
```

### Remove Container
```bash
docker rm bookmark-manager
docker rm -f bookmark-manager  # Force remove running container
```

### Access Container Shell
```bash
docker exec -it bookmark-manager /bin/bash
```

## Health Check

The container includes a health check that runs every 30 seconds:
```bash
docker ps  # Shows health status
docker inspect bookmark-manager | grep -A 10 Health
```

## Docker Compose (Optional)

Create `docker-compose.yml`:
```yaml
version: '3.8'

services:
  bookmark-manager:
    build: .
    image: bookmark-manager:latest
    container_name: bookmark-manager
    ports:
      - "8888:8888"
    environment:
      - PORT=8888
      - DB_URL=jdbc:sqlite:/app/data/bookmarks.db
    volumes:
      - bookmark-data:/app/data
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8888/health"]
      interval: 30s
      timeout: 3s
      retries: 3

volumes:
  bookmark-data:
```

Run with Docker Compose:
```bash
docker-compose up -d
docker-compose logs -f
docker-compose down
```

## Production Considerations

### Use Multi-Stage Build Benefits
- Smaller final image (~200MB vs 1GB+)
- Only JRE needed, not full JDK
- No build tools in production image

### Resource Limits
```bash
docker run -d \
  --name bookmark-manager \
  -p 8888:8888 \
  -v bookmark-data:/app/data \
  --memory="512m" \
  --cpus="1.0" \
  --restart unless-stopped \
  bookmark-manager:latest
```

### Security
```bash
# Run as non-root user (add to Dockerfile)
# Add this before ENTRYPOINT:
# RUN useradd -m -u 1000 appuser && chown -R appuser:appuser /app
# USER appuser
```

### Monitoring
```bash
# View resource usage
docker stats bookmark-manager

# View processes
docker top bookmark-manager
```

## Troubleshooting

### Container won't start
```bash
docker logs bookmark-manager
docker inspect bookmark-manager
```

### Database permission issues
```bash
docker exec bookmark-manager ls -la /app/data
docker exec bookmark-manager chmod 755 /app/data
```

### Port already in use
```bash
# Find process using port
netstat -ano | findstr :8888  # Windows
lsof -i :8888  # Linux/Mac

# Use different port
docker run -p 9000:8888 ...
```

### Clear all data and restart
```bash
docker stop bookmark-manager
docker rm bookmark-manager
docker volume rm bookmark-data
docker run -p 8888:8888 -v bookmark-data:/app/data bookmark-manager:latest
```
