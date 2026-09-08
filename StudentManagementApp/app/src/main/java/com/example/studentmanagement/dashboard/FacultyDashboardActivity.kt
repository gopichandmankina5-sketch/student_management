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
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
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
