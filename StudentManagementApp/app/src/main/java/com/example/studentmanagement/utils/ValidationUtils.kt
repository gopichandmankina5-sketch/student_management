package com.example.studentmanagement.utils

import android.util.Patterns
import com.example.studentmanagement.utils.Constants.MIN_PASSWORD_LENGTH

/**
 * Reusable, unit-testable validation utilities.
 *
 * All functions return a [ValidationResult] so callers can display
 * the specific error message directly on the relevant input field.
 */
object ValidationUtils {

    // ─────────────────────────────────────────────────────────────
    // Result type
    // ─────────────────────────────────────────────────────────────

    sealed class ValidationResult {
        object Valid : ValidationResult()
        data class Invalid(val errorMessage: String) : ValidationResult()
    }

    // ─────────────────────────────────────────────────────────────
    // Individual field validators
    // ─────────────────────────────────────────────────────────────

    /** Returns true if the email matches Android's built-in EMAIL_ADDRESS pattern. */
    fun isValidEmail(email: String): Boolean =
        email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()

    fun validateEmail(email: String): ValidationResult {
        if (email.isBlank()) return ValidationResult.Invalid("Email is required")
        if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches())
            return ValidationResult.Invalid("Please enter a valid email address")
        return ValidationResult.Valid
    }

    fun validatePassword(password: String): ValidationResult {
        if (password.isBlank()) return ValidationResult.Invalid("Password is required")
        if (password.length < MIN_PASSWORD_LENGTH)
            return ValidationResult.Invalid("Password must be at least $MIN_PASSWORD_LENGTH characters")
        return ValidationResult.Valid
    }

    fun validateConfirmPassword(password: String, confirm: String): ValidationResult {
        if (confirm.isBlank()) return ValidationResult.Invalid("Please confirm your password")
        if (password != confirm) return ValidationResult.Invalid("Passwords do not match")
        return ValidationResult.Valid
    }

    fun validateName(name: String): ValidationResult {
        if (name.isBlank()) return ValidationResult.Invalid("Full name is required")
        if (name.trim().length < 2) return ValidationResult.Invalid("Name is too short")
        return ValidationResult.Valid
    }

    fun validateRollNumber(rollNumber: String): ValidationResult {
        if (rollNumber.isBlank()) return ValidationResult.Invalid("Roll number is required")
        return ValidationResult.Valid
    }

    fun validateDepartment(department: String): ValidationResult {
        if (department.isBlank()) return ValidationResult.Invalid("Department is required")
        return ValidationResult.Valid
    }

    fun validateYear(year: String): ValidationResult {
        if (year.isBlank()) return ValidationResult.Invalid("Year / Semester is required")
        return ValidationResult.Valid
    }

    // ─────────────────────────────────────────────────────────────
    // Composite validators
    // ─────────────────────────────────────────────────────────────

    /**
     * Validates all login fields.
     * Returns a map of field name → error string (empty map = all valid).
     */
    fun validateLoginFields(email: String, password: String): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        validateEmail(email).let { if (it is ValidationResult.Invalid) errors["email"] = it.errorMessage }
        validatePassword(password).let { if (it is ValidationResult.Invalid) errors["password"] = it.errorMessage }
        return errors
    }

    /**
     * Validates all registration fields.
     * Returns a map of field name → error string (empty map = all valid).
     */
    fun validateRegisterFields(
        name: String,
        rollNumber: String,
        email: String,
        password: String,
        confirmPassword: String,
        department: String,
        year: String
    ): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        validateName(name).let { if (it is ValidationResult.Invalid) errors["name"] = it.errorMessage }
        validateRollNumber(rollNumber).let { if (it is ValidationResult.Invalid) errors["rollNumber"] = it.errorMessage }
        validateEmail(email).let { if (it is ValidationResult.Invalid) errors["email"] = it.errorMessage }
        validateDepartment(department).let { if (it is ValidationResult.Invalid) errors["department"] = it.errorMessage }
        validateYear(year).let { if (it is ValidationResult.Invalid) errors["year"] = it.errorMessage }
        validatePassword(password).let { if (it is ValidationResult.Invalid) errors["password"] = it.errorMessage }
        validateConfirmPassword(password, confirmPassword).let {
            if (it is ValidationResult.Invalid) errors["confirmPassword"] = it.errorMessage
        }
        return errors
    }

    /**
     * Validates all profile edit fields.
     * Returns a map of field name → error string (empty map = all valid).
     */
    fun validateProfileFields(
        name: String,
        rollNumber: String,
        email: String,
        department: String,
        year: String
    ): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        validateName(name).let { if (it is ValidationResult.Invalid) errors["name"] = it.errorMessage }
        validateRollNumber(rollNumber).let { if (it is ValidationResult.Invalid) errors["rollNumber"] = it.errorMessage }
        validateEmail(email).let { if (it is ValidationResult.Invalid) errors["email"] = it.errorMessage }
        validateDepartment(department).let { if (it is ValidationResult.Invalid) errors["department"] = it.errorMessage }
        validateYear(year).let { if (it is ValidationResult.Invalid) errors["year"] = it.errorMessage }
        return errors
    }
}
