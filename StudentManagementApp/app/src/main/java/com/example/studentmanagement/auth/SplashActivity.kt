package com.example.studentmanagement.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.studentmanagement.dashboard.DashboardActivity
import com.example.studentmanagement.databinding.ActivitySplashBinding
import com.example.studentmanagement.utils.Constants
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Entry point of the app.
 *
 * Flow:
 *   1. Show branded splash screen for SPLASH_DELAY_MS milliseconds
 *   2. Check if a session is already active (user previously logged in)
 *   3. Navigate to Dashboard (if logged in) or Login (if not)
 */
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        // Use a coroutine for the delay so we don't block the main thread
        lifecycleScope.launch {
            delay(Constants.SPLASH_DELAY_MS)
            navigateToNextScreen()
        }
    }

    private fun navigateToNextScreen() {
        val destination = if (sessionManager.isLoggedIn()) {
            DashboardActivity::class.java
        } else {
            LoginActivity::class.java
        }
        startActivity(Intent(this, destination))
        finish()   // Remove splash from back stack
    }
}
