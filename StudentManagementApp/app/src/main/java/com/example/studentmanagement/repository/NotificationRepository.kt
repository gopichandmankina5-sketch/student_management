package com.example.studentmanagement.repository

import android.content.Context
import com.example.studentmanagement.api.RetrofitClient
import com.example.studentmanagement.auth.SessionManager
import com.example.studentmanagement.database.AppDatabase
import com.example.studentmanagement.database.NotificationEntity
import com.example.studentmanagement.model.MarkReadRequest
import com.example.studentmanagement.model.Notification
import com.example.studentmanagement.model.NotificationType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Repository for notifications.
 *
 * UI → Repository → Room (primary) + Retrofit (optional sync)
 *
 * Integration hooks for Member 1 Dashboard:
 *   - getUnreadCount()
 *   - getRecentNotifications()
 */
class NotificationRepository(private val context: Context) {

    private val dao            = AppDatabase.getInstance(context).notificationDao()
    private val sessionManager = SessionManager(context)

    // ─────────────────────────────────────────────────────────────
    // Sealed result wrapper
    // ─────────────────────────────────────────────────────────────

    sealed class Result<out T> {
        data class Success<T>(val data: T) : Result<T>()
        data class Error(val message: String) : Result<Nothing>()
    }

    // ─────────────────────────────────────────────────────────────
    // Observe (Flow)
    // ─────────────────────────────────────────────────────────────

    fun observeNotifications(studentId: Int): Flow<List<NotificationEntity>> =
        dao.getNotificationsByStudentId(studentId)

    fun observeUnreadCount(studentId: Int): Flow<Int> =
        dao.observeUnreadCount(studentId)

    // ─────────────────────────────────────────────────────────────
    // Load
    // ─────────────────────────────────────────────────────────────

    suspend fun loadNotifications(studentId: Int): Result<List<NotificationEntity>> =
        withContext(Dispatchers.IO) {
            // Attempt API sync
            try {
                val token = sessionManager.getBearerToken()
                val response = RetrofitClient.api.getNotifications(studentId, token)
                if (response.isSuccessful) {
                    val apiData = response.body()
                    if (!apiData.isNullOrEmpty()) {
                        dao.insertAll(apiData.map { it.toEntity() })
                    }
                }
            } catch (e: Exception) {
                // API unavailable — Room data already available
            }

            Result.Success(dao.getNotificationsByStudentIdOnce(studentId))
        }

    // ─────────────────────────────────────────────────────────────
    // Mark read
    // ─────────────────────────────────────────────────────────────

    suspend fun markAsRead(notificationId: Int): Result<Unit> =
        withContext(Dispatchers.IO) {
            dao.markAsRead(notificationId)
            try {
                val token = sessionManager.getBearerToken()
                RetrofitClient.api.markNotificationRead(notificationId, token, MarkReadRequest())
            } catch (e: Exception) { /* API unavailable */ }
            Result.Success(Unit)
        }

    suspend fun markAllAsRead(studentId: Int): Result<Unit> =
        withContext(Dispatchers.IO) {
            dao.markAllAsRead(studentId)
            try {
                val token = sessionManager.getBearerToken()
                RetrofitClient.api.markAllNotificationsRead(studentId, token)
            } catch (e: Exception) { /* API unavailable */ }
            Result.Success(Unit)
        }

    // ─────────────────────────────────────────────────────────────
    // Dashboard integration helpers (Member 1)
    // ─────────────────────────────────────────────────────────────

    /** Unread notification count for the dashboard badge. */
    suspend fun getUnreadCount(studentId: Int): Int =
        withContext(Dispatchers.IO) { dao.getUnreadCount(studentId) }

    /** Most recent [limit] notifications for the dashboard preview. */
    suspend fun getRecentNotifications(
        studentId: Int, limit: Int = 5
    ): List<NotificationEntity> =
        withContext(Dispatchers.IO) { dao.getRecentNotifications(studentId, limit) }



    // ─────────────────────────────────────────────────────────────
    // Mapper
    // ─────────────────────────────────────────────────────────────

    private fun Notification.toEntity() = NotificationEntity(
        id = id, studentId = studentId,
        title = title, message = message, type = type,
        createdAt = createdAt, isRead = isRead,
        relatedAssignmentId = relatedAssignmentId
    )
}
