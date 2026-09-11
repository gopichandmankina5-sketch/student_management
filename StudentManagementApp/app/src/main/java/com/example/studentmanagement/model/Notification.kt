package com.example.studentmanagement.model



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
    val id: String,
    val studentUid: String,
    val title: String,
    val message: String,
    val type: String,           // NotificationType name
    val createdAt: String,
    val isRead: Boolean = false,
    val relatedAssignmentId: String? = null
)

/** Request body for marking a single notification read. */
data class MarkReadRequest(
    val isRead: Boolean = true
)
