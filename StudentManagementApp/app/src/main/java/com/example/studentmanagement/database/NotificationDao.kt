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
    @Query("SELECT * FROM notifications WHERE studentUid = :studentUid ORDER BY createdAt DESC")
    fun getNotificationsByStudentId(studentUid: String): Flow<List<NotificationEntity>>

    /** One-shot fetch of all notifications for a student. */
    @Query("SELECT * FROM notifications WHERE studentUid = :studentUid ORDER BY createdAt DESC")
    suspend fun getNotificationsByStudentIdOnce(studentUid: String): List<NotificationEntity>

    /** Fetch a single notification by ID. */
    @Query("SELECT * FROM notifications WHERE id = :notificationId LIMIT 1")
    suspend fun getNotificationById(notificationId: String): NotificationEntity?

    /** Count unread notifications for a student. */
    @Query("SELECT COUNT(*) FROM notifications WHERE studentUid = :studentUid AND isRead = 0")
    suspend fun getUnreadCount(studentUid: String): Int

    /** Observe unread count (for badge / dashboard). */
    @Query("SELECT COUNT(*) FROM notifications WHERE studentUid = :studentUid AND isRead = 0")
    fun observeUnreadCount(studentUid: String): Flow<Int>

    /** Most recent N notifications (for dashboard preview). */
    @Query("""
        SELECT * FROM notifications
        WHERE studentUid = :studentUid
        ORDER BY createdAt DESC
        LIMIT :limit
    """)
    suspend fun getRecentNotifications(studentUid: String, limit: Int = 5): List<NotificationEntity>

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
    suspend fun markAsRead(notificationId: String)

    /** Mark all notifications for a student as read. */
    @Query("UPDATE notifications SET isRead = 1 WHERE studentUid = :studentUid")
    suspend fun markAllAsRead(studentUid: String)

    /** Delete a specific notification. */
    @Query("DELETE FROM notifications WHERE id = :notificationId")
    suspend fun deleteById(notificationId: String)

    /** Delete all notifications for a student (data reset). */
    @Query("DELETE FROM notifications WHERE studentUid = :studentUid")
    suspend fun deleteAllForStudent(studentUid: String)
}
