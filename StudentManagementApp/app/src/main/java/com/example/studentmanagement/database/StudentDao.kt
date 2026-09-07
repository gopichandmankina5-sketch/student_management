package com.example.studentmanagement.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

/**
 * Room DAO for student database operations.
 *
 * All functions are suspend functions — call them from a coroutine or
 * a ViewModel's viewModelScope, never from the main thread.
 */
@Dao
interface StudentDao {

    // ─────────────────────────────────────────────────────────────
    // Insert
    // ─────────────────────────────────────────────────────────────

    /**
     * Inserts a new student record.
     * Returns the auto-generated row ID.
     * Throws SQLiteConstraintException if the email already exists.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(student: StudentEntity): Long

    // ─────────────────────────────────────────────────────────────
    // Update
    // ─────────────────────────────────────────────────────────────

    /** Updates an existing student record matched by primary key. */
    @Update
    suspend fun update(student: StudentEntity)

    // ─────────────────────────────────────────────────────────────
    // Query — by ID
    // ─────────────────────────────────────────────────────────────

    /** Returns the student with the given ID, or null if not found. */
    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): StudentEntity?

    // ─────────────────────────────────────────────────────────────
    // Query — by Email (used for login)
    // ─────────────────────────────────────────────────────────────

    /** Returns the student with the given email, or null if not found. */
    @Query("SELECT * FROM students WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): StudentEntity?

    // ─────────────────────────────────────────────────────────────
    // Query — check existence
    // ─────────────────────────────────────────────────────────────

    /** Returns 1 if a student with the given email exists, 0 otherwise. */
    @Query("SELECT COUNT(*) FROM students WHERE email = :email")
    suspend fun countByEmail(email: String): Int

    // ─────────────────────────────────────────────────────────────
    // Delete
    // ─────────────────────────────────────────────────────────────

    /** Deletes a student record by ID. */
    @Query("DELETE FROM students WHERE id = :id")
    suspend fun deleteById(id: Long)

    /** Clears all student records (use with caution — for testing only). */
    @Query("DELETE FROM students")
    suspend fun deleteAll()
}
