package com.example.studentmanagement.repository

import android.content.Context
import com.example.studentmanagement.api.RetrofitClient
import com.example.studentmanagement.auth.SessionManager
import com.example.studentmanagement.database.AppDatabase
import com.example.studentmanagement.database.AssignmentEntity
import com.example.studentmanagement.model.Assignment
import com.example.studentmanagement.model.SubmissionRequest
import com.example.studentmanagement.model.SubmissionStatus
import com.example.studentmanagement.utils.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Repository for assignments.
 *
 * UI → Repository → (Room primary cache) + (Retrofit optional sync)
 *
 * The app works fully offline using Room + mock data.
 * When Member 4's backend is live, Retrofit calls will sync fresh data.
 *
 * Integration hooks for Member 1 Dashboard:
 *   - getUpcomingAssignmentCount()
 *   - getNextAssignment()
 */
class AssignmentRepository(private val context: Context) {

    private val dao            = AppDatabase.getInstance(context).assignmentDao()
    private val sessionManager = SessionManager(context)

    // ─────────────────────────────────────────────────────────────
    // Sealed result wrapper
    // ─────────────────────────────────────────────────────────────

    sealed class Result<out T> {
        data class Success<T>(val data: T) : Result<T>()
        data class Error(val message: String) : Result<Nothing>()
    }

    // ─────────────────────────────────────────────────────────────
    // Observe (Flow — auto-updates RecyclerView)
    // ─────────────────────────────────────────────────────────────

    fun observeAssignments(studentId: Int): Flow<List<AssignmentEntity>> =
        dao.getAssignmentsByStudentId(studentId)

    // ─────────────────────────────────────────────────────────────
    // Load (seed mock → try API → cache)
    // ─────────────────────────────────────────────────────────────

    /**
     * Load assignments: seeds mock data on first run, then attempts API sync.
     * Room Flow handles pushing updates to the UI automatically.
     */
    suspend fun loadAssignments(studentId: Int): Result<List<AssignmentEntity>> =
        withContext(Dispatchers.IO) {
            // Attempt API sync (non-blocking — failure is safe)


            try {
                val token = sessionManager.getBearerToken()
                val response = RetrofitClient.api.getAssignments(studentId, token)
                if (response.isSuccessful) {
                    val apiData = response.body()
                    if (!apiData.isNullOrEmpty()) {
                        dao.insertAll(apiData.map { it.toEntity() })
                    }
                }
            } catch (e: Exception) {
                // API unavailable — Room data already available
            }

            Result.Success(dao.getAssignmentsByStudentIdOnce(studentId))
        }

    // ─────────────────────────────────────────────────────────────
    // Fetch single
    // ─────────────────────────────────────────────────────────────

    suspend fun getAssignmentById(assignmentId: Int): Result<AssignmentEntity> =
        withContext(Dispatchers.IO) {
            val entity = dao.getAssignmentById(assignmentId)
                ?: return@withContext Result.Error("Assignment not found")
            Result.Success(entity)
        }

    // ─────────────────────────────────────────────────────────────
    // Search
    // ─────────────────────────────────────────────────────────────

    suspend fun searchAssignments(studentId: Int, query: String): Result<List<AssignmentEntity>> =
        withContext(Dispatchers.IO) {
            val results = dao.searchAssignments(studentId, "%$query%")
            Result.Success(results)
        }

    // ─────────────────────────────────────────────────────────────
    // Submit
    // ─────────────────────────────────────────────────────────────

    suspend fun markAsSubmitted(assignmentId: Int): Result<Unit> =
        withContext(Dispatchers.IO) {
            val today = DateUtils.todayIso()
            // Update Room immediately (optimistic)
            dao.updateSubmissionStatus(assignmentId, SubmissionStatus.SUBMITTED.name, today)

            // Attempt API sync
            try {
                val token = sessionManager.getBearerToken()
                RetrofitClient.api.updateSubmission(
                    assignmentId, token,
                    SubmissionRequest(SubmissionStatus.SUBMITTED.name, today)
                )
            } catch (e: Exception) {
                // API unavailable — local state already updated
            }

            Result.Success(Unit)
        }

    // ─────────────────────────────────────────────────────────────
    // Dashboard integration helpers (Member 1)
    // ─────────────────────────────────────────────────────────────

    /** Count of upcoming (future deadline, not submitted) assignments. */
    suspend fun getUpcomingAssignmentCount(studentId: Int): Int =
        withContext(Dispatchers.IO) {
            dao.getUpcomingAssignmentCount(studentId, DateUtils.todayIso())
        }

    /** The nearest upcoming assignment, or null if none. */
    suspend fun getNextAssignment(studentId: Int): AssignmentEntity? =
        withContext(Dispatchers.IO) {
            dao.getNextAssignment(studentId, DateUtils.todayIso())
        }


    // ─────────────────────────────────────────────────────────────
    // Mapper
    // ─────────────────────────────────────────────────────────────

    private fun Assignment.toEntity() = AssignmentEntity(
        id = id, studentId = studentId, courseId = courseId,
        courseName = courseName, courseCode = courseCode,
        title = title, description = description,
        assignedDate = assignedDate, deadline = deadline,
        submissionStatus = submissionStatus, submittedDate = submittedDate
    )
}
