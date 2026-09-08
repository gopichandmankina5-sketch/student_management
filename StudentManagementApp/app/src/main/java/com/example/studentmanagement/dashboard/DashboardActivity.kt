package com.example.studentmanagement.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.studentmanagement.assignment.AssignmentActivity
import com.example.studentmanagement.auth.LoginActivity
import com.example.studentmanagement.auth.SessionManager
import com.example.studentmanagement.databinding.ActivityDashboardBinding
import com.example.studentmanagement.notification.NotificationActivity

import com.example.studentmanagement.profile.ProfileActivity
import com.example.studentmanagement.repository.StudentRepository
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

/**
 * Student Dashboard — the main hub of the app.
 *
 * Shows:
 *   - Student name + email + department/year
 *   - 4 stat cards: Attendance, Courses, Assignments, Notifications
 *   - Recent activity notifications
 *   - Quick-action navigation cards for all modules
 *
 * ─────────────────────────────────────────────────────────────────
 * TEAM INTEGRATION GUIDE:
 * ─────────────────────────────────────────────────────────────────
 * Member 2 (Attendance + Courses):
 *   → Connect navAttendance and navCourses click listeners
 *   → Replace mock stat values in tvAttendanceValue and tvCoursesValue
 *     with real data from their module/API
 *
 * Member 3 (Assignments + Notifications):
 *   → Connect navAssignments and navNotifications click listeners
 *   → Replace mock stat values in tvAssignmentsValue and tvNotifValue
 *     with real data from their module/API
 *
 * Member 4 (Backend):
 *   → The getDashboard() API call stub is ready in StudentRepository
 *   → Uncomment the loadDashboardFromApi() call once the API is live
 * ─────────────────────────────────────────────────────────────────
 */
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var repository: StudentRepository
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        repository = StudentRepository(this)

        if (!sessionManager.isLoggedIn()) {
            navigateToLogin()
            return
        }

        loadStudentInfo()
        setupNavigationCards()
        setupLogoutButton()
    }

    private fun loadStudentInfo() {
        val name = sessionManager.getStudentName()
        val email = sessionManager.getStudentEmail()
        binding.tvWelcome.text = "Welcome, $name \uD83D\uDC4B"
        binding.tvEmail.text = email

        val studentId = sessionManager.getStudentId()
        if (studentId > 0L) {
            lifecycleScope.launch {
                when (val result = repository.getStudentById(studentId)) {
                    is StudentRepository.RepositoryResult.Success -> {
                        val student = result.data
                        binding.tvDepartmentInfo.text = "${student.department} • ${student.year}"
                        sessionManager.updateName(student.name)
                        binding.tvWelcome.text = "Welcome, ${student.name} \uD83D\uDC4B"
                    }
                    is StudentRepository.RepositoryResult.Error -> {}
                }
            }
        }

        binding.tvAttendanceValue.text = "-"
        binding.tvCoursesValue.text = "-"
        binding.tvAssignmentsValue.text = "-"
        binding.tvNotifValue.text = "-"
        
        loadDashboardFromApi(studentId)
    }

    private fun loadDashboardFromApi(studentId: Long) {
        lifecycleScope.launch {
            try {
                // Member 1 implementation for dashboard using Firestore
                val assignmentsCount = firestore.collection("assignments")
                    .whereEqualTo("studentId", studentId.toInt())
                    .get()
                    .await()
                    .size()
                
                val notificationsCount = firestore.collection("notifications")
                    .whereEqualTo("studentId", studentId.toInt())
                    .whereEqualTo("isRead", false)
                    .get()
                    .await()
                    .size()

                binding.tvAttendanceValue.text = "0%" // Member 2
                binding.tvCoursesValue.text = "0"     // Member 2
                binding.tvAssignmentsValue.text = assignmentsCount.toString()
                binding.tvNotifValue.text = notificationsCount.toString()
            } catch (e: Exception) {
                Snackbar.make(binding.root, "Unable to load data. Please try again later.", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupNavigationCards() {
        binding.navProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        binding.navAttendance.setOnClickListener {
            showComingSoonMessage("Attendance module — Member 2")
        }
        binding.navCourses.setOnClickListener {
            showComingSoonMessage("Courses module — Member 2")
        }
        binding.navAssignments.setOnClickListener {
            startActivity(Intent(this, AssignmentActivity::class.java))
        }
        binding.navNotifications.setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }
        binding.cardAttendance.setOnClickListener {
            showComingSoonMessage("Attendance — Member 2")
        }
        binding.cardCourses.setOnClickListener {
            showComingSoonMessage("Courses — Member 2")
        }
        binding.cardAssignments.setOnClickListener {
            startActivity(Intent(this, AssignmentActivity::class.java))
        }
        binding.cardNotificationsStat.setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }
    }

    private fun showComingSoonMessage(module: String) {
        Snackbar.make(binding.root, "\uD83D\uDD1C $module — coming soon!", Snackbar.LENGTH_SHORT).show()
    }

    private fun setupLogoutButton() {
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ -> performLogout() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        FirebaseAuth.getInstance().signOut()
        sessionManager.logout()
        navigateToLogin()
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    override fun onResume() {
        super.onResume()
        val name = sessionManager.getStudentName()
        binding.tvWelcome.text = "Welcome, $name \uD83D\uDC4B"
    }
}
