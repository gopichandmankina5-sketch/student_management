package com.example.studentmanagement.profile

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.studentmanagement.auth.SessionManager
import com.example.studentmanagement.databinding.ActivityProfileBinding
import com.example.studentmanagement.model.Student
import com.example.studentmanagement.repository.StudentRepository
import com.example.studentmanagement.utils.ValidationUtils
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

/**
 * Student Profile screen.
 *
 * States:
 *   VIEW mode  — all fields are read-only; "Edit Profile" button visible
 *   EDIT mode  — all fields are editable; "Save Changes" + "Cancel" buttons visible
 *
 * Data flow:
 *   Load from Room → Display → (if edit) Validate → Save to Room → Refresh display
 */
class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var repository: StudentRepository

    private var currentStudent: Student? = null
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        repository = StudentRepository(this)

        setupToolbar()
        setupClickListeners()
        loadProfile()
    }

    // ─────────────────────────────────────────────────────────────
    // Setup
    // ─────────────────────────────────────────────────────────────

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(com.example.studentmanagement.R.string.student_profile)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupClickListeners() {
        binding.btnEditToggle.setOnClickListener {
            if (isEditMode) {
                // Currently in edit mode — clicking means "cancel edit"
                cancelEdit()
            } else {
                enterEditMode()
            }
        }

        binding.btnSave.setOnClickListener {
            saveProfile()
        }

        binding.btnCancel.setOnClickListener {
            cancelEdit()
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Data loading
    // ─────────────────────────────────────────────────────────────

    private fun loadProfile() {
        setProgressVisible(true)
        val studentId = sessionManager.getStudentId()
        lifecycleScope.launch {
            when (val result = repository.getStudentById(studentId)) {
                is StudentRepository.RepositoryResult.Success -> {
                    setProgressVisible(false)
                    currentStudent = result.data
                    populateFields(result.data)
                }
                is StudentRepository.RepositoryResult.Error -> {
                    setProgressVisible(false)
                    showMessage(result.message, isError = true)
                }
            }
        }
    }

    private fun populateFields(student: Student) {
        binding.etName.setText(student.name)
        binding.etRollNumber.setText(student.rollNumber)
        binding.etEmail.setText(student.email)
        binding.etDepartment.setText(student.department)
        binding.etYear.setText(student.year)

        // Avatar initials
        val initials = student.name
            .split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()
        binding.tvAvatarInitials.text = initials.ifEmpty { "S" }

        // Header labels
        binding.tvProfileName.text = student.name
        binding.tvProfileRoll.text = "Roll: ${student.rollNumber}"
        binding.chipDepartment.text = "${student.department} • ${student.year}"
    }

    // ─────────────────────────────────────────────────────────────
    // Edit / View mode switching
    // ─────────────────────────────────────────────────────────────

    private fun enterEditMode() {
        isEditMode = true
        setFieldsEnabled(true)
        binding.btnEditToggle.text = getString(com.example.studentmanagement.R.string.cancel)
        binding.btnSave.visibility = View.VISIBLE
        binding.btnCancel.visibility = View.VISIBLE
    }

    private fun cancelEdit() {
        isEditMode = false
        setFieldsEnabled(false)
        binding.btnEditToggle.text = getString(com.example.studentmanagement.R.string.edit_profile)
        binding.btnSave.visibility = View.GONE
        binding.btnCancel.visibility = View.GONE
        // Restore original values
        currentStudent?.let { populateFields(it) }
        clearErrors()
    }

    private fun setFieldsEnabled(enabled: Boolean) {
        binding.etName.isEnabled = enabled
        binding.etRollNumber.isEnabled = enabled
        binding.etEmail.isEnabled = enabled
        binding.etDepartment.isEnabled = enabled
        binding.etYear.isEnabled = enabled
    }

    // ─────────────────────────────────────────────────────────────
    // Save profile
    // ─────────────────────────────────────────────────────────────

    private fun saveProfile() {
        clearErrors()

        val name = binding.etName.text.toString()
        val rollNumber = binding.etRollNumber.text.toString()
        val email = binding.etEmail.text.toString()
        val department = binding.etDepartment.text.toString()
        val year = binding.etYear.text.toString()

        // Validate
        val errors = ValidationUtils.validateProfileFields(name, rollNumber, email, department, year)
        if (errors.isNotEmpty()) {
            errors["name"]?.let { binding.tilName.error = it }
            errors["rollNumber"]?.let { binding.tilRollNumber.error = it }
            errors["email"]?.let { binding.tilEmail.error = it }
            errors["department"]?.let { binding.tilDepartment.error = it }
            errors["year"]?.let { binding.tilYear.error = it }
            return
        }

        val student = currentStudent?.copy(
            name = name.trim(),
            rollNumber = rollNumber.trim(),
            email = email.trim().lowercase(),
            department = department.trim(),
            year = year.trim()
        ) ?: return

        setProgressVisible(true)
        lifecycleScope.launch {
            when (val result = repository.updateStudent(student)) {
                is StudentRepository.RepositoryResult.Success -> {
                    setProgressVisible(false)
                    currentStudent = student
                    // Update session so Dashboard shows updated name
                    sessionManager.updateName(student.name)
                    populateFields(student)
                    cancelEdit()   // Return to view mode
                    showMessage(getString(com.example.studentmanagement.R.string.profile_saved))
                }
                is StudentRepository.RepositoryResult.Error -> {
                    setProgressVisible(false)
                    showMessage(result.message, isError = true)
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // UI helpers
    // ─────────────────────────────────────────────────────────────

    private fun clearErrors() {
        binding.tilName.error = null
        binding.tilRollNumber.error = null
        binding.tilEmail.error = null
        binding.tilDepartment.error = null
        binding.tilYear.error = null
    }

    private fun setProgressVisible(visible: Boolean) {
        binding.progressProfile.visibility = if (visible) View.VISIBLE else View.GONE
    }

    private fun showMessage(message: String, isError: Boolean = false) {
        val snackbar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
        if (isError) {
            snackbar.setBackgroundTint(getColor(com.example.studentmanagement.R.color.colorError))
            snackbar.setTextColor(getColor(com.example.studentmanagement.R.color.white))
        } else {
            snackbar.setBackgroundTint(getColor(com.example.studentmanagement.R.color.colorSuccess))
            snackbar.setTextColor(getColor(com.example.studentmanagement.R.color.white))
        }
        snackbar.show()
    }
}
