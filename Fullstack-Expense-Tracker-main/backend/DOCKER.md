# Docker Guide for MyPockit Backend

This guide explains how to build and run the MyPockit backend using Docker.

## Prerequisites

- Docker installed on your system
- Docker Compose (optional, for running with MySQL)

## Building the Docker Image

### Basic Build

```bash
cd backend
docker build -t mypockit-backend:latest .
```

### Build with Custom Tag

```bash
docker build -t mypockit-backend:v1.0.0 .
```

## Running the Container

### Using Docker Run

```bash
docker run -d \
  --name mypockit-backend \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=production \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/expenses_tracker?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=your-password \
  -e APP_JWT_SECRET=your-super-secret-jwt-key-here \
  -e APP_JWT_EXPIRATION_MS=86400000 \
  -e APP_USER_PROFILE_UPLOAD_DIR=./uploads/user/profile \
  -e CORS_ALLOWED_ORIGINS=http://localhost:3000 \
  -v $(pwd)/uploads:/app/uploads \
  mypockit-backend:latest
```

### Using Docker Compose (Recommended)

The `docker-compose.yml` file includes both the backend and MySQL database:

```bash
cd backend
docker-compose up -d
```

This will:
- Build the backend image
- Start MySQL database
- Start the backend service
- Create necessary volumes and networks

To stop:

```bash
docker-compose down
```

To stop and remove volumes (⚠️ deletes database data):

```bash
docker-compose down -v
```

## Environment Variables

The following environment variables can be set:

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Spring profile | `production` |
| `SPRING_DATASOURCE_URL` | Database connection URL | Required |
| `SPRING_DATASOURCE_USERNAME` | Database username | Required |
| `SPRING_DATASOURCE_PASSWORD` | Database password | Required |
| `APP_JWT_SECRET` | JWT signing secret | Required |
| `APP_JWT_EXPIRATION_MS` | JWT expiration time (ms) | `86400000` |
| `APP_USER_PROFILE_UPLOAD_DIR` | Upload directory | `./uploads/user/profile` |
| `CORS_ALLOWED_ORIGINS` | Allowed CORS origins | Required |
| `PORT` | Server port | `8080` |

## Using Environment File

Create a `.env` file in the backend directory:

```env
SPRING_PROFILES_ACTIVE=production
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/expenses_tracker?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=123456789
APP_JWT_SECRET=your-super-secret-jwt-key-here-make-it-long-and-random
APP_JWT_EXPIRATION_MS=86400000
APP_USER_PROFILE_UPLOAD_DIR=./uploads/user/profile
CORS_ALLOWED_ORIGINS=http://localhost:3000
```

Then run:

```bash
docker-compose --env-file .env up -d
```

## Viewing Logs

### Docker Run

```bash
docker logs mypockit-backend
docker logs -f mypockit-backend  # Follow logs
```

### Docker Compose

```bash
docker-compose logs backend
docker-compose logs -f backend  # Follow logs
```

## Health Check

The Dockerfile includes a health check that verifies the application is running:

```bash
docker ps  # Check health status
```

## Volumes

The `uploads` directory is mounted as a volume to persist user profile images:

```bash
-v $(pwd)/uploads:/app/uploads
```

## Troubleshooting

### Container won't start

1. Check logs:
   ```bash
   docker logs mypockit-backend
   ```

2. Verify environment variables are set correctly

3. Check if port 8080 is already in use:
   ```bash
   # Linux/Mac
   lsof -i :8080
   
   # Windows
   netstat -ano | findstr :8080
   ```

### Database connection issues

1. Ensure MySQL is running and accessible
2. Check database URL, username, and password
3. For Docker Compose, use service name `mysql` instead of `localhost`
4. For Docker Run, use `host.docker.internal` to access host MySQL

### Build fails

1. Clear Docker cache:
   ```bash
   docker builder prune
   ```

2. Rebuild without cache:
   ```bash
   docker build --no-cache -t mypockit-backend:latest .
   ```

## Production Deployment

For production deployment:

1. Use a managed database service (not containerized MySQL)
2. Set strong passwords and JWT secrets
3. Use environment variables or secrets management
4. Enable SSL/TLS for database connections
5. Configure proper CORS origins
6. Use a reverse proxy (nginx) for additional security
7. Set up proper logging and monitoring

## Docker Image Size Optimization

The Dockerfile uses:
- Multi-stage build to reduce final image size
- Alpine Linux base images for smaller footprint
- JRE instead of JDK in runtime stage

Current image size: ~200-250MB

## Pushing to Docker Registry

### Tag for registry

```bash
docker tag mypockit-backend:latest your-registry/mypockit-backend:latest
```

### Push to Docker Hub

```bash
docker login
docker push your-registry/mypockit-backend:latest
```

### Push to other registries

```bash
# For AWS ECR, Google GCR, Azure ACR, etc.
# Follow their specific authentication and push commands
```

## Using with Render

Render can use Dockerfiles directly. Simply:

1. Point Render to your repository
2. Set Root Directory to `backend`
3. Render will automatically detect and use the Dockerfile
4. Set environment variables in Render dashboard

## Using with Other Platforms

The Dockerfile is compatible with:
- AWS ECS/Fargate
- Google Cloud Run
- Azure Container Instances
- Kubernetes
- Any Docker-compatible platform

