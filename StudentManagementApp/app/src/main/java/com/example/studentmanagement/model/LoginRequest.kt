package com.example.studentmanagement.model



/**
 * Request body for POST /api/auth/login
 * Matches Member 4's backend contract.
 */
data class LoginRequest(
    val email: String,
    val password: String
)
