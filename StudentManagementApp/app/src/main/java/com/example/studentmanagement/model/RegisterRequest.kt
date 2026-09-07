package com.example.studentmanagement.model

import com.google.gson.annotations.SerializedName

/**
 * Request body for POST /api/auth/register
 * Matches Member 4's backend contract.
 */
data class RegisterRequest(
    @SerializedName("name") val name: String,
    @SerializedName("rollNumber") val rollNumber: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("department") val department: String,
    @SerializedName("year") val year: String
)
