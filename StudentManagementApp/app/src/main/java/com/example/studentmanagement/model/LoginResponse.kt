package com.example.studentmanagement.model



/**
 * Response from POST /api/auth/login and POST /api/auth/register
 * Matches Member 4's backend contract.
 *
 * Example:
 * {
 *   "success": true,
 *   "message": "Login successful",
 *   "studentId": 1,
 *   "token": "eyJhbGci..."
 * }
 */
data class LoginResponse(
    val success: Boolean,
    val message: String,
    val studentId: Long? = null,
    val userId: Long? = null,
    val role: String? = null,
    val token: String? = null
)
