package com.example.studentmanagement.utils

/**
 * App-wide constants.
 *
 * TEAM INTEGRATION NOTE:
 * - BASE_URL is set to the Android emulator localhost alias (10.0.2.2:3000).
 * - When Member 4 deploys the backend, update BASE_URL to the real server URL here only.
 * - Do NOT hardcode URLs elsewhere in the codebase.
 */
object Constants {

    // ─────────────────────────────────────────────────────────────
    // API Configuration
    // ─────────────────────────────────────────────────────────────

    /**
     * Development backend URL (Android emulator → localhost:3000).
     * Member 4: Replace this URL with the deployed server address when ready.
     *
     * For a physical device on the same WiFi, use your machine's LAN IP:
     *   e.g. "http://192.168.1.100:3000/"
     */
    const val BASE_URL = "http://10.0.2.2:3000/"

    // API timeout (seconds)
    const val CONNECT_TIMEOUT = 15L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L

    // ─────────────────────────────────────────────────────────────
    // SharedPreferences Keys
    // ─────────────────────────────────────────────────────────────
    const val PREF_FILE = "student_session"
    const val KEY_LOGGED_IN = "logged_in"
    const val KEY_STUDENT_ID = "student_id"
    const val KEY_STUDENT_NAME = "name"
    const val KEY_STUDENT_EMAIL = "email"
    const val KEY_AUTH_TOKEN = "auth_token"

    // ─────────────────────────────────────────────────────────────
    // Intent / Bundle Extras
    // ─────────────────────────────────────────────────────────────
    const val EXTRA_STUDENT_ID = "extra_student_id"
    const val EXTRA_FROM_REGISTER = "extra_from_register"

    // ─────────────────────────────────────────────────────────────
    // Database
    // ─────────────────────────────────────────────────────────────
    const val DB_NAME = "student_management.db"
    const val DB_VERSION = 1

    // ─────────────────────────────────────────────────────────────
    // Validation Rules
    // ─────────────────────────────────────────────────────────────
    const val MIN_PASSWORD_LENGTH = 6
    const val MAX_PASSWORD_LENGTH = 64

    // ─────────────────────────────────────────────────────────────
    // Splash delay
    // ─────────────────────────────────────────────────────────────
    const val SPLASH_DELAY_MS = 1500L
}
