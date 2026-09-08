package com.example.studentmanagement.repository

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import com.example.studentmanagement.api.RetrofitClient
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
class StudentRepository(context: Context) {

    private val dao = AppDatabase.getInstance(context).studentDao()
    private val api = RetrofitClient.api

    // ─────────────────────────────────────────────────────────────
    // Result wrapper
    // ─────────────────────────────────────────────────────────────

    sealed class RepositoryResult<out T> {
        data class Success<T>(val data: T) : RepositoryResult<T>()
        data class Error(val message: String) : RepositoryResult<Nothing>()
    }

    // ─────────────────────────────────────────────────────────────
    // Authentication — API
    // ─────────────────────────────────────────────────────────────

    /**
     * Registers a new student locally.
     *
     * To switch to the REST API, replace the body below with:
     *   val response = api.register(RegisterRequest(...))
     *   return if (response.isSuccessful && response.body()?.success == true) {
     *       val loginData = response.body()!!
     *       // optionally cache in Room for offline use
     *       RepositoryResult.Success(loginData)
     *   } else {
     *       RepositoryResult.Error(response.body()?.message ?: "Registration failed")
     *   }
     */
    suspend fun register(
        name: String,
        rollNumber: String,
        email: String,
        password: String,
        department: String,
        year: String
    ): RepositoryResult<LoginResponse> {
        return try {
            val response = api.register(
                RegisterRequest(
                    name = name.trim(),
                    rollNumber = rollNumber.trim(),
                    email = email.trim(),
                    password = password,
                    department = department.trim(),
                    year = year.trim()
                )
            )
            val body = response.body()
            if (response.isSuccessful && body != null && body.success) {
                RepositoryResult.Success(body)
            } else {
                RepositoryResult.Error(body?.message ?: "Registration failed")
            }
        } catch (e: IOException) {
            RepositoryResult.Error("Network unavailable. Please check your connection.")
        } catch (e: Exception) {
            RepositoryResult.Error("Registration failed: ${e.localizedMessage}")
        }
    }

    /**
     * Logs in a student using local Room data.
     *
     * To switch to the REST API, replace the body below with:
     *   val response = api.login(LoginRequest(email, password))
     *   return if (response.isSuccessful && response.body()?.success == true) {
     *       RepositoryResult.Success(response.body()!!)
     *   } else {
     *       RepositoryResult.Error(response.body()?.message ?: "Login failed")
     *   }
     */
    suspend fun login(email: String, password: String): RepositoryResult<LoginResponse> {
        return try {
            val response = api.login(LoginRequest(email.trim(), password))
            val body = response.body()
            if (response.isSuccessful && body != null && body.success) {
                RepositoryResult.Success(body)
            } else {
                RepositoryResult.Error(body?.message ?: "Login failed")
            }
        } catch (e: IOException) {
            RepositoryResult.Error("Network unavailable. Please check your connection.")
        } catch (e: Exception) {
            RepositoryResult.Error("Login failed: ${e.localizedMessage}")
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Student Profile
    // ─────────────────────────────────────────────────────────────

    /** Fetches a student by ID from the local Room database. */
    suspend fun getStudentById(id: Long): RepositoryResult<Student> {
        return try {
            val entity = dao.getById(id)
                ?: return RepositoryResult.Error("Student not found")
            RepositoryResult.Success(entity.toStudent())
        } catch (e: Exception) {
            RepositoryResult.Error("Failed to load profile: ${e.localizedMessage}")
        }
    }

    /** Saves updated profile fields to Room. */
    suspend fun updateStudent(student: Student): RepositoryResult<Unit> {
        return try {
            val existing = dao.getById(student.id)
                ?: return RepositoryResult.Error("Student not found")

            dao.update(
                existing.copy(
                    name = student.name.trim(),
                    rollNumber = student.rollNumber.trim(),
                    email = student.email.trim().lowercase(),
                    department = student.department.trim(),
                    year = student.year.trim()
                )
            )
            RepositoryResult.Success(Unit)
        } catch (e: Exception) {
            RepositoryResult.Error("Failed to save profile: ${e.localizedMessage}")
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Mapping helpers
    // ─────────────────────────────────────────────────────────────

    private fun StudentEntity.toStudent() = Student(
        id = id,
        name = name,
        rollNumber = rollNumber,
        email = email,
        department = department,
        year = year,
        passwordHash = passwordHash
    )
}
