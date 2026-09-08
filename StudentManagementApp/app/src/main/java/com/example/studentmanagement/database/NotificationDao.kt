package com.example.studentmanagement.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO for notification CRUD operations.
 */
@Dao
interface NotificationDao {

    // ─────────────────────────────────────────────────────────────
    // Read
    // ─────────────────────────────────────────────────────────────

    /** Observe all notifications for a student, newest first. */
    @Query("SELECT * FROM notifications WHERE studentId = :studentId ORDER BY createdAt DESC")
    fun getNotificationsByStudentId(studentId: Int): Flow<List<NotificationEntity>>

    /** One-shot fetch of all notifications for a student. */
    @Query("SELECT * FROM notifications WHERE studentId = :studentId ORDER BY createdAt DESC")
    suspend fun getNotificationsByStudentIdOnce(studentId: Int): List<NotificationEntity>

    /** Fetch a single notification by ID. */
    @Query("SELECT * FROM notifications WHERE id = :notificationId LIMIT 1")
    suspend fun getNotificationById(notificationId: Int): NotificationEntity?

    /** Count unread notifications for a student. */
    @Query("SELECT COUNT(*) FROM notifications WHERE studentId = :studentId AND isRead = 0")
    suspend fun getUnreadCount(studentId: Int): Int

    /** Observe unread count (for badge / dashboard). */
    @Query("SELECT COUNT(*) FROM notifications WHERE studentId = :studentId AND isRead = 0")
    fun observeUnreadCount(studentId: Int): Flow<Int>

    /** Most recent N notifications (for dashboard preview). */
    @Query("""
        SELECT * FROM notifications
        WHERE studentId = :studentId
        ORDER BY createdAt DESC
        LIMIT :limit
    """)
    suspend fun getRecentNotifications(studentId: Int, limit: Int = 5): List<NotificationEntity>

    // ─────────────────────────────────────────────────────────────
    // Write
    // ─────────────────────────────────────────────────────────────

    /** Insert or replace list of notifications (API sync). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notifications: List<NotificationEntity>)

    /** Insert or replace a single notification. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationEntity)

    /** Mark a single notification as read. */
    @Query("UPDATE notifications SET isRead = 1 WHERE id = :notificationId")
    suspend fun markAsRead(notificationId: Int)

    /** Mark all notifications for a student as read. */
    @Query("UPDATE notifications SET isRead = 1 WHERE studentId = :studentId")
    suspend fun markAllAsRead(studentId: Int)

    /** Delete a specific notification. */
    @Query("DELETE FROM notifications WHERE id = :notificationId")
    suspend fun deleteById(notificationId: Int)

    /** Delete all notifications for a student (data reset). */
    @Query("DELETE FROM notifications WHERE studentId = :studentId")
    suspend fun deleteAllForStudent(studentId: Int)
}
