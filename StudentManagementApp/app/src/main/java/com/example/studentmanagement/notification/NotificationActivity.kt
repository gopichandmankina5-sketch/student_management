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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NotificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationBinding
    private lateinit var repository: NotificationRepository
    private lateinit var adapter: NotificationAdapter
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = NotificationRepository(this)
        sessionManager = SessionManager(this)

        setupToolbar()
        setupRecyclerView()

        val studentId = sessionManager.getStudentId().toInt()
        loadNotifications(studentId)
        observeNotifications(studentId)

        binding.btnRetry.setOnClickListener {
            loadNotifications(studentId)
        }

        binding.btnMarkAllRead.setOnClickListener {
            markAllAsRead(studentId)
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

    private fun loadNotifications(studentId: Int) {
        binding.progressBar.visibility = View.VISIBLE
        binding.layoutError.visibility = View.GONE
        binding.layoutEmpty.visibility = View.GONE
        binding.rvNotifications.visibility = View.GONE

        lifecycleScope.launch {
            when (val result = repository.loadNotifications(studentId)) {
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

    private fun observeNotifications(studentId: Int) {
        lifecycleScope.launch {
            repository.observeNotifications(studentId).collectLatest { notifications ->
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

    private fun markAllAsRead(studentId: Int) {
        lifecycleScope.launch {
            repository.markAllAsRead(studentId)
            Snackbar.make(binding.root, "All notifications marked as read", Snackbar.LENGTH_SHORT).show()
        }
    }
}
