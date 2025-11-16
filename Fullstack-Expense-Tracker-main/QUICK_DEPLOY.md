# Quick Deployment Checklist

Follow these steps to deploy MyPockit to Render (backend) and Netlify (frontend).

## 🚀 Step-by-Step Deployment

### 1️⃣ Backend on Render

1. **Go to [Render Dashboard](https://dashboard.render.com)**
   - Click **New** → **Web Service**
   - Connect your GitHub repository
   - Select the repository

2. **Configure Service:**
   - **Name**: `mypockit-backend`
   - **Root Directory**: `backend`
   - **Environment**: `Java`
   - **Build Command**: `./mvnw clean package -DskipTests`
   - **Start Command**: `java -jar target/expenseTracker-0.0.1-SNAPSHOT.jar`

3. **Add Environment Variables** (in Render dashboard):
   ```
   SPRING_PROFILES_ACTIVE=production
   SPRING_DATASOURCE_URL=jdbc:mysql://your-db-host:3306/expenses_tracker?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true
   SPRING_DATASOURCE_USERNAME=your-username
   SPRING_DATASOURCE_PASSWORD=your-password
   APP_JWT_SECRET=generate-a-long-random-string-here
   APP_JWT_EXPIRATION_MS=86400000
   APP_USER_PROFILE_UPLOAD_DIR=./uploads/user/profile
   CORS_ALLOWED_ORIGINS=https://your-app.netlify.app
   ```
   *(Update CORS_ALLOWED_ORIGINS after deploying frontend)*

4. **Deploy** and wait for completion
5. **Copy your backend URL**: `https://your-backend.onrender.com`

---

### 2️⃣ Frontend on Netlify

1. **Go to [Netlify Dashboard](https://app.netlify.com)**
   - Click **Add new site** → **Import an existing project**
   - Connect your GitHub repository

2. **Configure Build:**
   - **Base directory**: `frontend`
   - **Build command**: `npm run build` (auto-detected)
   - **Publish directory**: `frontend/build` (auto-detected)

3. **Add Environment Variable:**
   - Go to **Site settings** → **Environment variables**
   - Add: `REACT_APP_API_BASE_URL` = `https://your-backend.onrender.com/mypockit`
   - *(Use the backend URL from step 1)*

4. **Deploy** and wait for completion
5. **Copy your frontend URL**: `https://your-app.netlify.app`

---

### 3️⃣ Update Backend CORS

1. Go back to **Render Dashboard**
2. Open your backend service
3. Go to **Environment** tab
4. Update `CORS_ALLOWED_ORIGINS` to: `https://your-app.netlify.app`
5. **Save** and **Redeploy**

---

### 4️⃣ Test Your Deployment

1. Visit your Netlify URL
2. Try logging in (default admin: `admin@gmail.com` - check seeder for password)
3. Create a test transaction
4. Check if everything works!

---

## 📝 Important Notes

- **Database**: You need a MySQL database. Options:
  - Render PostgreSQL (free tier) - requires code changes to use PostgreSQL
  - External MySQL service (PlanetScale, Railway, etc.)
  - Your own MySQL server

- **JWT Secret**: Generate a secure random string (min 32 characters)
  ```bash
  openssl rand -base64 64
  ```

- **File Storage**: Render's file system is ephemeral. User uploads will be lost on restart. Consider cloud storage for production.

- **Free Tier Limits**: 
  - Render free tier: Services spin down after 15 minutes of inactivity
  - Netlify free tier: 100GB bandwidth/month

---

## 🆘 Need Help?

See `DEPLOYMENT.md` for detailed instructions and troubleshooting.

