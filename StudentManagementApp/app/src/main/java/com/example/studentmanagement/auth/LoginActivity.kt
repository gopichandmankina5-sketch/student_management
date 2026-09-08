package com.example.studentmanagement.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.studentmanagement.dashboard.AdminDashboardActivity
import com.example.studentmanagement.dashboard.DashboardActivity
import com.example.studentmanagement.dashboard.FacultyDashboardActivity
import com.example.studentmanagement.databinding.ActivityLoginBinding
import com.example.studentmanagement.repository.StudentRepository
import com.example.studentmanagement.utils.ValidationUtils
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

/**
 * Login screen.
 *
 * Flow:
 *   Validate fields → Authenticate via Repository → Save session → Navigate to Dashboard
 *
 * BACKEND INTEGRATION:
 *   The repository handles the local vs. remote switch transparently.
 *   When Member 4's API is ready, only StudentRepository needs to change;
 *   this Activity does not need modification.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var repository: StudentRepository
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = StudentRepository(this)
        sessionManager = SessionManager(this)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            attemptLogin()
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun attemptLogin() {
        // Clear previous errors
        binding.tilEmail.error = null
        binding.tilPassword.error = null

        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        // Validate inputs
        val errors = ValidationUtils.validateLoginFields(email, password)
        if (errors.isNotEmpty()) {
            errors["email"]?.let { binding.tilEmail.error = it }
            errors["password"]?.let { binding.tilPassword.error = it }
            return
        }

        // Perform login
        setLoading(true)
        lifecycleScope.launch {
            when (val result = repository.login(email, password)) {
                is StudentRepository.RepositoryResult.Success -> {
                    val data = result.data
                    val resolvedId = data.studentId ?: data.userId ?: 0L
                    val role = data.role?.uppercase() ?: "STUDENT"
                    sessionManager.saveSession(
                        studentId = resolvedId,
                        name = sessionManager.getStudentName(), // updated after profile load
                        email = email,
                        token = data.token,
                        role = role
                    )
                    // Load the student name from DB to store in session
                    if (resolvedId > 0L) {
                        val profileResult = repository.getStudentById(resolvedId)
                        if (profileResult is StudentRepository.RepositoryResult.Success) {
                            sessionManager.saveSession(
                                studentId = resolvedId,
                                name = profileResult.data.name,
                                email = email,
                                token = data.token,
                                role = role
                            )
                        }
                    }
                    navigateToDashboard()
                }
                is StudentRepository.RepositoryResult.Error -> {
                    setLoading(false)
                    showError(result.message)
                }
            }
        }
    }

    private fun navigateToDashboard() {
        val destination = when (sessionManager.getRole()) {
            "FACULTY" -> FacultyDashboardActivity::class.java
            "ADMIN" -> AdminDashboardActivity::class.java
            else -> DashboardActivity::class.java
        }
        startActivity(Intent(this, destination).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    private fun setLoading(loading: Boolean) {
        binding.btnLogin.isEnabled = !loading
        binding.progressLogin.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(getColor(com.example.studentmanagement.R.color.colorError))
            .setTextColor(getColor(com.example.studentmanagement.R.color.white))
            .show()
    }
}
