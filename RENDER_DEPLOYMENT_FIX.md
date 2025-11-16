# Fix for Render "Service Root Directory is missing" Error

## Problem
```
Service Root Directory "/opt/render/project/src/backend" is missing.
```

## Solutions (Try in Order)

### Solution 1: Remove rootDir (Current Fix)

I've updated the root `render.yaml` to remove `rootDir`. This should work if:
- Your `backend` folder exists at the repository root on GitHub
- The Dockerfile is at `backend/Dockerfile`

**Current configuration:**
```yaml
dockerfilePath: ./backend/Dockerfile
# rootDir removed
```

**Steps:**
1. Commit and push the updated `render.yaml`
2. Redeploy on Render

---

### Solution 2: Use render.yaml in Backend Folder

If Solution 1 doesn't work, move `render.yaml` to the `backend` folder:

1. **Move the file:**
   ```bash
   # Delete root render.yaml (or keep as backup)
   # Use backend/render.yaml instead
   ```

2. **In Render Dashboard:**
   - Go to your service settings
   - Set **Root Directory** to: `backend`
   - Or point Render to use `backend/render.yaml`

3. **The backend/render.yaml uses:**
   ```yaml
   dockerfilePath: ./Dockerfile  # Relative to backend folder
   ```

---

### Solution 3: Configure in Render Dashboard (No render.yaml)

1. **Go to Render Dashboard** → Your Service → Settings

2. **Set these manually:**
   - **Root Directory**: `backend` (or leave blank if backend is at root)
   - **Dockerfile Path**: `backend/Dockerfile` (or `Dockerfile` if rootDir is `backend`)
   - **Environment**: `Docker`

3. **Add all environment variables** manually in the dashboard

---

### Solution 4: Verify GitHub Repository Structure

**Check your actual GitHub repository structure:**

1. Visit: `https://github.com/thefootballervb-creator/expenses-tracker`
2. Check if you see:
   - `backend/` folder at root? → Use `dockerfilePath: ./backend/Dockerfile`
   - `Fullstack-Expense-Tracker-main/backend/`? → Use `dockerfilePath: ./Fullstack-Expense-Tracker-main/backend/Dockerfile`
   - Something else? → Adjust path accordingly

---

### Solution 5: Update Dockerfile Build Context

If the Dockerfile needs files from parent directories, update it:

**In Dockerfile, change:**
```dockerfile
# If backend is nested, you might need to adjust COPY paths
COPY ../pom.xml .  # Won't work - Docker context issue
```

**Better approach:** Ensure all files needed for build are within the backend folder, or adjust the build context.

---

## Recommended Steps

1. **First, try Solution 1** (already applied - removed rootDir)
   - Commit and push
   - Redeploy

2. **If that fails, try Solution 2:**
   - Use `backend/render.yaml` 
   - Set Root Directory in Render dashboard to `backend`

3. **If still failing, use Solution 3:**
   - Configure everything manually in Render dashboard
   - Don't use render.yaml

---

## Verify Your Setup

After applying a solution, check:

1. **Render logs** show Docker build starting
2. **No "missing directory" errors**
3. **Build progresses** to compiling Java code

---

## Quick Checklist

- [ ] `backend` folder exists in GitHub repository
- [ ] `backend/Dockerfile` exists
- [ ] `render.yaml` is at repository root OR in backend folder
- [ ] `rootDir` is either removed or set correctly
- [ ] `dockerfilePath` matches actual file location
- [ ] Committed and pushed changes
- [ ] Triggered new deployment on Render

---

## Still Not Working?

If none of these work, the issue might be:

1. **Repository structure mismatch** - GitHub structure differs from local
2. **Case sensitivity** - Check if folder is `Backend` vs `backend`
3. **Hidden files** - Ensure `.gitignore` isn't excluding backend folder
4. **Branch mismatch** - Render might be deploying from wrong branch

**Check:**
- Which branch Render is deploying from (Settings → Build & Deploy)
- If backend folder is in that branch
- Repository structure on that specific branch

