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

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        sessionManager = SessionManager(this)

        if (!sessionManager.isLoggedIn()) {
            navigateToLogin()
            return
        }

        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        tvWelcome.text = "Welcome, ${sessionManager.getStudentName()} (Admin)"

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            showLogoutConfirmation()
        }

        setupNavigationCards()
    }

    private fun setupNavigationCards() {
        // Manage Students — fully implemented with Firestore
        findViewById<MaterialCardView>(R.id.navManageStudents).setOnClickListener {
            startActivity(Intent(this, ManageStudentsActivity::class.java))
        }

        // Manage Faculty — implemented with Firestore
        findViewById<MaterialCardView>(R.id.navManageFaculty).setOnClickListener {
            startActivity(Intent(this, ManageFacultyActivity::class.java))
        }

        // Manage Courses — implemented with Firestore
        findViewById<MaterialCardView>(R.id.navManageCourses).setOnClickListener {
            startActivity(Intent(this, ManageCoursesActivity::class.java))
        }
        
        findViewById<MaterialCardView>(R.id.navManageAssignments).setOnClickListener {
            showComingSoonMessage("Manage Assignments")
        }
        findViewById<MaterialCardView>(R.id.navManageNotifications).setOnClickListener {
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
