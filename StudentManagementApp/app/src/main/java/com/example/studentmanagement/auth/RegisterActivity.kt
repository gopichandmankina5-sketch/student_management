package com.example.studentmanagement.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.studentmanagement.dashboard.DashboardActivity
import com.example.studentmanagement.databinding.ActivityRegisterBinding
import com.example.studentmanagement.repository.StudentRepository
import com.example.studentmanagement.utils.ValidationUtils
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

/**
 * Registration screen.
 *
 * Flow:
 *   Validate all 7 fields → Create account in Repository → Save session → Navigate to Dashboard
 *
 * BACKEND INTEGRATION:
 *   The repository handles the local vs. remote switch transparently.
 *   When Member 4's API is ready, only StudentRepository needs to change;
 *   this Activity does not need modification.
 */
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var repository: StudentRepository
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = StudentRepository(this)
        sessionManager = SessionManager(this)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            attemptRegister()
        }

        binding.tvLogin.setOnClickListener {
            finish()   // Go back to LoginActivity
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun attemptRegister() {
        // Clear all previous errors
        clearErrors()

        val name = binding.etName.text.toString()
        val rollNumber = binding.etRollNumber.text.toString()
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()
        val department = binding.etDepartment.text.toString()
        val year = binding.etYear.text.toString()

        // Validate all fields
        val errors = ValidationUtils.validateRegisterFields(
            name = name,
            rollNumber = rollNumber,
            email = email,
            password = password,
            confirmPassword = confirmPassword,
            department = department,
            year = year
        )

        if (errors.isNotEmpty()) {
            errors["name"]?.let { binding.tilName.error = it }
            errors["rollNumber"]?.let { binding.tilRollNumber.error = it }
            errors["email"]?.let { binding.tilEmail.error = it }
            errors["department"]?.let { binding.tilDepartment.error = it }
            errors["year"]?.let { binding.tilYear.error = it }
            errors["password"]?.let { binding.tilPassword.error = it }
            errors["confirmPassword"]?.let { binding.tilConfirmPassword.error = it }
            return
        }

        // Perform registration
        setLoading(true)
        lifecycleScope.launch {
            when (val result = repository.register(
                name = name.trim(),
                rollNumber = rollNumber.trim(),
                email = email.trim(),
                password = password,
                department = department.trim(),
                year = year.trim()
            )) {
                is StudentRepository.RepositoryResult.Success -> {
                    val data = result.data
                    sessionManager.saveSession(
                        studentId = data.studentId ?: 0L,
                        name = name.trim(),
                        email = email.trim().lowercase(),
                        token = data.token
                    )
                    navigateToDashboard()
                }
                is StudentRepository.RepositoryResult.Error -> {
                    setLoading(false)
                    showError(result.message)
                }
            }
        }
    }

    private fun clearErrors() {
        binding.tilName.error = null
        binding.tilRollNumber.error = null
        binding.tilEmail.error = null
        binding.tilDepartment.error = null
        binding.tilYear.error = null
        binding.tilPassword.error = null
        binding.tilConfirmPassword.error = null
    }

    private fun navigateToDashboard() {
        startActivity(Intent(this, DashboardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }

    private fun setLoading(loading: Boolean) {
        binding.btnRegister.isEnabled = !loading
        binding.progressRegister.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(getColor(com.example.studentmanagement.R.color.colorError))
            .setTextColor(getColor(com.example.studentmanagement.R.color.white))
            .show()
    }
}
