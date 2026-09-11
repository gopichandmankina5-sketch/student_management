package com.example.studentmanagement.model



/** Submission states for an assignment. */
enum class SubmissionStatus {
    NOT_SUBMITTED,
    SUBMITTED,
    OVERDUE,
    DUE_TODAY;

    companion object {
        fun fromString(value: String): SubmissionStatus = when (value.uppercase()) {
            "SUBMITTED"     -> SUBMITTED
            "OVERDUE"       -> OVERDUE
            "DUE_TODAY"     -> DUE_TODAY
            else            -> NOT_SUBMITTED
        }
    }
}

/**
 * Domain / API model for an assignment.
 *
 * Dates are stored as ISO-8601 strings (yyyy-MM-dd).
 * Use [com.example.studentmanagement.utils.DateUtils] for all date operations.
 */
data class Assignment(
    val id: String,
    val studentUid: String,
    val facultyUid: String,
    val courseId: String,
    val courseName: String,
    val courseCode: String,
    val title: String,
    val description: String,
    val assignedDate: String,       // yyyy-MM-dd
    val deadline: String,           // yyyy-MM-dd
    val submissionStatus: String,   // SubmissionStatus name
    val submittedDate: String? = null
)

/** Request body sent to PUT /api/assignments/{id}/submission */
data class SubmissionRequest(
    val submissionStatus: String,
    val submittedDate: String
)
