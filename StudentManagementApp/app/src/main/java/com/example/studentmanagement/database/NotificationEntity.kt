package com.example.studentmanagement.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for notifications.
 *
 * [type] stores NotificationType.name as a String.
 * [relatedAssignmentId] links ASSIGNMENT-type notifications to an AssignmentEntity.
 * [createdAt] is stored as an ISO-8601 string for easy sorting.
 */
@Entity(
    tableName = "notifications",
    indices = [Index(value = ["studentId"]), Index(value = ["isRead"])]
)
data class NotificationEntity(
    @PrimaryKey val id: Int,
    val studentId: Int,
    val title: String,
    val message: String,
    val type: String,                       // NotificationType.name
    val createdAt: String,                  // yyyy-MM-dd'T'HH:mm:ss or yyyy-MM-dd
    val isRead: Boolean = false,
    val relatedAssignmentId: Int? = null    // non-null → deep-link to assignment
)
