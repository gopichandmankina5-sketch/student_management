# Smart Student Management & Learning — Android App
## Member 1 Module: Authentication + Student Profile + Student Dashboard

---

## Table of Contents
1. [Project Description](#project-description)
2. [Member 1 Responsibilities](#member-1-responsibilities)
3. [Technology Stack](#technology-stack)
4. [Project Structure](#project-structure)
5. [Setup Instructions](#setup-instructions)
6. [API Configuration](#api-configuration)
7. [API Contract (Backend Integration)](#api-contract-backend-integration)
8. [Testing Checklist](#testing-checklist)
9. [Team Integration Guide](#team-integration-guide)

---

## Project Description
A collaborative Android application for student academic management. The system provides student authentication, profile management, attendance tracking, course enrollment, assignment management, and notifications. Developed by a 4-member team using an MVVM-style architecture with Room database and Retrofit for REST API integration.

---

## Member 1 Responsibilities
- ✅ Splash Screen (session-aware routing)
- ✅ Student Registration (full validation, Room persistence)
- ✅ Login (credential verification, session management)
- ✅ Session Management (SharedPreferences-based, logout-safe)
- ✅ Student Dashboard (stats, quick navigation, team placeholders)
- ✅ Student Profile (view/edit modes, Room CRUD)
- ✅ Input Validation (email, password, all required fields)
- ✅ Error Handling (local, network, database)
- ✅ REST API layer preparation (Retrofit, ready for Member 4's backend)
- ✅ Password security (SHA-256 hashing, never plaintext)

---

## Technology Stack
| Technology | Version |
|---|---|
| Language | Kotlin |
| Min SDK | 24 |
| Target/Compile SDK | 35 |
| UI | XML Layouts + Material Design 3 |
| Architecture | MVVM-style (Repository pattern) |
| Local DB | Room 2.6.1 |
| HTTP Client | Retrofit 2.11.0 + OkHttp 4.12.0 |
| JSON | Gson |
| Async | Kotlin Coroutines |
| ViewBinding | Enabled |

---

## Project Structure
```
app/src/main/java/com/example/studentmanagement/
│
├── auth/
│   ├── SplashActivity.kt       — Entry point, session routing
│   ├── LoginActivity.kt        — Email/password login
│   ├── RegisterActivity.kt     — 7-field registration
│   └── SessionManager.kt       — SharedPreferences session state
│
├── dashboard/
│   └── DashboardActivity.kt    — Main student hub with stats & nav
│
├── profile/
│   └── ProfileActivity.kt      — View/edit student profile
│
├── model/
│   ├── Student.kt              — Core domain model
│   ├── LoginRequest.kt         — API request body
│   ├── LoginResponse.kt        — API response body
│   ├── RegisterRequest.kt      — API request body
│   └── DashboardData.kt        — Dashboard API response + ApiResponse<T>
│
├── database/
│   ├── AppDatabase.kt          — Room singleton
│   ├── StudentDao.kt           — CRUD operations
│   └── StudentEntity.kt        — Room @Entity (with unique email index)
│
├── api/
│   ├── ApiService.kt           — Retrofit interface (5 endpoints)
│   └── RetrofitClient.kt       — OkHttp + Retrofit singleton
│
├── repository/
│   └── StudentRepository.kt    — Single source of truth (local + API)
│
└── utils/
    ├── Constants.kt            — BASE_URL, SharedPrefs keys, DB config
    └── ValidationUtils.kt      — Reusable field validators
```

---

## Setup Instructions

### 1. Clone & Open
```bash
git clone <repo-url>
# Open StudentManagementApp/ in Android Studio
```

### 2. Sync Gradle
In Android Studio: **File → Sync Project with Gradle Files**

### 3. Run (Emulator or Device)
- **Emulator**: Use any AVD with API 24+
- **Physical device**: Enable USB debugging

### 4. First Launch
- App opens at the Splash screen
- Since no account exists, it redirects to Login
- Tap "Sign Up" to create an account
- After registration, Dashboard opens automatically

---

## API Configuration

### Changing the Backend URL
The backend URL is configured in **one single place**:

```kotlin
// utils/Constants.kt
const val BASE_URL = "http://10.0.2.2:3000/"
```

| Environment | URL |
|---|---|
| Android Emulator (localhost) | `http://10.0.2.2:3000/` |
| Physical device (same WiFi) | `http://192.168.x.x:3000/` |
| Deployed server | `https://your-domain.com/` |

> **Do NOT** change the URL anywhere else. Only edit `Constants.kt`.

---

## API Contract (Backend Integration)

Member 4 (Node.js/Express + MySQL) must implement the following endpoints.

### Authentication

#### POST `/api/auth/login`
**Request:**
```json
{
  "email": "student@example.com",
  "password": "password123"
}
```
**Response (success):**
```json
{
  "success": true,
  "message": "Login successful",
  "studentId": 1,
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```
**Response (failure):**
```json
{
  "success": false,
  "message": "Invalid email or password"
}
```

#### POST `/api/auth/register`
**Request:**
```json
{
  "name": "John Doe",
  "rollNumber": "CS2024001",
  "email": "john@university.edu",
  "password": "password123",
  "department": "Computer Science",
  "year": "3rd Year / Semester 5"
}
```
**Response:**
```json
{
  "success": true,
  "message": "Registration successful",
  "studentId": 1,
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

---

### Student Profile

#### GET `/api/students/{id}`
**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
{
  "id": 1,
  "name": "John Doe",
  "rollNumber": "CS2024001",
  "email": "john@university.edu",
  "department": "Computer Science",
  "year": "3rd Year / Semester 5"
}
```

#### PUT `/api/students/{id}`
**Headers:** `Authorization: Bearer <token>`

**Request:**
```json
{
  "name": "John Doe Updated",
  "rollNumber": "CS2024001",
  "department": "Information Technology",
  "year": "4th Year / Semester 7"
}
```
**Response:** Updated student object (same structure as GET)

---

### Dashboard

#### GET `/api/dashboard/{studentId}`
**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
{
  "studentId": 1,
  "attendancePercentage": 82,
  "enrolledCourses": 6,
  "upcomingAssignments": 3,
  "unreadNotifications": 2
}
```

---

## Testing Checklist

| Test | Expected Result |
|---|---|
| App launches | Splash screen appears for ~1.5 seconds |
| First launch (no session) | Redirects to Login |
| Tap "Sign Up" | Opens Registration screen |
| Submit empty fields | Error shown on each empty field |
| Invalid email | "Please enter a valid email address" error |
| Password < 6 chars | "Password must be at least 6 characters" error |
| Password mismatch | "Passwords do not match" error |
| Valid registration | Dashboard opens; name shown in greeting |
| Restart app | Stays logged in (Dashboard shown) |
| Dashboard loads | Student name, dept, year displayed |
| Tap profile card | Profile screen opens |
| View profile | All fields shown, read-only |
| Tap "Edit Profile" | Fields become editable |
| Save valid profile | Success snackbar; view mode restored |
| Tap "Logout" | Confirmation dialog → Login screen |
| Restart after logout | Login screen shown (session cleared) |
| API unavailable | App runs normally on local data; no crash |

---

## Team Integration Guide

### Member 2 — Attendance & Courses
In `DashboardActivity.kt`, locate the placeholder click listeners:
```kotlin
binding.navAttendance.setOnClickListener { ... }
binding.navCourses.setOnClickListener { ... }
```
Replace the `showComingSoonMessage()` calls with your Activity intents.

Update the stat values:
```kotlin
binding.tvAttendanceValue.text = "82%"   // Replace with real data
binding.tvCoursesValue.text = "6"        // Replace with real data
```

### Member 3 — Assignments & Notifications
In `DashboardActivity.kt`, locate:
```kotlin
binding.navAssignments.setOnClickListener { ... }
binding.navNotifications.setOnClickListener { ... }
```
Replace the `showComingSoonMessage()` calls with your Activity intents.

Update the stat values:
```kotlin
binding.tvAssignmentsValue.text = "3"   // Replace with real data
binding.tvNotifValue.text = "2"         // Replace with real data
```

### Member 4 — Backend API
1. Update `Constants.BASE_URL` to your deployed server URL
2. In `StudentRepository.kt`, uncomment the Retrofit API stubs and replace the local Room implementations in `login()` and `register()`
3. The `ApiService.kt` endpoints are already defined; implement the matching Express routes
4. The dashboard stub in `DashboardActivity.kt` (`loadDashboardFromApi()`) is ready to be uncommented

---

*Smart Student Management App — Member 1 Module — September 2026*
