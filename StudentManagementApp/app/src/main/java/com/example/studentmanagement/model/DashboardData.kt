package com.example.studentmanagement.model

import com.google.gson.annotations.SerializedName

/**
 * Dashboard data response from GET /api/dashboard/{studentId}
 * Populated by Member 4's backend; attendance/courses/assignments
 * will also be used by Member 2 and Member 3 features.
 */
data class DashboardData(
    @SerializedName("studentId") val studentId: Long,
    @SerializedName("attendancePercentage") val attendancePercentage: Int = 0,
    @SerializedName("enrolledCourses") val enrolledCourses: Int = 0,
    @SerializedName("upcomingAssignments") val upcomingAssignments: Int = 0,
    @SerializedName("unreadNotifications") val unreadNotifications: Int = 0
)

/**
 * Generic API response wrapper for endpoints that return a list or simple status.
 */
data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: T? = null
)
