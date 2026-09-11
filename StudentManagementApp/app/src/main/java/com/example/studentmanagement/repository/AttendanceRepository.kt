package com.example.studentmanagement.repository

import com.example.studentmanagement.auth.SessionManager
import com.example.studentmanagement.model.AttendanceRecord
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Repository for managing Attendance.
 * Note: Member 2 has not implemented local Room caching for Attendance yet.
 * Therefore, we implement this purely with Firestore as the source of truth for Member 3.
 */
class AttendanceRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    sealed class Result<out T> {
        data class Success<T>(val data: T) : Result<T>()
        data class Error(val message: String) : Result<Nothing>()
    }

    /**
     * Fetch attendance records for a specific course and date.
     * Only Faculty should be calling this for their own courses.
     */
    suspend fun getAttendanceForCourseAndDate(courseId: String, date: String): Result<List<AttendanceRecord>> =
        withContext(Dispatchers.IO) {
            try {
                val snapshot = firestore.collection("attendance")
                    .whereEqualTo("courseId", courseId)
                    .whereEqualTo("date", date)
                    .get()
                    .await()

                val records = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    AttendanceRecord(
                        id = doc.id,
                        courseId = data["courseId"] as? String ?: "",
                        studentUid = data["studentUid"] as? String ?: "",
                        facultyUid = data["facultyUid"] as? String ?: "",
                        date = data["date"] as? String ?: "",
                        status = data["status"] as? String ?: "NOT_MARKED",
                        studentName = data["studentName"] as? String ?: ""
                    )
                }
                Result.Success(records)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Failed to load attendance")
            }
        }

    /**
     * Batch save attendance records.
     */
    suspend fun saveAttendanceBatch(records: List<AttendanceRecord>): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val batch = firestore.batch()
                val collection = firestore.collection("attendance")

                records.forEach { record ->
                    // Expected ID format: courseId_studentUid_date
                    val docId = "${record.courseId}_${record.studentUid}_${record.date}"
                    val docRef = collection.document(docId)

                    val data = hashMapOf(
                        "courseId" to record.courseId,
                        "studentUid" to record.studentUid,
                        "facultyUid" to record.facultyUid,
                        "date" to record.date,
                        "status" to record.status,
                        "studentName" to record.studentName
                    )
                    batch.set(docRef, data)
                }

                batch.commit().await()
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Failed to save attendance")
            }
        }

    /**
     * Fetch all attendance records for a specific student.
     */
    suspend fun getAttendanceForStudent(studentUid: String): Result<List<AttendanceRecord>> =
        withContext(Dispatchers.IO) {
            try {
                val snapshot = firestore.collection("attendance")
                    .whereEqualTo("studentUid", studentUid)
                    .get()
                    .await()

                val records = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    AttendanceRecord(
                        id = doc.id,
                        courseId = data["courseId"] as? String ?: "",
                        studentUid = data["studentUid"] as? String ?: "",
                        facultyUid = data["facultyUid"] as? String ?: "",
                        date = data["date"] as? String ?: "",
                        status = data["status"] as? String ?: "NOT_MARKED",
                        studentName = data["studentName"] as? String ?: ""
                    )
                }
                Result.Success(records)
            } catch (e: Exception) {
                Result.Error(e.message ?: "Failed to load student attendance")
            }
        }
}
