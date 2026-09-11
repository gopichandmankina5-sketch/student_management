package com.example.studentmanagement.dashboard

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.studentmanagement.R
import com.example.studentmanagement.auth.LoginActivity
import com.example.studentmanagement.auth.SessionManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth

class FacultyDashboardActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faculty_dashboard)

        sessionManager = SessionManager(this)

        if (!sessionManager.isLoggedIn()) {
            navigateToLogin()
            return
        }

        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        tvWelcome.text = "Welcome, ${sessionManager.getStudentName()} (Faculty)"

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            showLogoutConfirmation()
        }

        setupNavigationCards()
    }

    private fun setupNavigationCards() {
        // My Courses — shows courses assigned to this faculty member
        findViewById<MaterialCardView>(R.id.navMyCourses).setOnClickListener {
            startActivity(Intent(this, FacultyCoursesActivity::class.java))
        }

        // Take Attendance
        findViewById<MaterialCardView>(R.id.navMarkAttendance).setOnClickListener {
            startActivity(Intent(this, com.example.studentmanagement.faculty.FacultyAttendanceActivity::class.java))
        }

        // Manage Assignments
        findViewById<MaterialCardView>(R.id.navAssignments).setOnClickListener {
            startActivity(Intent(this, com.example.studentmanagement.assignment.FacultyAssignmentListActivity::class.java))
        }
        
        // My Students
        findViewById<MaterialCardView>(R.id.navMyStudents).setOnClickListener {
            startActivity(Intent(this, com.example.studentmanagement.faculty.FacultyStudentsActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.navNotifications).setOnClickListener {
            showComingSoonMessage("Manage Notifications")
        }
    }

    private fun showComingSoonMessage(module: String) {
        Snackbar.make(
            findViewById(android.R.id.content),
            "🚧 $module — coming soon!",
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                FirebaseAuth.getInstance().signOut()
                sessionManager.logout()
                navigateToLogin()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
