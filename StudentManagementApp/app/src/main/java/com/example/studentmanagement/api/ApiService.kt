package com.example.studentmanagement.api

import com.example.studentmanagement.model.ApiResponse
import com.example.studentmanagement.model.Assignment
import com.example.studentmanagement.model.DashboardData
import com.example.studentmanagement.model.LoginRequest
import com.example.studentmanagement.model.LoginResponse
import com.example.studentmanagement.model.MarkReadRequest
import com.example.studentmanagement.model.Notification
import com.example.studentmanagement.model.RegisterRequest
import com.example.studentmanagement.model.Student
import com.example.studentmanagement.model.SubmissionRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Retrofit API service interface.
 *
 * BACKEND CONTRACT (Member 4 — Node.js/Express + MySQL):
 * ──────────────────────────────────────────────────────
 *
 * POST /api/auth/login
 *   Request:  { "email": "...", "password": "..." }
 *   Response: { "success": true, "message": "...", "studentId": 1, "token": "..." }
 *
 * POST /api/auth/register
 *   Request:  { "name": "...", "rollNumber": "...", "email": "...",
 *               "password": "...", "department": "...", "year": "..." }
 *   Response: { "success": true, "message": "...", "studentId": 1, "token": "..." }
 *
 * GET /api/students/{id}
 *   Headers:  Authorization: Bearer <token>
 *   Response: { "id": 1, "name": "...", ... }
 *
 * PUT /api/students/{id}
 *   Headers:  Authorization: Bearer <token>
 *   Request/Response: Student object
 *
 * GET /api/dashboard/{studentId}
 *   Headers:  Authorization: Bearer <token>
 *   Response: { "studentId": 1, "attendancePercentage": 82,
 *               "enrolledCourses": 6, "upcomingAssignments": 3,
 *               "unreadNotifications": 2 }
 *
 * ── Member 3 endpoints (Assignment + Notification) ───
 *
 * GET  /api/assignments/{studentId}
 * GET  /api/assignments/detail/{assignmentId}
 * PUT  /api/assignments/{assignmentId}/submission
 *        Body: { "submissionStatus": "SUBMITTED", "submittedDate": "yyyy-MM-dd" }
 *
 * GET  /api/notifications/{studentId}
 * PUT  /api/notifications/{notificationId}/read
 *        Body: { "isRead": true }
 * PUT  /api/notifications/{studentId}/read-all
 * ──────────────────────────────────────────────────────
 */
interface ApiService {

    // ─────────────────────────────────────────────────────────────
    // Authentication
    // ─────────────────────────────────────────────────────────────

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<LoginResponse>

    // ─────────────────────────────────────────────────────────────
    // Student Profile
    // ─────────────────────────────────────────────────────────────

    @GET("api/students/{id}")
    suspend fun getStudent(
        @Path("id") id: Long,
        @Header("Authorization") authToken: String
    ): Response<Student>

    @PUT("api/students/{id}")
    suspend fun updateStudent(
        @Path("id") id: Long,
        @Header("Authorization") authToken: String,
        @Body student: Student
    ): Response<Student>

    // ─────────────────────────────────────────────────────────────
    // Dashboard
    // ─────────────────────────────────────────────────────────────

    @GET("api/dashboard/{studentId}")
    suspend fun getDashboard(
        @Path("studentId") studentId: Long,
        @Header("Authorization") authToken: String
    ): Response<DashboardData>

    // ─────────────────────────────────────────────────────────────
    // Member 3 — Assignments
    // ─────────────────────────────────────────────────────────────

    @GET("api/assignments/{studentId}")
    suspend fun getAssignments(
        @Path("studentId") studentId: Int,
        @Header("Authorization") authToken: String
    ): Response<List<Assignment>>

    @GET("api/assignments/detail/{assignmentId}")
    suspend fun getAssignmentById(
        @Path("assignmentId") assignmentId: Int,
        @Header("Authorization") authToken: String
    ): Response<Assignment>

    @PUT("api/assignments/{assignmentId}/submission")
    suspend fun updateSubmission(
        @Path("assignmentId") assignmentId: Int,
        @Header("Authorization") authToken: String,
        @Body request: SubmissionRequest
    ): Response<ApiResponse<Unit>>

    // ─────────────────────────────────────────────────────────────
    // Member 3 — Notifications
    // ─────────────────────────────────────────────────────────────

    @GET("api/notifications/{studentId}")
    suspend fun getNotifications(
        @Path("studentId") studentId: Int,
        @Header("Authorization") authToken: String
    ): Response<List<Notification>>

    @PUT("api/notifications/{notificationId}/read")
    suspend fun markNotificationRead(
        @Path("notificationId") notificationId: Int,
        @Header("Authorization") authToken: String,
        @Body request: MarkReadRequest
    ): Response<ApiResponse<Unit>>

    @PUT("api/notifications/{studentId}/read-all")
    suspend fun markAllNotificationsRead(
        @Path("studentId") studentId: Int,
        @Header("Authorization") authToken: String
    ): Response<ApiResponse<Unit>>
}
