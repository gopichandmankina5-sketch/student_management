package com.example.studentmanagement.utils

/**
 * App-wide constants.
 */
object Constants {

    // API Configuration removed - Firebase handles networking internally

    // ─────────────────────────────────────────────────────────────
    // SharedPreferences Keys
    // ─────────────────────────────────────────────────────────────
    const val PREF_FILE = "student_session"
    const val KEY_LOGGED_IN = "logged_in"
    const val KEY_STUDENT_ID = "student_id"
    const val KEY_STUDENT_NAME = "name"
    const val KEY_STUDENT_EMAIL = "email"
    const val KEY_AUTH_TOKEN = "auth_token"
    const val KEY_UID = "firebase_uid"   // Firebase Authentication UID for Firestore lookups


    // ─────────────────────────────────────────────────────────────
    // Intent / Bundle Extras
    // ─────────────────────────────────────────────────────────────
    const val EXTRA_STUDENT_ID       = "extra_student_id"
    const val EXTRA_FROM_REGISTER    = "extra_from_register"

    // Member 3 — Assignment & Notification extras
    const val EXTRA_ASSIGNMENT_ID    = "extra_assignment_id"
    const val EXTRA_NOTIFICATION_ID  = "extra_notification_id"

    // ─────────────────────────────────────────────────────────────
    // Database
    // ─────────────────────────────────────────────────────────────
    const val DB_NAME = "student_management.db"
    // Increment this whenever the Room schema changes and add a Migration.
    // v1 → Member 1 (students table)
    // v2 → Member 3 original (assignments + notifications tables with Int IDs)
    // v3 → Member 3 updated (assignments + notifications migrated to String UIDs)
    const val DB_VERSION = 3

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
