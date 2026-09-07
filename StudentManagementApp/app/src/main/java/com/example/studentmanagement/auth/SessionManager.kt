package com.example.studentmanagement.auth

import android.content.Context
import com.example.studentmanagement.utils.Constants

/**
 * Manages user session state using SharedPreferences.
 *
 * Responsibilities:
 * - Persist login state across app restarts
 * - Store student ID and display info for quick access
 * - Store the auth token for API calls (when Member 4's backend is live)
 * - Provide a clean logout (clear all session data)
 *
 * SECURITY NOTE:
 * - The auth token is stored in SharedPreferences (MODE_PRIVATE).
 * - For higher security, consider using EncryptedSharedPreferences
 *   (androidx.security.crypto) in a production app.
 */
class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences(
        Constants.PREF_FILE,
        Context.MODE_PRIVATE
    )

    // ─────────────────────────────────────────────────────────────
    // Save session
    // ─────────────────────────────────────────────────────────────

    /**
     * Saves session data after a successful login or registration.
     *
     * @param studentId The local or backend-assigned student ID.
     * @param name      The student's display name.
     * @param email     The student's email address.
     * @param token     The JWT/auth token from the backend (null for local mock).
     */
    fun saveSession(
        studentId: Long,
        name: String,
        email: String,
        token: String? = null
    ) {
        prefs.edit()
            .putBoolean(Constants.KEY_LOGGED_IN, true)
            .putLong(Constants.KEY_STUDENT_ID, studentId)
            .putString(Constants.KEY_STUDENT_NAME, name)
            .putString(Constants.KEY_STUDENT_EMAIL, email)
            .apply {
                if (token != null) putString(Constants.KEY_AUTH_TOKEN, token)
            }
            .apply()
    }

    // ─────────────────────────────────────────────────────────────
    // Read session
    // ─────────────────────────────────────────────────────────────

    /** Returns true if a user is currently logged in. */
    fun isLoggedIn(): Boolean = prefs.getBoolean(Constants.KEY_LOGGED_IN, false)

    /** Returns the currently logged-in student's ID, or 0 if not logged in. */
    fun getStudentId(): Long = prefs.getLong(Constants.KEY_STUDENT_ID, 0L)

    /** Returns the student's display name, or "Student" as fallback. */
    fun getStudentName(): String =
        prefs.getString(Constants.KEY_STUDENT_NAME, "Student") ?: "Student"

    /** Returns the student's email, or empty string if not set. */
    fun getStudentEmail(): String =
        prefs.getString(Constants.KEY_STUDENT_EMAIL, "") ?: ""

    /**
     * Returns the auth token for API calls.
     * Format for use: "Bearer $token"
     * Will be null until Member 4's backend is integrated.
     */
    fun getAuthToken(): String? =
        prefs.getString(Constants.KEY_AUTH_TOKEN, null)

    /** Returns "Bearer <token>" or an empty string if no token exists. */
    fun getBearerToken(): String {
        val token = getAuthToken() ?: return ""
        return "Bearer $token"
    }

    // ─────────────────────────────────────────────────────────────
    // Update name (called after profile edit)
    // ─────────────────────────────────────────────────────────────

    fun updateName(newName: String) {
        prefs.edit().putString(Constants.KEY_STUDENT_NAME, newName).apply()
    }

    // ─────────────────────────────────────────────────────────────
    // Logout
    // ─────────────────────────────────────────────────────────────

    /**
     * Clears all session data.
     * After calling this, [isLoggedIn] returns false and the user
     * will be redirected to the login screen.
     */
    fun logout() {
        prefs.edit().clear().apply()
    }
}
