# Environment Variables Reference

## Backend (Render) - Required Environment Variables

### Database Configuration
```bash
SPRING_DATASOURCE_URL=jdbc:mysql://your-db-host:3306/expenses_tracker?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_USERNAME=your-db-username
SPRING_DATASOURCE_PASSWORD=your-db-password
```

### Application Configuration
```bash
SPRING_PROFILES_ACTIVE=production
APP_JWT_SECRET=your-super-secret-jwt-key-here-make-it-long-and-random-min-32-chars
APP_JWT_EXPIRATION_MS=86400000
APP_USER_PROFILE_UPLOAD_DIR=./uploads/user/profile
```

### CORS Configuration
```bash
CORS_ALLOWED_ORIGINS=https://your-app.netlify.app
```
*Note: Update this after deploying frontend to Netlify*

---

## Frontend (Netlify) - Required Environment Variables

### API Configuration
```bash
REACT_APP_API_BASE_URL=https://your-backend.onrender.com/mypockit
```
*Replace with your actual Render backend URL*

---

## Quick Setup Checklist

### Backend (Render)
- [ ] Set `SPRING_PROFILES_ACTIVE=production`
- [ ] Configure database connection (`SPRING_DATASOURCE_URL`, `USERNAME`, `PASSWORD`)
- [ ] Generate and set `APP_JWT_SECRET` (long random string, min 32 characters)
- [ ] Set `CORS_ALLOWED_ORIGINS` to your Netlify URL (after frontend deployment)

### Frontend (Netlify)
- [ ] Set `REACT_APP_API_BASE_URL` to your Render backend URL

---

## Generating a Secure JWT Secret

You can generate a secure JWT secret using one of these methods:

**Using OpenSSL:**
```bash
openssl rand -base64 64
```

**Using Node.js:**
```bash
node -e "console.log(require('crypto').randomBytes(64).toString('base64'))"
```

**Using Python:**
```bash
python -c "import secrets; print(secrets.token_urlsafe(64))"
```

**Online Generator:**
- Visit: https://www.grc.com/passwords.htm
- Generate a 64-character random password

---

## Example Values

### Backend Example
```bash
SPRING_PROFILES_ACTIVE=production
SPRING_DATASOURCE_URL=jdbc:mysql://db.example.com:3306/expenses_tracker?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_USERNAME=expense_user
SPRING_DATASOURCE_PASSWORD=SecurePassword123!
APP_JWT_SECRET=K8mN2pQ5rS7tU9vW1xY3zA4bC6dE8fG0hI2jK4lM6nO8pQ0rS2tU4vW6xY8z
APP_JWT_EXPIRATION_MS=86400000
APP_USER_PROFILE_UPLOAD_DIR=./uploads/user/profile
CORS_ALLOWED_ORIGINS=https://mypockit.netlify.app
```

### Frontend Example
```bash
REACT_APP_API_BASE_URL=https://mypockit-backend.onrender.com/mypockit
```

