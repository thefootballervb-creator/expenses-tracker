# Deployment Guide: MyPockit Expense Tracker

This guide will help you deploy the MyPockit Expense Tracker application to **Render** (backend) and **Netlify** (frontend).

## Prerequisites

1. **GitHub Account** - Your code should be in a GitHub repository
2. **Render Account** - Sign up at [render.com](https://render.com)
3. **Netlify Account** - Sign up at [netlify.com](https://netlify.com)
4. **MySQL Database** - You can use Render's PostgreSQL (free tier) or an external MySQL service

---

## Part 1: Backend Deployment on Render

### Step 1: Prepare MySQL Database

You have two options:

**Option A: Use Render's PostgreSQL (Recommended)**
- Go to Render Dashboard → New → PostgreSQL
- Create a new PostgreSQL database
- Note the **Internal Database URL** and **External Database URL**

**Option B: Use External MySQL**
- Use a service like [PlanetScale](https://planetscale.com), [Railway](https://railway.app), or your own MySQL server
- Note the connection URL, username, and password

### Step 2: Deploy Backend to Render

1. **Go to Render Dashboard**
   - Click **New** → **Web Service**

2. **Connect Your Repository**
   - Connect your GitHub repository
   - Select the repository containing this project

3. **Configure the Service**
   - **Name**: `mypockit-backend` (or any name you prefer)
   - **Root Directory**: `backend`
   - **Environment**: `Java`
   - **Build Command**: `./mvnw clean package -DskipTests`
   - **Start Command**: `java -jar target/expenseTracker-0.0.1-SNAPSHOT.jar`

4. **Set Environment Variables**
   Click **Advanced** → **Add Environment Variable** and add:

   ```
   SPRING_PROFILES_ACTIVE=production
   ```

   ```
   SPRING_DATASOURCE_URL=jdbc:mysql://your-db-host:3306/expenses_tracker?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true
   ```
   *(Replace with your actual database URL)*

   ```
   SPRING_DATASOURCE_USERNAME=your-db-username
   ```

   ```
   SPRING_DATASOURCE_PASSWORD=your-db-password
   ```

   ```
   APP_JWT_SECRET=your-super-secret-jwt-key-here-make-it-long-and-random-min-32-chars
   ```
   *(Generate a long random string for security)*

   ```
   APP_JWT_EXPIRATION_MS=86400000
   ```

   ```
   APP_USER_PROFILE_UPLOAD_DIR=./uploads/user/profile
   ```

   ```
   CORS_ALLOWED_ORIGINS=https://your-app.netlify.app
   ```
   *(Replace with your Netlify frontend URL - you'll update this after deploying frontend)*

5. **Deploy**
   - Click **Create Web Service**
   - Render will build and deploy your backend
   - Wait for deployment to complete (5-10 minutes)
   - Note your backend URL: `https://your-backend.onrender.com`

### Step 3: Update CORS After Frontend Deployment

After deploying the frontend, update the `CORS_ALLOWED_ORIGINS` environment variable in Render:
- Go to your Render service → **Environment**
- Update `CORS_ALLOWED_ORIGINS` to: `https://your-app.netlify.app`
- Save and redeploy

---

## Part 2: Frontend Deployment on Netlify

### Step 1: Build Configuration

The project already includes `netlify.toml` with the correct build settings.

### Step 2: Deploy to Netlify

1. **Go to Netlify Dashboard**
   - Click **Add new site** → **Import an existing project**

2. **Connect Your Repository**
   - Connect your GitHub repository
   - Select the repository

3. **Configure Build Settings**
   - **Base directory**: `frontend`
   - **Build command**: `npm run build` (should auto-detect)
   - **Publish directory**: `frontend/build` (should auto-detect)

4. **Set Environment Variables**
   Click **Site settings** → **Environment variables** → **Add variable**:

   ```
   REACT_APP_API_BASE_URL=https://your-backend.onrender.com/mypockit
   ```
   *(Replace with your actual Render backend URL from Part 1)*

5. **Deploy**
   - Click **Deploy site**
   - Netlify will build and deploy your frontend
   - Wait for deployment to complete (3-5 minutes)
   - Your site will be available at: `https://your-app.netlify.app`

### Step 3: Update Backend CORS

Go back to Render and update the `CORS_ALLOWED_ORIGINS` environment variable with your Netlify URL.

---

## Part 3: Database Setup

### Initial Data Seeding

The application includes data seeders that will run automatically on first startup:
- Admin users (`admin@gmail.com` and `admin2@gmail.com`)
- Transaction types (Income, Expense)
- Default categories
- User roles

**Default Admin Credentials:**
- Email: `admin@gmail.com` or `admin2@gmail.com`
- Password: Check your `AdminUserSeeder.java` file for the default password

**⚠️ Important:** Change the admin password after first login in production!

---

## Part 4: Testing the Deployment

1. **Test Frontend**
   - Visit your Netlify URL
   - Try logging in with the admin credentials
   - Test creating a transaction

2. **Test Backend**
   - Visit `https://your-backend.onrender.com/mypockit/category/getAll`
   - You should see JSON response with categories

3. **Check Logs**
   - **Render**: Go to your service → **Logs** tab
   - **Netlify**: Go to your site → **Deploys** → Click on a deploy → **Deploy log**

---

## Troubleshooting

### Backend Issues

**Problem: Build fails**
- Check Render logs for Maven errors
- Ensure Java version is compatible (JDK 17+)
- Verify `pom.xml` is correct

**Problem: Database connection fails**
- Verify database URL, username, and password
- Check if database allows external connections
- For Render PostgreSQL, use the **Internal Database URL** if both services are on Render

**Problem: 401 Unauthorized errors**
- Check if JWT secret is set correctly
- Verify CORS configuration includes your Netlify URL
- Ensure users are logging in to get fresh tokens

**Problem: File uploads not working**
- Render's file system is ephemeral (files are lost on restart)
- Consider using cloud storage (AWS S3, Cloudinary) for production

### Frontend Issues

**Problem: Build fails**
- Check Netlify build logs
- Ensure `REACT_APP_API_BASE_URL` is set correctly
- Verify Node.js version (should be 18+)

**Problem: API calls fail**
- Check browser console for CORS errors
- Verify `REACT_APP_API_BASE_URL` matches your Render backend URL
- Ensure backend CORS includes your Netlify URL

**Problem: Blank page**
- Check browser console for errors
- Verify environment variables are set
- Check Netlify build logs

---

## Environment Variables Summary

### Backend (Render)
```
SPRING_PROFILES_ACTIVE=production
SPRING_DATASOURCE_URL=jdbc:mysql://...
SPRING_DATASOURCE_USERNAME=...
SPRING_DATASOURCE_PASSWORD=...
APP_JWT_SECRET=...
APP_JWT_EXPIRATION_MS=86400000
APP_USER_PROFILE_UPLOAD_DIR=./uploads/user/profile
CORS_ALLOWED_ORIGINS=https://your-app.netlify.app
```

### Frontend (Netlify)
```
REACT_APP_API_BASE_URL=https://your-backend.onrender.com/mypockit
```

---

## Additional Notes

1. **File Storage**: Render's file system is ephemeral. For production, consider using cloud storage for user profile images.

2. **Database Migrations**: The app uses `spring.jpa.hibernate.ddl-auto=update`, which automatically creates/updates tables. For production, consider using Flyway or Liquibase for better migration control.

3. **SSL/HTTPS**: Both Render and Netlify provide free SSL certificates automatically.

4. **Custom Domain**: You can add custom domains to both Render and Netlify services.

5. **Monitoring**: Set up monitoring and alerts in both Render and Netlify dashboards.

---

## Support

If you encounter issues:
1. Check the logs in Render and Netlify
2. Verify all environment variables are set correctly
3. Ensure database is accessible
4. Test API endpoints directly using Postman or curl

Good luck with your deployment! 🚀

