package com.example.studentmanagement.model

import com.google.gson.annotations.SerializedName

/** Category of notification for display/filtering. */
enum class NotificationType {
    ACADEMIC,
    ASSIGNMENT,
    ATTENDANCE,
    GENERAL;

    companion object {
        fun fromString(value: String): NotificationType = when (value.uppercase()) {
            "ACADEMIC"   -> ACADEMIC
            "ASSIGNMENT" -> ASSIGNMENT
            "ATTENDANCE" -> ATTENDANCE
            else         -> GENERAL
        }
    }

    fun emoji(): String = when (this) {
        ACADEMIC   -> "📢"
        ASSIGNMENT -> "🔔"
        ATTENDANCE -> "⚠️"
        GENERAL    -> "ℹ️"
    }
}

/**
 * Domain / API model for a notification.
 *
 * [createdAt] is an ISO-8601 string (yyyy-MM-dd'T'HH:mm:ss or yyyy-MM-dd).
 * [relatedAssignmentId] is non-null for ASSIGNMENT notifications; use it to
 * deep-link into AssignmentDetailsActivity.
 */
data class Notification(
    @SerializedName("id")                  val id: Int,
    @SerializedName("studentId")           val studentId: Int,
    @SerializedName("title")               val title: String,
    @SerializedName("message")             val message: String,
    @SerializedName("type")                val type: String,           // NotificationType name
    @SerializedName("createdAt")           val createdAt: String,
    @SerializedName("isRead")              val isRead: Boolean = false,
    @SerializedName("relatedAssignmentId") val relatedAssignmentId: Int? = null
)

/** Request body for marking a single notification read. */
data class MarkReadRequest(
    @SerializedName("isRead") val isRead: Boolean = true
)
