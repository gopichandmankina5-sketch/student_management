package com.example.studentmanagement.model



/**
 * Request body for POST /api/auth/register
 * Matches Member 4's backend contract.
 */
data class RegisterRequest(
    val name: String,
    val rollNumber: String,
    val email: String,
    val password: String,
    val department: String,
    val year: String
)
