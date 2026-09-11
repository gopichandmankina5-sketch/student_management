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

/**
 * StudentRepository — single source of truth for student data.
 *
 * ─────────────────────────────────────────────────────────────────
 * ARCHITECTURE:
 * ─────────────────────────────────────────────────────────────────
 * Firebase Authentication is used for auth.
 * Cloud Firestore is the primary data store.
 * Room is used as a local cache for profile data only.
 *
 * CRITICAL: All Firestore lookups use the Firebase UID string.
 * The integer studentId in Room is only a local cache key.
 * ─────────────────────────────────────────────────────────────────
 */
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.studentmanagement.auth.SessionManager

class StudentRepository(context: Context) {

    private val dao = AppDatabase.getInstance(context).studentDao()
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val sessionManager = SessionManager(context)

    sealed class RepositoryResult<out T> {
        data class Success<T>(val data: T) : RepositoryResult<T>()
        data class Error(val message: String) : RepositoryResult<Nothing>()
    }

    // ─────────────────────────────────────────────────────────────
    // Registration
    // ─────────────────────────────────────────────────────────────

    suspend fun register(
        name: String, rollNumber: String, email: String, password: String,
        department: String, year: String
    ): RepositoryResult<LoginResponse> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            val uid = authResult.user?.uid ?: throw Exception("Auth failed — UID was null")

            val role = "STUDENT"

            // 1. Create global user record (role enforcement: always STUDENT on registration)
            val userData = hashMapOf(
                "uid" to uid,
                "name" to name.trim(),
                "email" to email.trim(),
                "role" to role
            )
            firestore.collection("users").document(uid).set(userData).await()

            // 2. Create student-specific profile
            val studentData = hashMapOf(
                "uid" to uid,
                "name" to name.trim(),
                "rollNumber" to rollNumber.trim(),
                "email" to email.trim(),
                "department" to department.trim(),
                "year" to year.trim(),
                "role" to role
            )
            firestore.collection("students").document(uid).set(studentData).await()

            // 3. Cache in Room so offline profile loads work
            try {
                dao.insert(
                    StudentEntity(
                        name = name.trim(),
                        rollNumber = rollNumber.trim(),
                        email = email.trim(),
                        department = department.trim(),
                        year = year.trim(),
                        passwordHash = ""
                    )
                )
            } catch (e: Exception) { /* ignore local cache failure */ }

            // 4. Persist UID in session immediately
            sessionManager.saveUid(uid)

            RepositoryResult.Success(
                LoginResponse(
                    success = true,
                    message = "Registration successful",
                    studentId = 0L,
                    token = uid,  // uid used as token
                    role = role
                )
            )
        } catch (e: Exception) {
            RepositoryResult.Error("Registration failed: ${e.localizedMessage}")
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Login
    // ─────────────────────────────────────────────────────────────

    suspend fun login(email: String, password: String): RepositoryResult<LoginResponse> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email.trim(), password).await()
            val uid = authResult.user?.uid ?: throw Exception("Auth failed — UID was null")

            // Fetch role from global user record
            val userDoc = firestore.collection("users").document(uid).get().await()
            val role = if (userDoc.exists()) {
                userDoc.getString("role") ?: "STUDENT"
            } else {
                // Fallback: check students collection
                val studentDoc = firestore.collection("students").document(uid).get().await()
                if (studentDoc.exists()) "STUDENT"
                else throw Exception("User profile not found in Firestore. Please contact admin.")
            }

            // Fetch display name
            val name = if (userDoc.exists()) {
                userDoc.getString("name") ?: ""
            } else {
                authResult.user?.displayName ?: ""
            }

            // Persist UID in session
            sessionManager.saveUid(uid)

            RepositoryResult.Success(
                LoginResponse(
                    success = true,
                    message = "Login successful",
                    studentId = 0L,
                    token = uid,  // uid used as token
                    role = role
                )
            )
        } catch (e: Exception) {
            RepositoryResult.Error("Login failed: ${e.localizedMessage}")
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Profile — uses Firebase UID for Firestore, not integer ID
    // ─────────────────────────────────────────────────────────────

    suspend fun getStudentById(id: Long): RepositoryResult<Student> {
        return try {
            // Always use the current Firebase UID, not the local integer ID
            val uid = auth.currentUser?.uid
                ?: sessionManager.getUid().takeIf { it.isNotEmpty() }
                ?: throw Exception("Not authenticated")

            val collection = when (sessionManager.getRole()) {
                "FACULTY" -> "faculty"
                "ADMIN" -> "users"
                else -> "students"
            }

            val doc = firestore.collection(collection).document(uid).get().await()

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

                // Update Room cache
                try {
                    val existing = dao.getById(id)
                    if (existing != null) {
                        dao.update(existing.copy(
                            name = student.name,
                            department = student.department,
                            year = student.year
                        ))
                    } else {
                        dao.insert(StudentEntity(
                            id = id, name = student.name,
                            rollNumber = student.rollNumber, email = student.email,
                            department = student.department, year = student.year,
                            passwordHash = ""
                        ))
                    }
                } catch (e: Exception) { /* ignore cache failure */ }

                RepositoryResult.Success(student)
            } else {
                RepositoryResult.Error("Profile not found. Please contact admin.")
            }
        } catch (e: Exception) {
            // Fallback to Room cache
            val entity = dao.getById(id)
            if (entity != null) RepositoryResult.Success(entity.toStudent())
            else RepositoryResult.Error("Failed to load profile: ${e.localizedMessage}")
        }
    }

    suspend fun updateStudent(student: Student): RepositoryResult<Unit> {
        return try {
            val uid = auth.currentUser?.uid
                ?: sessionManager.getUid().takeIf { it.isNotEmpty() }
                ?: throw Exception("Not authenticated")

            val updates = hashMapOf<String, Any>(
                "name" to student.name.trim(),
                "department" to student.department.trim(),
                "year" to student.year.trim()
            )

            // Update in the appropriate collection
            val collection = when (sessionManager.getRole()) {
                "FACULTY" -> "faculty"
                "ADMIN" -> "users"
                else -> "students"
            }
            firestore.collection(collection).document(uid).update(updates).await()

            // Also update users collection name if student
            if (collection == "students") {
                try {
                    firestore.collection("users").document(uid)
                        .update("name", student.name.trim()).await()
                } catch (e: Exception) { /* best effort */ }
            }

            // Update Room cache
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

    // ─────────────────────────────────────────────────────────────
    // Admin — list all students
    // ─────────────────────────────────────────────────────────────

    suspend fun getAllStudents(): RepositoryResult<List<Student>> {
        return try {
            val snapshot = firestore.collection("students").get().await()
            val students = snapshot.documents.mapNotNull { doc ->
                Student(
                    id = 0L,
                    name = doc.getString("name") ?: "",
                    rollNumber = doc.getString("rollNumber") ?: "",
                    email = doc.getString("email") ?: "",
                    department = doc.getString("department") ?: "",
                    year = doc.getString("year") ?: "",
                    passwordHash = ""
                )
            }
            RepositoryResult.Success(students)
        } catch (e: Exception) {
            RepositoryResult.Error("Failed to fetch students: ${e.localizedMessage}")
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Extension
    // ─────────────────────────────────────────────────────────────

    private fun StudentEntity.toStudent() = Student(
        id = id, name = name, rollNumber = rollNumber, email = email,
        department = department, year = year, passwordHash = passwordHash
    )
}
