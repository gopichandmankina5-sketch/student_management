package com.example.studentmanagement.notification

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studentmanagement.assignment.AssignmentDetailsActivity
import com.example.studentmanagement.auth.SessionManager
import com.example.studentmanagement.database.NotificationEntity
import com.example.studentmanagement.databinding.ActivityNotificationBinding
import com.example.studentmanagement.repository.NotificationRepository
import com.example.studentmanagement.utils.Constants
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NotificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationBinding
    private lateinit var repository: NotificationRepository
    private lateinit var adapter: NotificationAdapter
    private lateinit var sessionManager: SessionManager
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = NotificationRepository(this)
        sessionManager = SessionManager(this)

        setupToolbar()
        setupRecyclerView()

        // Use FirebaseAuth UID as primary source — never use hashCode or Int conversion
        val studentUid = auth.currentUser?.uid
            ?: sessionManager.getUid().takeIf { it.isNotEmpty() }
            ?: ""

        if (studentUid.isEmpty()) {
            // Keep the screen open — show error instead of crashing or finishing
            binding.progressBar.visibility = View.GONE
            binding.layoutError.visibility = View.VISIBLE
            binding.tvErrorMessage.text = "Unable to identify your account. Please log out and log in again."
            return
        }

        loadNotifications(studentUid)
        observeNotifications(studentUid)

        binding.btnRetry.setOnClickListener {
            loadNotifications(studentUid)
        }

        binding.btnMarkAllRead.setOnClickListener {
            markAllAsRead(studentUid)
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = NotificationAdapter { notification ->
            handleNotificationClick(notification)
        }
        binding.rvNotifications.layoutManager = LinearLayoutManager(this)
        binding.rvNotifications.adapter = adapter
    }

    private fun loadNotifications(studentUid: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.layoutError.visibility = View.GONE
        binding.layoutEmpty.visibility = View.GONE
        binding.rvNotifications.visibility = View.GONE

        lifecycleScope.launch {
            when (val result = repository.loadNotifications(studentUid)) {
                is NotificationRepository.Result.Success -> {
                    // Handled by observer
                }
                is NotificationRepository.Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.layoutError.visibility = View.VISIBLE
                    binding.tvErrorMessage.text = result.message
                }
            }
        }
    }

    private fun observeNotifications(studentUid: String) {
        lifecycleScope.launch {
            repository.observeNotifications(studentUid).collectLatest { notifications ->
                binding.progressBar.visibility = View.GONE
                if (notifications.isEmpty()) {
                    binding.rvNotifications.visibility = View.GONE
                    binding.layoutEmpty.visibility = View.VISIBLE
                } else {
                    binding.rvNotifications.visibility = View.VISIBLE
                    binding.layoutEmpty.visibility = View.GONE
                    adapter.submitList(notifications)
                }
            }
        }
    }

    private fun handleNotificationClick(notification: NotificationEntity) {
        // 1. Mark as read if unread
        if (!notification.isRead) {
            lifecycleScope.launch {
                repository.markAsRead(notification.id)
            }
        }

        // 2. Navigate based on type
        if (notification.relatedAssignmentId != null) {
            val intent = Intent(this, AssignmentDetailsActivity::class.java).apply {
                putExtra(Constants.EXTRA_ASSIGNMENT_ID, notification.relatedAssignmentId)
            }
            startActivity(intent)
        } else {
            // Can show a bottom sheet or dialog with full message if needed
            Snackbar.make(binding.root, notification.message, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun markAllAsRead(studentUid: String) {
        lifecycleScope.launch {
            repository.markAllAsRead(studentUid)
            Snackbar.make(binding.root, "All notifications marked as read", Snackbar.LENGTH_SHORT).show()
        }
    }
}
