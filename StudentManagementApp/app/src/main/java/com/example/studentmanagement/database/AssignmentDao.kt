package com.example.studentmanagement.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO for assignment CRUD operations.
 *
 * All suspend functions are safe to call from a coroutine (IO dispatcher).
 * Flow-returning queries emit automatically whenever the underlying table changes.
 */
@Dao
interface AssignmentDao {

    // ─────────────────────────────────────────────────────────────
    // Read
    // ─────────────────────────────────────────────────────────────

    /** Observe all assignments for a student, ordered by deadline ascending. */
    @Query("SELECT * FROM assignments WHERE studentUid = :studentUid ORDER BY deadline ASC")
    fun getAssignmentsByStudentId(studentUid: String): Flow<List<AssignmentEntity>>

    /** One-shot fetch of all assignments for a student. */
    @Query("SELECT * FROM assignments WHERE studentUid = :studentUid ORDER BY deadline ASC")
    suspend fun getAssignmentsByStudentIdOnce(studentUid: String): List<AssignmentEntity>

    /** Fetch a single assignment by its primary key. */
    @Query("SELECT * FROM assignments WHERE id = :assignmentId LIMIT 1")
    suspend fun getAssignmentById(assignmentId: String): AssignmentEntity?

    /**
     * Full-text search across title, course name, and course code.
     * Caller should wrap the query with '%' wildcards: e.g. "%java%".
     */
    @Query("""
        SELECT * FROM assignments
        WHERE studentUid = :studentUid
          AND (title LIKE :query OR courseName LIKE :query OR courseCode LIKE :query)
        ORDER BY deadline ASC
    """)
    suspend fun searchAssignments(studentUid: String, query: String): List<AssignmentEntity>

    /** Count of upcoming (future deadline, not submitted) assignments for a student. */
    @Query("""
        SELECT COUNT(*) FROM assignments
        WHERE studentUid = :studentUid
          AND deadline >= :today
          AND submissionStatus != 'SUBMITTED'
    """)
    suspend fun getUpcomingAssignmentCount(studentUid: String, today: String): Int

    /** The nearest upcoming assignment. */
    @Query("""
        SELECT * FROM assignments
        WHERE studentUid = :studentUid
          AND deadline >= :today
          AND submissionStatus != 'SUBMITTED'
        ORDER BY deadline ASC
        LIMIT 1
    """)
    suspend fun getNextAssignment(studentUid: String, today: String): AssignmentEntity?

    // ─────────────────────────────────────────────────────────────
    // Write
    // ─────────────────────────────────────────────────────────────

    /** Insert or replace assignments (used for API sync). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(assignments: List<AssignmentEntity>)

    /** Insert or replace a single assignment. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(assignment: AssignmentEntity)

    /** Full update of an assignment entity. */
    @Update
    suspend fun update(assignment: AssignmentEntity)

    /** Update only submission fields — avoids overwriting unrelated columns. */
    @Query("""
        UPDATE assignments
        SET submissionStatus = :status, submittedDate = :submittedDate
        WHERE id = :assignmentId
    """)
    suspend fun updateSubmissionStatus(
        assignmentId: String,
        status: String,
        submittedDate: String?
    )

    /** Delete a specific assignment. */
    @Query("DELETE FROM assignments WHERE id = :assignmentId")
    suspend fun deleteById(assignmentId: String)

    /** Delete all assignments for a student (used on logout / data reset). */
    @Query("DELETE FROM assignments WHERE studentUid = :studentUid")
    suspend fun deleteAllForStudent(studentUid: String)
}
