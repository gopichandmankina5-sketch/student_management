package com.example.studentmanagement.repository

import android.content.Context

import com.example.studentmanagement.auth.SessionManager
import com.example.studentmanagement.database.AppDatabase
import com.example.studentmanagement.database.AssignmentEntity
import com.example.studentmanagement.model.Assignment
import com.example.studentmanagement.model.SubmissionRequest
import com.example.studentmanagement.model.SubmissionStatus
import com.example.studentmanagement.utils.DateUtils
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Repository for assignments.
 */
class AssignmentRepository(private val context: Context) {

    private val dao            = AppDatabase.getInstance(context).assignmentDao()
    private val sessionManager = SessionManager(context)
    private val firestore      = FirebaseFirestore.getInstance()

    sealed class Result<out T> {
        data class Success<T>(val data: T) : Result<T>()
        data class Error(val message: String) : Result<Nothing>()
    }

    fun observeAssignments(studentId: Int): Flow<List<AssignmentEntity>> =
        dao.getAssignmentsByStudentId(studentId)

    suspend fun loadAssignments(studentId: Int): Result<List<AssignmentEntity>> =
        withContext(Dispatchers.IO) {
            try {
                // Fetch from Firestore
                val snapshot = firestore.collection("assignments")
                    .whereEqualTo("studentId", studentId)
                    .get()
                    .await()
                
                val apiData = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    AssignmentEntity(
                        id = doc.id.hashCode(),
                        studentId = studentId,
                        courseId = (data["courseId"] as? Long)?.toInt() ?: 0,
                        courseName = data["courseName"] as? String ?: "",
                        courseCode = data["courseCode"] as? String ?: "",
                        title = data["title"] as? String ?: "",
                        description = data["description"] as? String ?: "",
                        assignedDate = data["assignedDate"] as? String ?: "",
                        deadline = data["deadline"] as? String ?: "",
                        submissionStatus = data["submissionStatus"] as? String ?: "PENDING",
                        submittedDate = data["submittedDate"] as? String
                    )
                }
                
                if (apiData.isNotEmpty()) {
                    dao.insertAll(apiData)
                }
            } catch (e: Exception) {
                // API unavailable — Room data already available
            }

            Result.Success(dao.getAssignmentsByStudentIdOnce(studentId))
        }

    suspend fun getAssignmentById(assignmentId: Int): Result<AssignmentEntity> =
        withContext(Dispatchers.IO) {
            val entity = dao.getAssignmentById(assignmentId)
                ?: return@withContext Result.Error("Assignment not found")
            Result.Success(entity)
        }

    suspend fun searchAssignments(studentId: Int, query: String): Result<List<AssignmentEntity>> =
        withContext(Dispatchers.IO) {
            val results = dao.searchAssignments(studentId, "%$query%")
            Result.Success(results)
        }

    suspend fun markAsSubmitted(assignmentId: Int): Result<Unit> =
        withContext(Dispatchers.IO) {
            val today = DateUtils.todayIso()
            // Update Room immediately (optimistic)
            dao.updateSubmissionStatus(assignmentId, SubmissionStatus.SUBMITTED.name, today)

            // Attempt API sync
            try {
                // Find document by hashcode id in Firestore (not ideal but works for this structure)
                val snapshot = firestore.collection("assignments").get().await()
                val targetDoc = snapshot.documents.find { it.id.hashCode() == assignmentId }
                
                if (targetDoc != null) {
                    firestore.collection("assignments").document(targetDoc.id)
                        .update(
                            "submissionStatus", SubmissionStatus.SUBMITTED.name,
                            "submittedDate", today
                        ).await()
                }
            } catch (e: Exception) {
                // API unavailable — local state already updated
            }

            Result.Success(Unit)
        }

    suspend fun getUpcomingAssignmentCount(studentId: Int): Int =
        withContext(Dispatchers.IO) {
            dao.getUpcomingAssignmentCount(studentId, DateUtils.todayIso())
        }

    suspend fun getNextAssignment(studentId: Int): AssignmentEntity? =
        withContext(Dispatchers.IO) {
            dao.getNextAssignment(studentId, DateUtils.todayIso())
        }
}
