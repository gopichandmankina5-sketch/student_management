package com.example.studentmanagement.dashboard

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.example.studentmanagement.auth.SessionManager
import com.example.studentmanagement.databinding.ActivityCreateFacultyBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Admin-only screen: Creates a new Faculty account.
 *
 * SECURITY:
 * - Only accessible from AdminDashboardActivity.
 * - Role is ALWAYS set to "FACULTY" — not user-selectable.
 * - Uses a secondary FirebaseAuth instance so creating the faculty account
 *   does NOT sign out the current Admin session.
 * - The Admin session is preserved throughout.
 *
 * Flow:
 *   1. Admin fills in name, email, password, department
 *   2. A secondary Auth instance creates the Firebase Auth account
 *   3. Firestore documents are written: users/{uid} + faculty/{uid}
 *   4. Secondary Auth instance is immediately signed out (Admin stays signed in)
 */
class CreateFacultyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateFacultyBinding
    private lateinit var sessionManager: SessionManager
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateFacultyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        // Security gate: only admin can reach this screen
        if (sessionManager.getRole() != "ADMIN") {
            Snackbar.make(binding.root, "Access denied: Admin only", Snackbar.LENGTH_LONG).show()
            finish()
            return
        }

        binding.btnBack.setOnClickListener { finish() }

        binding.btnCreateFaculty.setOnClickListener {
            attemptCreateFaculty()
        }
    }

    private fun attemptCreateFaculty() {
        clearErrors()

        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val department = binding.etDepartment.text.toString().trim()

        // Validate
        var hasError = false
        if (name.isEmpty()) {
            binding.tilName.error = "Name is required"
            hasError = true
        }
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Valid email is required"
            hasError = true
        }
        if (password.length < 6) {
            binding.tilPassword.error = "Password must be at least 6 characters"
            hasError = true
        }
        if (department.isEmpty()) {
            binding.tilDepartment.error = "Department is required"
            hasError = true
        }
        if (hasError) return

        setLoading(true)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Use a SECONDARY FirebaseAuth instance so the Admin session is preserved
                val secondaryApp = withContext(Dispatchers.IO) {
                    try {
                        com.google.firebase.FirebaseApp.getInstance("secondary")
                    } catch (e: Exception) {
                        // Initialize secondary app if not already done
                        val options = com.google.firebase.FirebaseApp.getInstance().options
                        com.google.firebase.FirebaseApp.initializeApp(
                            applicationContext, options, "secondary"
                        )
                    }
                }
                val secondaryAuth = FirebaseAuth.getInstance(secondaryApp as com.google.firebase.FirebaseApp)

                val authResult = withContext(Dispatchers.IO) {
                    secondaryAuth.createUserWithEmailAndPassword(email, password).await()
                }
                val uid = authResult.user?.uid ?: throw Exception("Failed to get UID after creation")

                // Write Firestore documents
                val role = "FACULTY"
                withContext(Dispatchers.IO) {
                    val userData = hashMapOf(
                        "uid" to uid,
                        "name" to name,
                        "email" to email,
                        "department" to department,
                        "role" to role
                    )
                    firestore.collection("users").document(uid).set(userData).await()

                    val facultyData = hashMapOf(
                        "uid" to uid,
                        "name" to name,
                        "email" to email,
                        "department" to department,
                        "role" to role
                    )
                    firestore.collection("faculty").document(uid).set(facultyData).await()
                }

                // Sign out the secondary auth instance immediately to keep Admin session clean
                withContext(Dispatchers.IO) {
                    secondaryAuth.signOut()
                }

                setLoading(false)
                Snackbar.make(binding.root, "Faculty account created: $name", Snackbar.LENGTH_LONG).show()
                // Clear form for adding another
                clearForm()

            } catch (e: Exception) {
                setLoading(false)
                val msg = when {
                    e.message?.contains("email address is already in use", ignoreCase = true) == true ->
                        "An account with this email already exists"
                    e.message?.contains("network", ignoreCase = true) == true ->
                        "Network error. Please check your connection."
                    else -> "Failed to create faculty: ${e.localizedMessage}"
                }
                Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun clearErrors() {
        binding.tilName.error = null
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        binding.tilDepartment.error = null
    }

    private fun clearForm() {
        binding.etName.text?.clear()
        binding.etEmail.text?.clear()
        binding.etPassword.text?.clear()
        binding.etDepartment.text?.clear()
    }

    private fun setLoading(loading: Boolean) {
        binding.btnCreateFaculty.isEnabled = !loading
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }
}
