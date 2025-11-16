# Deployment Checklist - Render Backend

## Current Issue
Docker build is failing because it can't find files. The Dockerfile needs to be updated and pushed to GitHub.

## Steps to Fix

### 1. Verify Dockerfile is Updated
The Dockerfile should have `backend/` prefix in COPY commands:
```dockerfile
COPY backend/mvnw .
COPY backend/.mvn .mvn
COPY backend/pom.xml .
COPY backend/src ./src
```

### 2. Commit and Push Changes
```bash
cd Fullstack-Expense-Tracker-main/backend
git add Dockerfile
git commit -m "Fix Dockerfile paths for Render build context"
git push origin main
```

### 3. Verify GitHub Repository Structure
Check: https://github.com/thefootballervb-creator/expenses-tracker

**Expected structure:**
```
expenses-tracker/
├── backend/
│   ├── Dockerfile
│   ├── pom.xml
│   ├── mvnw
│   ├── src/
│   └── ...
├── frontend/
└── render.yaml
```

**If structure is different (nested):**
```
expenses-tracker/
├── Fullstack-Expense-Tracker-main/
│   ├── backend/
│   │   ├── Dockerfile
│   │   └── ...
│   └── frontend/
└── render.yaml
```

Then update Dockerfile to:
```dockerfile
COPY Fullstack-Expense-Tracker-main/backend/mvnw .
COPY Fullstack-Expense-Tracker-main/backend/.mvn .mvn
COPY Fullstack-Expense-Tracker-main/backend/pom.xml .
COPY Fullstack-Expense-Tracker-main/backend/src ./src
```

And update render.yaml:
```yaml
dockerfilePath: ./Fullstack-Expense-Tracker-main/backend/Dockerfile
```

### 4. Clear Render Cache (if needed)
In Render Dashboard:
- Go to your service
- Settings → Clear build cache
- Redeploy

### 5. Verify Deployment
After pushing, Render should automatically:
1. Detect the new commit
2. Start a new build
3. Successfully find all files with `backend/` prefix

## Quick Fix Command

If you're in the project root:
```bash
# Make sure you're in the right directory
cd Fullstack-Expense-Tracker-main/backend

# Verify Dockerfile has backend/ prefix
grep "COPY backend" Dockerfile

# If not, the file needs to be updated (already done locally)

# Commit and push
git add Dockerfile
git commit -m "Fix Dockerfile: Add backend/ prefix for Render build context"
git push
```

## Alternative: Use rootDir in render.yaml

If the paths are still wrong, try setting rootDir in render.yaml:

```yaml
services:
  - type: web
    name: mypockit-backend
    env: docker
    dockerfilePath: ./Dockerfile
    rootDir: ./backend
```

Then revert Dockerfile to original (without backend/ prefix):
```dockerfile
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src ./src
```

This tells Render to use `backend/` as the build context, so paths in Dockerfile are relative to backend folder.

