package com.example.studentmanagement.model

/**
 * Core Student domain model used throughout the app.
 * Shared between UI, Repository, and Database layers.
 *
 * Note: passwordHash stores a SHA-256 hash. Plaintext passwords are NEVER stored.
 */
data class Student(
    val id: Long = 0,
    val name: String,
    val rollNumber: String,
    val email: String,
    val department: String,
    val year: String,
    // SHA-256 hash of the password — never store plaintext
    val passwordHash: String = ""
)
