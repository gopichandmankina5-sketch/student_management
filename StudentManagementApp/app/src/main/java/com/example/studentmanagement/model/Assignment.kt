package com.example.studentmanagement.model

import com.google.gson.annotations.SerializedName

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
    @SerializedName("id")               val id: Int,
    @SerializedName("studentId")        val studentId: Int,
    @SerializedName("courseId")         val courseId: Int,
    @SerializedName("courseName")       val courseName: String,
    @SerializedName("courseCode")       val courseCode: String,
    @SerializedName("title")            val title: String,
    @SerializedName("description")      val description: String,
    @SerializedName("assignedDate")     val assignedDate: String,       // yyyy-MM-dd
    @SerializedName("deadline")         val deadline: String,           // yyyy-MM-dd
    @SerializedName("submissionStatus") val submissionStatus: String,   // SubmissionStatus name
    @SerializedName("submittedDate")    val submittedDate: String? = null
)

/** Request body sent to PUT /api/assignments/{id}/submission */
data class SubmissionRequest(
    @SerializedName("submissionStatus") val submissionStatus: String,
    @SerializedName("submittedDate")    val submittedDate: String
)
