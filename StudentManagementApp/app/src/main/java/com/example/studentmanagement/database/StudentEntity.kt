package com.example.studentmanagement.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room database entity for student records.
 *
 * email is indexed with a unique constraint to prevent duplicate accounts.
 * passwordHash stores a SHA-256 hash — plaintext passwords are NEVER stored.
 */
@Entity(
    tableName = "students",
    indices = [Index(value = ["email"], unique = true)]
)
data class StudentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val rollNumber: String,
    val email: String,
    val department: String,
    val year: String,
    // SHA-256 hash of the user's password
    val passwordHash: String = ""
)
