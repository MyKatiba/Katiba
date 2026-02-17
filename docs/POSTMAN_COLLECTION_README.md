# Katiba API Postman Collection

This is a comprehensive Postman collection for testing the Katiba Backend API. It includes all endpoints for authentication, user management, content retrieval, learning progress tracking, and administrative operations.

## 📦 What's Included

The collection contains **60+ API requests** organized into the following categories:

### 1. General (2 requests)
- Root API Info
- Health Check

### 2. Authentication (9 requests)
- Register
- Verify Email (with OTP)
- Resend OTP
- Login
- Refresh Token
- Logout
- Forgot Password
- Verify Reset OTP
- Reset Password

### 3. Users (6 requests)
- Get My Profile
- Update Profile
- Upload Avatar
- Get My Progress
- Get My Badges
- Get My Activity

### 4. Content (8 requests)
- Get Daily Content
- Get Daily Content by Date
- Get Full Constitution
- Get All Chapters
- Get Chapter by Number
- Get Article by Number
- Search Content
- Get Videos by Category

### 5. Learning (5 requests)
- Get Lessons
- Get Lesson by ID
- Complete Lesson
- Update Streak
- Get Leaderboard

### 6. Admin (17 requests)
#### User Management
- Get All Users
- Get User by ID
- Update User
- Delete User
- Update User Role
- Reset User Password

#### Session Management
- Get All Sessions
- Invalidate Session

#### Content Management
- Create Daily Content
- Update Daily Content
- Delete Daily Content

#### Video Management
- Upload Video
- Update Video
- Delete Video

#### Analytics
- Get Analytics

---

## 🚀 Getting Started

### Step 1: Import the Collection

1. Open Postman
2. Click **"Import"** button (top left)
3. Drag and drop `Katiba_API_Postman_Collection.json` or click to browse
4. Collection will appear in your Collections sidebar

### Step 2: Set Up Environment Variables

The collection uses variables for easy testing. Update these in the collection variables:

| Variable | Default Value | Description |
|----------|---------------|-------------|
| `baseUrl` | `http://localhost:3000` | Your API base URL |
| `accessToken` | (auto-set) | JWT access token |
| `refreshToken` | (auto-set) | JWT refresh token |
| `userId` | (auto-set) | Current user ID |
| `resetToken` | (auto-set) | Password reset token |

**To edit variables:**
1. Right-click the collection → **"Edit"**
2. Go to **"Variables"** tab
3. Update the `baseUrl` to match your deployed backend URL
4. Click **"Save"**

### Step 3: Start Testing

Begin with the authentication flow:
1. **Register** → Creates a new user
2. **Verify Email** → Validates OTP and logs you in
3. Now you can test protected endpoints!

---

## 🔐 Authentication Flow

### Complete Registration & Login Flow

```
1. Register
   ↓ (saves userId)
2. Check email for OTP
   ↓
3. Verify Email (with userId + OTP)
   ↓ (saves accessToken & refreshToken)
4. Access protected endpoints
   ↓ (token expires)
5. Refresh Token
   ↓ (gets new accessToken)
6. Continue using API
   ↓
7. Logout (invalidates refreshToken)
```

### Password Reset Flow

```
1. Forgot Password (with email)
   ↓ (saves userId)
2. Check email for OTP
   ↓
3. Verify Reset OTP (with userId + OTP)
   ↓ (saves resetToken)
4. Reset Password (with resetToken + new password)
   ↓
5. Login with new password
```

---

## 🎯 Auto-Configuration

The collection includes **automatic scripts** that save tokens and IDs for you:

### After Registration:
- `userId` is automatically saved

### After Login/Verify Email:
- `accessToken` is automatically saved
- `refreshToken` is automatically saved
- `userId` is automatically saved

### After Verify Reset OTP:
- `resetToken` is automatically saved

**You don't need to manually copy/paste tokens!** 🎉

---

## 📝 Testing Guide

### Testing Public Endpoints (No Auth Required)

These endpoints work without authentication:
- Root API Info: `GET /`
- Health Check: `GET /api/health`
- All auth endpoints in `/api/auth/*`

### Testing Protected Endpoints (Auth Required)

All other endpoints require authentication. The collection automatically includes your `accessToken` in the Authorization header.

**If you get "Unauthorized" errors:**
1. Make sure you've logged in first
2. Check that `accessToken` variable is set
3. Token may have expired → use "Refresh Token" endpoint

### Testing Admin Endpoints

Admin endpoints require:
1. A user account with `admin` or `moderator` role
2. Valid authentication token

**Default roles:**
- `user` - Regular users (default)
- `moderator` - Can manage content and videos
- `admin` - Full access to all admin endpoints

---

## 🧪 Sample Test Scenarios

### Scenario 1: New User Registration

1. **Register** with new email
2. Check terminal/email service for OTP code
3. **Verify Email** with the OTP
4. **Get My Profile** to see user details
5. **Update Profile** to add civic information
6. **Upload Avatar** to add profile picture

### Scenario 2: Content Browsing

1. **Login** with credentials
2. **Get Daily Content** to see today's lesson
3. **Get All Chapters** to browse constitution structure
4. **Get Chapter by Number** (e.g., Chapter 1)
5. **Get Article by Number** (e.g., Article 1)
6. **Search Content** for specific keywords

### Scenario 3: Learning Progress

1. **Login** with credentials
2. **Get Lessons** to see available lessons
3. **Get Lesson by ID** to view a specific lesson
4. **Complete Lesson** with score and time
5. **Update Streak** to maintain learning streak
6. **Get My Progress** to see completed lessons
7. **Get Leaderboard** to see rankings

