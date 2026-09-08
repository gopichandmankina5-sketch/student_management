package com.example.studentmanagement.repository

import android.content.Context

import com.example.studentmanagement.auth.SessionManager
import com.example.studentmanagement.database.AppDatabase
import com.example.studentmanagement.database.NotificationEntity
import com.example.studentmanagement.model.MarkReadRequest
import com.example.studentmanagement.model.Notification
import com.example.studentmanagement.model.NotificationType
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Repository for notifications.
 */
class NotificationRepository(private val context: Context) {

    private val dao            = AppDatabase.getInstance(context).notificationDao()
    private val sessionManager = SessionManager(context)
    private val firestore      = FirebaseFirestore.getInstance()

    sealed class Result<out T> {
        data class Success<T>(val data: T) : Result<T>()
        data class Error(val message: String) : Result<Nothing>()
    }

    fun observeNotifications(studentId: Int): Flow<List<NotificationEntity>> =
        dao.getNotificationsByStudentId(studentId)

    fun observeUnreadCount(studentId: Int): Flow<Int> =
        dao.observeUnreadCount(studentId)

    suspend fun loadNotifications(studentId: Int): Result<List<NotificationEntity>> =
        withContext(Dispatchers.IO) {
            try {
                val snapshot = firestore.collection("notifications")
                    .whereEqualTo("studentId", studentId)
                    .get()
                    .await()
                
                val apiData = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    NotificationEntity(
                        id = doc.id.hashCode(),
                        studentId = studentId,
                        title = data["title"] as? String ?: "",
                        message = data["message"] as? String ?: "",
                        type = data["type"] as? String ?: "INFO",
                        createdAt = data["createdAt"] as? String ?: "",
                        isRead = data["isRead"] as? Boolean ?: false,
                        relatedAssignmentId = (data["relatedAssignmentId"] as? Long)?.toInt()
                    )
                }
                
                if (apiData.isNotEmpty()) {
                    dao.insertAll(apiData)
                }
            } catch (e: Exception) {
                // API unavailable — Room data already available
            }

            Result.Success(dao.getNotificationsByStudentIdOnce(studentId))
        }

    suspend fun markAsRead(notificationId: Int): Result<Unit> =
        withContext(Dispatchers.IO) {
            dao.markAsRead(notificationId)
            try {
                val snapshot = firestore.collection("notifications").get().await()
                val targetDoc = snapshot.documents.find { it.id.hashCode() == notificationId }
                
                if (targetDoc != null) {
                    firestore.collection("notifications").document(targetDoc.id)
                        .update("isRead", true).await()
                }
            } catch (e: Exception) { /* API unavailable */ }
            Result.Success(Unit)
        }

    suspend fun markAllAsRead(studentId: Int): Result<Unit> =
        withContext(Dispatchers.IO) {
            dao.markAllAsRead(studentId)
            try {
                val snapshot = firestore.collection("notifications")
                    .whereEqualTo("studentId", studentId)
                    .whereEqualTo("isRead", false)
                    .get()
                    .await()
                
                val batch = firestore.batch()
                for (doc in snapshot.documents) {
                    batch.update(doc.reference, "isRead", true)
                }
                batch.commit().await()
            } catch (e: Exception) { /* API unavailable */ }
            Result.Success(Unit)
        }

    suspend fun getUnreadCount(studentId: Int): Int =
        withContext(Dispatchers.IO) { dao.getUnreadCount(studentId) }

    suspend fun getRecentNotifications(
        studentId: Int, limit: Int = 5
    ): List<NotificationEntity> =
        withContext(Dispatchers.IO) { dao.getRecentNotifications(studentId, limit) }
}
