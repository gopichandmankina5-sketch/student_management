package com.example.studentmanagement.repository

import android.content.Context
import android.database.sqlite.SQLiteConstraintException

import com.example.studentmanagement.database.AppDatabase
import com.example.studentmanagement.database.StudentEntity
import com.example.studentmanagement.model.LoginRequest
import com.example.studentmanagement.model.LoginResponse
import com.example.studentmanagement.model.RegisterRequest
import com.example.studentmanagement.model.Student
import java.io.IOException
import java.security.MessageDigest

/**
 * StudentRepository — single source of truth for student data.
 *
 * ─────────────────────────────────────────────────────────────────
 * ARCHITECTURE NOTE (for Member 4 backend integration):
 * ─────────────────────────────────────────────────────────────────
 * The repository currently uses LOCAL (Room) implementations for
 * login and registration, because Member 4's backend is not yet
 * available.
 *
 * When the backend is ready:
 *   1. Replace the login() and register() bodies with Retrofit calls
 *      (commented stubs are provided below).
 *   2. The UI activities do NOT need to change — they only call
 *      repository functions and observe [RepositoryResult].
 * ─────────────────────────────────────────────────────────────────
 */
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class StudentRepository(context: Context) {

    private val dao = AppDatabase.getInstance(context).studentDao()
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val sessionManager = com.example.studentmanagement.auth.SessionManager(context)

    sealed class RepositoryResult<out T> {
        data class Success<T>(val data: T) : RepositoryResult<T>()
        data class Error(val message: String) : RepositoryResult<Nothing>()
    }

    suspend fun register(
        name: String, rollNumber: String, email: String, password: String, department: String, year: String
    ): RepositoryResult<LoginResponse> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            val uid = authResult.user?.uid ?: throw Exception("Auth failed")

            val studentData = hashMapOf(
                "name" to name.trim(),
                "rollNumber" to rollNumber.trim(),
                "email" to email.trim(),
                "department" to department.trim(),
                "year" to year.trim(),
                "role" to "STUDENT",
                "uid" to uid
            )

            firestore.collection("students").document(uid).set(studentData).await()

            RepositoryResult.Success(LoginResponse(success = true, message = "Success", studentId = uid.hashCode().toLong(), token = uid))
        } catch (e: Exception) {
            RepositoryResult.Error("Registration failed: ${e.localizedMessage}")
        }
    }

    suspend fun login(email: String, password: String): RepositoryResult<LoginResponse> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email.trim(), password).await()
            val uid = authResult.user?.uid ?: throw Exception("Auth failed")
            
            // Just verifying the document exists
            val doc = firestore.collection("students").document(uid).get().await()
            if (!doc.exists()) throw Exception("Student profile not found")

            RepositoryResult.Success(LoginResponse(success = true, message = "Success", studentId = uid.hashCode().toLong(), token = uid))
        } catch (e: Exception) {
            RepositoryResult.Error("Login failed: ${e.localizedMessage}")
        }
    }

    suspend fun getStudentById(id: Long): RepositoryResult<Student> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("Not authenticated")
            val doc = firestore.collection("students").document(uid).get().await()
            
            if (doc.exists()) {
                val student = Student(
                    id = id,
                    name = doc.getString("name") ?: "",
                    rollNumber = doc.getString("rollNumber") ?: "",
                    email = doc.getString("email") ?: "",
                    department = doc.getString("department") ?: "",
                    year = doc.getString("year") ?: "",
                    passwordHash = ""
                )
                
                dao.update(dao.getById(id)?.copy(
                    name = student.name, department = student.department, year = student.year
                ) ?: StudentEntity(
                    id = id, name = student.name,
                    rollNumber = student.rollNumber, email = student.email,
                    department = student.department, year = student.year, passwordHash = ""
                ))
                RepositoryResult.Success(student)
            } else {
                RepositoryResult.Error("Student not found on server")
            }
        } catch (e: Exception) {
            val entity = dao.getById(id)
            if (entity != null) RepositoryResult.Success(entity.toStudent())
            else RepositoryResult.Error("Failed to load profile: ${e.localizedMessage}")
        }
    }

    suspend fun updateStudent(student: Student): RepositoryResult<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("Not authenticated")
            
            val updates = hashMapOf<String, Any>(
                "name" to student.name.trim(),
                "department" to student.department.trim(),
                "year" to student.year.trim()
            )
            
            firestore.collection("students").document(uid).update(updates).await()

            val existing = dao.getById(student.id)
            if (existing != null) {
                dao.update(existing.copy(
                    name = student.name.trim(),
                    department = student.department.trim(),
                    year = student.year.trim()
                ))
            }
            RepositoryResult.Success(Unit)
        } catch (e: Exception) {
            RepositoryResult.Error("Failed to save profile: ${e.localizedMessage}")
        }
    }

    private fun StudentEntity.toStudent() = Student(
        id = id, name = name, rollNumber = rollNumber, email = email, department = department, year = year, passwordHash = passwordHash
    )
}