### Scenario 4: Admin Operations

1. **Login** as admin user
2. **Get All Users** to view user list
3. **Update User Role** to promote a moderator
4. **Create Daily Content** for a specific date
5. **Upload Video** lesson with thumbnail
6. **Get Analytics** to view platform stats

---

## 📋 Request Body Examples

### Register
```json
{
  "email": "user@example.com",
  "password": "SecurePass123!",
  "confirm_password": "SecurePass123!",
  "name": "John Doe"
}
```

### Verify Email
```json
{
  "userId": "{{userId}}",
  "otp": "123456"
}
```

### Login
```json
{
  "email": "user@example.com",
  "password": "SecurePass123!"
}
```

### Update Profile
```json
{
  "name": "John Doe Updated",
  "county": "Nairobi",
  "constituency": "Westlands",
  "ward": "Kitisuru",
  "national_id": "12345678",
  "registered_voter": true
}
```

### Complete Lesson
```json
{
  "lessonType": "daily_content",
  "score": 100,
  "timeTaken": 120
}
```

### Create Daily Content (Admin)
```json
{
  "content_date": "2026-02-20",
  "article_number": 1,
  "clause_number": "1(1)",
  "focus_area": "Sovereignty",
  "explanation": "This clause establishes the sovereignty of Kenya."
}
```

### Upload Video (Admin)
Form data:
- `video` (file) - Video file (max 100MB)
- `thumbnail` (file) - Thumbnail image
- `title` (text) - "Introduction to the Constitution"
- `description` (text) - Video description
- `category` (text) - "introduction", "rights", "structure", etc.
- `duration` (text) - Duration in seconds
- `order_index` (text) - Order in category

---

## 🔧 Query Parameters

### Pagination (Most List Endpoints)
```
?page=1&limit=20
```

### Search
```
?q=freedom&type=all
```
Types: `all`, `chapter`, `article`, `clause`

### Date Filter
```
/api/content/daily/2026-02-15
```
Format: `YYYY-MM-DD`

### Video Category
```
/api/content/videos/introduction
```
Categories: `introduction`, `rights`, `government`, `devolution`, etc.

---

## 🌐 Deployment URLs

Update the `baseUrl` variable based on your environment:

### Local Development
```
http://localhost:3000
```

### Staging/Production
Replace with your deployed URL:
```
https://api.katiba.app
https://katiba-backend.onrender.com
https://katiba-api.railway.app
```

**To switch environments quickly:**
1. Create Postman environments (Local, Staging, Production)
2. Set different `baseUrl` values for each
3. Switch between them using the environment dropdown

---

## ⚠️ Common Issues & Solutions

### 1. "Route not found" Error
- **Solution**: Check that your backend is running and `baseUrl` is correct

### 2. "Unauthorized" Error
- **Solution**: Run the Login endpoint first, or refresh your token

### 3. "Validation Error"
- **Solution**: Check the request body matches the expected format
- Review the request examples in this README

### 4. "OTP expired"
- **Solution**: Request a new OTP using "Resend OTP" endpoint

### 5. File Upload Not Working
- **Solution**: Ensure "Content-Type" is set to `multipart/form-data`
- Use the "form-data" body type in Postman

### 6. CORS Errors
- **Solution**: This shouldn't affect Postman, but if testing from browser:
  - Add your origin to `CORS_ORIGIN` in backend `.env`
  - Restart backend server

---

## 📊 Response Format

All endpoints follow a consistent response format:

### Success Response
```json
{
  "success": true,
  "data": {
    // Response data here
  }
}
```

### Error Response
```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable error message"
  }
}
```

### Paginated Response
```json
{
  "success": true,
  "data": {
    "items": [ /* array of items */ ],
    "pagination": {
      "total": 100,
      "page": 1,
      "limit": 20,
      "totalPages": 5
    }
  }
}
```

---

## 🎓 Tips for Effective Testing

1. **Use Environments**: Create separate environments for local, staging, and production
2. **Test in Order**: Follow the authentication flow before testing protected endpoints
3. **Check Console**: View Postman console (bottom left) for detailed request/response logs
4. **Save Examples**: Save response examples for documentation
5. **Use Collections Runner**: Run entire folders to test multiple endpoints sequentially
6. **Monitor API**: Use Postman Monitors to track API uptime and performance
7. **Generate Documentation**: Use Postman's documentation feature to share API docs with team

---

## 📚 Additional Resources

- [Backend Implementation Plan](IMPLEMENTATION_PLAN.md)
- [PostgreSQL Deployment Guide](POSTGRESQL_DEPLOYMENT_GUIDE.md)
- [Backend README](../README.md)

---

## 🤝 Contributing

If you find issues or want to add more test cases:
1. Test the endpoint manually
2. Add the request to the collection
3. Include example request body
4. Add test scripts if applicable
5. Update this README

---

## ✅ Testing Checklist

- [ ] Import collection into Postman
- [ ] Update `baseUrl` variable
- [ ] Test health check endpoint
- [ ] Register a new user
- [ ] Verify email with OTP
- [ ] Test user profile endpoints
- [ ] Browse content endpoints
- [ ] Complete a lesson
- [ ] Test search functionality
- [ ] Login as admin (if available)
- [ ] Test admin endpoints
- [ ] Test error handling
- [ ] Test pagination
- [ ] Test file uploads

---

**Happy Testing! 🚀**

If you encounter any issues or need help, refer to the [PostgreSQL Deployment Guide](POSTGRESQL_DEPLOYMENT_GUIDE.md) or check your backend logs.

