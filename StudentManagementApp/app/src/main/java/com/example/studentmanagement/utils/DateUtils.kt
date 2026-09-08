package com.example.studentmanagement.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Date/time utilities for Member 3 — Assignment & Notification module.
 *
 * All assignment deadlines are stored as "yyyy-MM-dd" ISO strings.
 * Never compare dates as raw strings unless the format is guaranteed ISO-ordered.
 */
object DateUtils {

    private val ISO_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val DISPLAY_FORMAT  = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    private val TIME_FORMAT     = SimpleDateFormat("hh:mm a", Locale.getDefault())
    private val ISO_DATETIME_FORMAT = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

    init {
        ISO_DATE_FORMAT.isLenient    = false
        DISPLAY_FORMAT.isLenient     = false
        ISO_DATETIME_FORMAT.isLenient = false
    }

    // ─────────────────────────────────────────────────────────────
    // Parse
    // ─────────────────────────────────────────────────────────────

    /** Parse a yyyy-MM-dd string to a [Date], or null on failure. */
    fun parseDate(isoDate: String): Date? = try {
        ISO_DATE_FORMAT.parse(isoDate)
    } catch (e: Exception) {
        null
    }

    /** Parse ISO-8601 datetime or fall back to date-only format. */
    fun parseDateTime(isoString: String): Date? = try {
        ISO_DATETIME_FORMAT.parse(isoString)
    } catch (e: Exception) {
        try { ISO_DATE_FORMAT.parse(isoString) } catch (e2: Exception) { null }
    }

    /** Today's date with time zeroed to midnight. */
    fun today(): Date {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.time
    }

    /** Today as yyyy-MM-dd string — use for Room queries. */
    fun todayIso(): String = ISO_DATE_FORMAT.format(today())

    // ─────────────────────────────────────────────────────────────
    // Deadline classification
    // ─────────────────────────────────────────────────────────────

    /** Returns true if the deadline has already passed (before today). */
    fun isOverdue(deadlineIso: String): Boolean {
        val deadline = parseDate(deadlineIso) ?: return false
        return deadline.before(today())
    }

    /** Returns true if the deadline is today. */
    fun isDueToday(deadlineIso: String): Boolean {
        val deadline = parseDate(deadlineIso) ?: return false
        return isSameDay(deadline, today())
    }

    /** Returns true if the deadline is strictly in the future (not today). */
    fun isUpcoming(deadlineIso: String): Boolean {
        val deadline = parseDate(deadlineIso) ?: return false
        return deadline.after(today()) && !isSameDay(deadline, today())
    }

    /** Days remaining until deadline. Negative means overdue. */
    fun daysRemaining(deadlineIso: String): Long {
        val deadline = parseDate(deadlineIso) ?: return 0L
        val todayMs  = today().time
        val diff     = deadline.time - todayMs
        return diff / (1000L * 60 * 60 * 24)
    }

    // ─────────────────────────────────────────────────────────────
    // Formatting
    // ─────────────────────────────────────────────────────────────

    /** Format yyyy-MM-dd → "10 September 2026" */
    fun formatDisplayDate(isoDate: String): String {
        val date = parseDate(isoDate) ?: return isoDate
        return DISPLAY_FORMAT.format(date)
    }

    /** Format ISO datetime → "10:30 AM" */
    fun formatTime(isoString: String): String {
        val date = parseDateTime(isoString) ?: return ""
        return TIME_FORMAT.format(date)
    }

    /**
     * Human-readable relative time label for notifications:
     * "Just now", "X minutes ago", "X hours ago", "Today", "Yesterday", or the display date.
     */
    fun formatRelativeTime(isoString: String): String {
        val date = parseDateTime(isoString) ?: return isoString
        val now  = Date()
        val diffMs = now.time - date.time
        val diffMin = diffMs / (1000 * 60)
        val diffHr  = diffMs / (1000 * 60 * 60)

        return when {
            diffMs < 60_000L          -> "Just now"
            diffMin < 60             -> "$diffMin min ago"
            diffHr  < 24             -> formatTime(isoString)
            isSameDay(date, yesterday()) -> "Yesterday"
            else                     -> formatDisplayDate(isoString.take(10))
        }
    }

    /**
     * Human-readable deadline label for assignment cards:
     * "Today", "Tomorrow", "In X days", "X days ago"
     */
    fun formatDeadlineLabel(deadlineIso: String): String {
        val days = daysRemaining(deadlineIso)
        return when {
            days == 0L   -> "Today"
            days == 1L   -> "Tomorrow"
            days > 1L    -> "In $days days"
            days == -1L  -> "Yesterday"
            else         -> "${-days} days ago"
        }
    }

    /**
     * Remaining-time string for the details screen.
     * "3 days remaining", "Due today", "X days overdue".
     */
    fun formatRemainingTime(deadlineIso: String): String {
        val days = daysRemaining(deadlineIso)
        return when {
            days > 1L   -> "$days days remaining"
            days == 1L  -> "1 day remaining"
            days == 0L  -> "Due today"
            days == -1L -> "1 day overdue"
            else        -> "${-days} days overdue"
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Internal helpers
    // ─────────────────────────────────────────────────────────────

    private fun isSameDay(a: Date, b: Date): Boolean {
        val calA = Calendar.getInstance().also { it.time = a }
        val calB = Calendar.getInstance().also { it.time = b }
        return calA.get(Calendar.YEAR)         == calB.get(Calendar.YEAR)
            && calA.get(Calendar.DAY_OF_YEAR)  == calB.get(Calendar.DAY_OF_YEAR)
    }

    private fun yesterday(): Date {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0);      cal.set(Calendar.MILLISECOND, 0)
        return cal.time
    }
}
