package com.example.studentmanagement.repository

import android.content.Context

import com.example.studentmanagement.auth.SessionManager
import com.example.studentmanagement.database.AppDatabase
import com.example.studentmanagement.database.AssignmentEntity
import com.example.studentmanagement.model.Assignment
import com.example.studentmanagement.model.SubmissionRequest
import com.example.studentmanagement.model.SubmissionStatus
import com.example.studentmanagement.utils.DateUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Repository for assignments.
 *
 * Firestore is the source of truth.
 * Room is used as a local offline cache.
 *
 * CRITICAL: Firestore documents use "studentUid" (string Firebase UID).
 * Room uses integer studentId as a local cache key only.
 */
class AssignmentRepository(private val context: Context) {

    private val dao            = AppDatabase.getInstance(context).assignmentDao()
    private val sessionManager = SessionManager(context)
    private val firestore      = FirebaseFirestore.getInstance()
    private val auth           = FirebaseAuth.getInstance()

    sealed class Result<out T> {
        data class Success<T>(val data: T) : Result<T>()
        data class Error(val message: String) : Result<Nothing>()
    }

    fun observeAssignments(studentUid: String): Flow<List<AssignmentEntity>> =
        dao.getAssignmentsByStudentId(studentUid)

    suspend fun loadAssignments(studentUid: String): Result<List<AssignmentEntity>> =
        withContext(Dispatchers.IO) {
            val uid = auth.currentUser?.uid
                ?: sessionManager.getUid().takeIf { it.isNotEmpty() }

            if (uid != null) {
                try {
                    val snapshot = firestore.collection("assignments")
                        .whereEqualTo("studentUid", uid)
                        .get()
                        .await()

                    val apiData = snapshot.documents.mapNotNull { doc ->
                        val data = doc.data ?: return@mapNotNull null
                        AssignmentEntity(
                            id = doc.id,
                            studentUid = uid,
                            facultyUid = data["facultyUid"] as? String ?: "",
                            courseId = data["courseId"] as? String ?: "",
                            courseName = data["courseName"] as? String ?: "",
                            courseCode = data["courseCode"] as? String ?: "",
                            title = data["title"] as? String ?: "",
                            description = data["description"] as? String ?: "",
                            assignedDate = data["assignedDate"] as? String ?: "",
                            deadline = data["deadline"] as? String ?: "",
                            submissionStatus = data["submissionStatus"] as? String ?: "NOT_SUBMITTED",
                            submittedDate = data["submittedDate"] as? String
                        )
                    }

                    if (apiData.isNotEmpty()) {
                        dao.insertAll(apiData)
                    }
                } catch (e: Exception) {
                    // Firestore unavailable
                }
            }

            Result.Success(dao.getAssignmentsByStudentIdOnce(studentUid))
        }

    suspend fun getAssignmentById(assignmentId: String): Result<AssignmentEntity> =
        withContext(Dispatchers.IO) {
            val entity = dao.getAssignmentById(assignmentId)
                ?: return@withContext Result.Error("Assignment not found")
            Result.Success(entity)
        }

    suspend fun searchAssignments(studentUid: String, query: String): Result<List<AssignmentEntity>> =
        withContext(Dispatchers.IO) {
            val results = dao.searchAssignments(studentUid, "%$query%")
            Result.Success(results)
        }

    suspend fun submitAssignment(assignmentId: String, studentUid: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            val today = DateUtils.todayIso()

            // 1. Update local Room DB
            dao.updateSubmissionStatus(assignmentId, SubmissionStatus.SUBMITTED.name, today)

            // 2. Update Firestore
            val uid = sessionManager.getUid()
            if (uid.isNotEmpty()) {
                try {
                    firestore.collection("assignments").document(assignmentId)
                        .update(
                            "submissionStatus", SubmissionStatus.SUBMITTED.name,
                            "submittedDate", today
                        ).await()
                } catch (e: Exception) {
                    // Local state already updated — Firestore sync failure is non-critical
                }
            }

            Result.Success(Unit)
        }

    suspend fun getUpcomingAssignmentCount(studentUid: String): Int =
        withContext(Dispatchers.IO) {
            dao.getUpcomingAssignmentCount(studentUid, DateUtils.todayIso())
        }

    suspend fun getNextAssignment(studentUid: String): AssignmentEntity? =
        withContext(Dispatchers.IO) {
            dao.getNextAssignment(studentUid, DateUtils.todayIso())
        }
}
