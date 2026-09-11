package com.example.studentmanagement.auth

import android.content.Context
import com.example.studentmanagement.utils.Constants

/**
 * Manages user session state using SharedPreferences.
 *
 * Responsibilities:
 * - Persist login state across app restarts
 * - Store student ID and display info for quick access
 * - Store the Firebase UID for Firestore document lookups
 * - Provide a clean logout (clear all session data)
 *
 * SECURITY NOTE:
 * - Session data is stored in SharedPreferences (MODE_PRIVATE).
 * - For higher security, consider using EncryptedSharedPreferences
 *   (androidx.security.crypto) in a production app.
 */
class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences(
        Constants.PREF_FILE,
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_ROLE = "KEY_ROLE"
    }

    // ─────────────────────────────────────────────────────────────
    // Save session
    // ─────────────────────────────────────────────────────────────

    /**
     * Saves session data after a successful login or registration.
     *
     * @param studentId The local or backend-assigned student ID.
     * @param name      The student's display name.
     * @param email     The student's email address.
     * @param token     The Firebase UID used as the auth token.
     * @param role      The user's role: STUDENT, FACULTY, or ADMIN.
     */
    fun saveSession(
        studentId: Long,
        name: String,
        email: String,
        token: String? = null,
        role: String = "STUDENT"
    ) {
        prefs.edit()
            .putBoolean(Constants.KEY_LOGGED_IN, true)
            .putLong(Constants.KEY_STUDENT_ID, studentId)
            .putString(Constants.KEY_STUDENT_NAME, name)
            .putString(Constants.KEY_STUDENT_EMAIL, email)
            .putString(KEY_ROLE, role)
            .apply {
                // token here is the Firebase UID — store as both auth token and UID
                if (token != null) {
                    putString(Constants.KEY_AUTH_TOKEN, token)
                    putString(Constants.KEY_UID, token)
                }
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

    /** Returns the user's role (STUDENT, FACULTY, ADMIN), defaults to STUDENT. */
    fun getRole(): String =
        prefs.getString(KEY_ROLE, "STUDENT") ?: "STUDENT"

    /** Returns the auth token (Firebase UID) stored during login/register. */
    fun getAuthToken(): String? =
        prefs.getString(Constants.KEY_AUTH_TOKEN, null)

    /** Returns "Bearer <token>" or an empty string if no token exists. */
    fun getBearerToken(): String {
        val token = getAuthToken() ?: return ""
        return "Bearer $token"
    }

    // ─────────────────────────────────────────────────────────────
    // Firebase UID — primary key for ALL Firestore operations
    // ─────────────────────────────────────────────────────────────

    /**
     * Explicitly saves the Firebase Authentication UID.
     * This is the canonical identity used for all Firestore document lookups.
     * Called separately after login/register to ensure the UID is always current.
     */
    fun saveUid(uid: String) {
        prefs.edit().putString(Constants.KEY_UID, uid).apply()
    }

    /**
     * Returns the Firebase Authentication UID.
     * This is the canonical identity for ALL Firestore queries — never use
     * the integer studentId for Firestore document lookups.
     * Returns empty string if not logged in.
     */
    fun getUid(): String = prefs.getString(Constants.KEY_UID, "") ?: ""

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
