package com.example.studentmanagement.repository

import android.content.Context

import com.example.studentmanagement.auth.SessionManager
import com.example.studentmanagement.database.AppDatabase
import com.example.studentmanagement.database.NotificationEntity
import com.example.studentmanagement.model.MarkReadRequest
import com.example.studentmanagement.model.Notification
import com.example.studentmanagement.model.NotificationType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Repository for notifications.
 *
 * Firestore is the source of truth.
 * Room is used as a local offline cache.
 *
 * CRITICAL: Firestore documents use "studentUid" (string Firebase UID).
 * Room uses integer studentId as a local cache key only.
 */
class NotificationRepository(private val context: Context) {

    private val dao            = AppDatabase.getInstance(context).notificationDao()
    private val sessionManager = SessionManager(context)
    private val firestore      = FirebaseFirestore.getInstance()
    private val auth           = FirebaseAuth.getInstance()

    sealed class Result<out T> {
        data class Success<T>(val data: T) : Result<T>()
        data class Error(val message: String) : Result<Nothing>()
    }

    fun observeNotifications(studentUid: String): Flow<List<NotificationEntity>> =
        dao.getNotificationsByStudentId(studentUid)

    fun observeUnreadCount(studentUid: String): Flow<Int> =
        dao.observeUnreadCount(studentUid)

    suspend fun loadNotifications(studentUid: String): Result<List<NotificationEntity>> =
        withContext(Dispatchers.IO) {
            val uid = auth.currentUser?.uid
                ?: sessionManager.getUid().takeIf { it.isNotEmpty() }

            if (uid != null) {
                try {
                    val snapshot = firestore.collection("notifications")
                        .whereEqualTo("studentUid", uid)
                        .get()
                        .await()

                    val apiData = snapshot.documents.mapNotNull { doc ->
                        val data = doc.data ?: return@mapNotNull null
                        NotificationEntity(
                            id = doc.id,
                            studentUid = uid,
                            title = data["title"] as? String ?: "",
                            message = data["message"] as? String ?: "",
                            type = data["type"] as? String ?: "GENERAL",
                            createdAt = data["createdAt"] as? String ?: "",
                            isRead = data["isRead"] as? Boolean ?: false,
                            relatedAssignmentId = data["relatedAssignmentId"] as? String
                        )
                    }

                    if (apiData.isNotEmpty()) {
                        dao.insertAll(apiData)
                    }
                } catch (e: Exception) {
                    // Firestore unavailable
                }
            }

            Result.Success(dao.getNotificationsByStudentIdOnce(studentUid))
        }

    suspend fun markAsRead(notificationId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            dao.markAsRead(notificationId)
            val uid = auth.currentUser?.uid ?: sessionManager.getUid().takeIf { it.isNotEmpty() }
            if (uid != null) {
                try {
                    firestore.collection("notifications").document(notificationId)
                        .update("isRead", true).await()
                } catch (e: Exception) { /* Firestore unavailable */ }
            }
            Result.Success(Unit)
        }

    suspend fun markAllAsRead(studentUid: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            dao.markAllAsRead(studentUid)
            val uid = auth.currentUser?.uid ?: sessionManager.getUid().takeIf { it.isNotEmpty() }
            if (uid != null) {
                try {
                    val snapshot = firestore.collection("notifications")
                        .whereEqualTo("studentUid", uid)
                        .whereEqualTo("isRead", false)
                        .get()
                        .await()

                    val batch = firestore.batch()
                    for (doc in snapshot.documents) {
                        batch.update(doc.reference, "isRead", true)
                    }
                    if (snapshot.documents.isNotEmpty()) {
                        batch.commit().await()
                    }
                } catch (e: Exception) { /* Firestore unavailable */ }
            }
            Result.Success(Unit)
        }

    suspend fun getUnreadCount(studentUid: String): Int =
        withContext(Dispatchers.IO) { dao.getUnreadCount(studentUid) }

    suspend fun getRecentNotifications(
        studentUid: String, limit: Int = 5
    ): List<NotificationEntity> =
        withContext(Dispatchers.IO) { dao.getRecentNotifications(studentUid, limit) }
}
