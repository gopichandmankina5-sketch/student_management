package com.example.studentmanagement.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for assignments.
 *
 * Dates stored as ISO-8601 strings (yyyy-MM-dd).
 * submissionStatus stores the SubmissionStatus enum name as a String.
 *
 * courseId / courseName / courseCode mirror Member 2's course data so
 * the two modules can be joined later without schema changes.
 */
@Entity(
    tableName = "assignments",
    indices = [Index(value = ["studentId"]), Index(value = ["deadline"])]
)
data class AssignmentEntity(
    @PrimaryKey val id: Int,
    val studentId: Int,
    val courseId: Int,
    val courseName: String,
    val courseCode: String,
    val title: String,
    val description: String,
    val assignedDate: String,           // yyyy-MM-dd
    val deadline: String,               // yyyy-MM-dd
    val submissionStatus: String,       // SubmissionStatus.name
    val submittedDate: String? = null   // yyyy-MM-dd or null
)
