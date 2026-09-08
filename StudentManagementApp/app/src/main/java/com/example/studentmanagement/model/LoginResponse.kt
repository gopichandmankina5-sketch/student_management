package com.example.studentmanagement.model

import com.google.gson.annotations.SerializedName

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
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("studentId") val studentId: Long? = null,
    @SerializedName("userId") val userId: Long? = null,
    @SerializedName("role") val role: String? = null,
    @SerializedName("token") val token: String? = null
)
