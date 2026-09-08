package com.example.studentmanagement.model



/**
 * Dashboard data response from GET /api/dashboard/{studentId}
 * Populated by Member 4's backend; attendance/courses/assignments
 * will also be used by Member 2 and Member 3 features.
 */
data class DashboardData(
    val studentId: Long,
    val attendancePercentage: Int = 0,
    val enrolledCourses: Int = 0,
    val upcomingAssignments: Int = 0,
    val unreadNotifications: Int = 0
)

/**
 * Generic API response wrapper for endpoints that return a list or simple status.
 */
data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null
)
