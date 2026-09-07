package com.example.studentmanagement.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.studentmanagement.auth.LoginActivity
import com.example.studentmanagement.auth.SessionManager
import com.example.studentmanagement.databinding.ActivityDashboardBinding
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
class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var repository: StudentRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        repository = StudentRepository(this)

        // Redirect to login if session expired
        if (!sessionManager.isLoggedIn()) {
            navigateToLogin()
            return
        }

        loadStudentInfo()
        setupNavigationCards()
        setupLogoutButton()
    }

    // ─────────────────────────────────────────────────────────────
    // Data loading
    // ─────────────────────────────────────────────────────────────

    private fun loadStudentInfo() {
        // Immediately display session-cached values (fast)
        val name = sessionManager.getStudentName()
        val email = sessionManager.getStudentEmail()
        binding.tvWelcome.text = "Welcome, $name 👋"
        binding.tvEmail.text = email

        // Load full profile from Room for department + year display
        val studentId = sessionManager.getStudentId()
        if (studentId > 0L) {
            lifecycleScope.launch {
                when (val result = repository.getStudentById(studentId)) {
                    is StudentRepository.RepositoryResult.Success -> {
                        val student = result.data
                        binding.tvDepartmentInfo.text =
                            "${student.department} • ${student.year}"
                        // Update session name in case it was edited
                        sessionManager.updateName(student.name)
                        binding.tvWelcome.text = "Welcome, ${student.name} 👋"
                    }
                    is StudentRepository.RepositoryResult.Error -> {
                        // Non-critical: session data already displayed
                    }
                }
            }
        }

        // ── Mock dashboard stats (replace with API call when Member 4 is ready) ──
        // To load real data: call loadDashboardFromApi(studentId)
        binding.tvAttendanceValue.text = "82%"
        binding.tvCoursesValue.text = "6"
        binding.tvAssignmentsValue.text = "3"
        binding.tvNotifValue.text = "2"
    }

    /*
     * ── STUB: Uncomment when Member 4's backend is available ──
     *
     * private fun loadDashboardFromApi(studentId: Long) {
     *     lifecycleScope.launch {
     *         try {
     *             val token = sessionManager.getBearerToken()
     *             val response = RetrofitClient.api.getDashboard(studentId, token)
     *             if (response.isSuccessful) {
     *                 val data = response.body() ?: return@launch
     *                 binding.tvAttendanceValue.text = "${data.attendancePercentage}%"
     *                 binding.tvCoursesValue.text = data.enrolledCourses.toString()
     *                 binding.tvAssignmentsValue.text = data.upcomingAssignments.toString()
     *                 binding.tvNotifValue.text = data.unreadNotifications.toString()
     *             }
     *         } catch (e: Exception) {
     *             // API unavailable — mock data already shown, no crash
     *         }
     *     }
     * }
     */

    // ─────────────────────────────────────────────────────────────
    // Navigation
    // ─────────────────────────────────────────────────────────────

    private fun setupNavigationCards() {
        // Profile — implemented (Member 1)
        binding.navProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // ── Placeholders for other team members ──

        // Member 2: Attendance module
        binding.navAttendance.setOnClickListener {
            showComingSoonMessage("Attendance module — Member 2")
        }

        // Member 2: Courses module
        binding.navCourses.setOnClickListener {
            showComingSoonMessage("Courses module — Member 2")
        }

        // Member 3: Assignments module
        binding.navAssignments.setOnClickListener {
            showComingSoonMessage("Assignments module — Member 3")
        }

        // Member 3: Notifications module
        binding.navNotifications.setOnClickListener {
            showComingSoonMessage("Notifications module — Member 3")
        }

        // Stat card shortcuts
        binding.cardAttendance.setOnClickListener {
            showComingSoonMessage("Attendance — Member 2")
        }
        binding.cardCourses.setOnClickListener {
            showComingSoonMessage("Courses — Member 2")
        }
        binding.cardAssignments.setOnClickListener {
            showComingSoonMessage("Assignments — Member 3")
        }
        binding.cardNotificationsStat.setOnClickListener {
            showComingSoonMessage("Notifications — Member 3")
        }
    }

    private fun showComingSoonMessage(module: String) {
        Snackbar.make(
            binding.root,
            "🔜 $module — coming soon!",
            Snackbar.LENGTH_SHORT
        ).show()
    }

    // ─────────────────────────────────────────────────────────────
    // Logout
    // ─────────────────────────────────────────────────────────────

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
        sessionManager.logout()
        navigateToLogin()
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    // ─────────────────────────────────────────────────────────────
    // Lifecycle — refresh data when returning from Profile
    // ─────────────────────────────────────────────────────────────

    override fun onResume() {
        super.onResume()
        // Refresh name in case the user edited their profile
        val name = sessionManager.getStudentName()
        binding.tvWelcome.text = "Welcome, $name 👋"
    }
}
