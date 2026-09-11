package com.example.studentmanagement.model

/**
 * Domain model for an Attendance record.
 */
data class AttendanceRecord(
    val id: String = "",                 // Expected format: courseId_studentUid_date
    val courseId: String = "",
    val studentUid: String = "",
    val facultyUid: String = "",
    val date: String = "",               // yyyy-MM-dd
    val status: String = "NOT_MARKED",   // PRESENT, ABSENT, NOT_MARKED
    val studentName: String = ""         // Cached for convenience
)

enum class AttendanceStatus {
    PRESENT,
    ABSENT,
    NOT_MARKED;

    companion object {
        fun fromString(value: String): AttendanceStatus = when (value.uppercase()) {
            "PRESENT" -> PRESENT
            "ABSENT" -> ABSENT
            else -> NOT_MARKED
        }
    }
}
