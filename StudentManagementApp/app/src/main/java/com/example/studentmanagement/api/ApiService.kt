package com.example.studentmanagement.api

import com.example.studentmanagement.model.ApiResponse
import com.example.studentmanagement.model.DashboardData
import com.example.studentmanagement.model.LoginRequest
import com.example.studentmanagement.model.LoginResponse
import com.example.studentmanagement.model.RegisterRequest
import com.example.studentmanagement.model.Student
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
 *   Response: { "id": 1, "name": "...", "rollNumber": "...", "email": "...",
 *               "department": "...", "year": "..." }
 *
 * PUT /api/students/{id}
 *   Headers:  Authorization: Bearer <token>
 *   Request:  { "name": "...", "rollNumber": "...", "department": "...", "year": "..." }
 *   Response: Updated student object
 *
 * GET /api/dashboard/{studentId}
 *   Headers:  Authorization: Bearer <token>
 *   Response: { "studentId": 1, "attendancePercentage": 82,
 *               "enrolledCourses": 6, "upcomingAssignments": 3,
 *               "unreadNotifications": 2 }
 * ──────────────────────────────────────────────────────
 */
interface ApiService {

    // ─────────────────────────────────────────────────────────────
    // Authentication
    // ─────────────────────────────────────────────────────────────

    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("api/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<LoginResponse>

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
    // Dashboard (aggregated data — connects Member 2 & 3 modules)
    // ─────────────────────────────────────────────────────────────

    @GET("api/dashboard/{studentId}")
    suspend fun getDashboard(
        @Path("studentId") studentId: Long,
        @Header("Authorization") authToken: String
    ): Response<DashboardData>
}
