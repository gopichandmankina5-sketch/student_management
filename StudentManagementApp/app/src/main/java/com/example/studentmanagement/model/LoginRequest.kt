package com.example.studentmanagement.model

import com.google.gson.annotations.SerializedName

/**
 * Request body for POST /api/auth/login
 * Matches Member 4's backend contract.
 */
data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)
