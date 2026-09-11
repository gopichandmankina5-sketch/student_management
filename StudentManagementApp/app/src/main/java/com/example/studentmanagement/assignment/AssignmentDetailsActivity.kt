package com.example.studentmanagement.assignment

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.studentmanagement.database.AssignmentEntity
import com.example.studentmanagement.databinding.ActivityAssignmentDetailsBinding
import com.example.studentmanagement.model.SubmissionStatus
import com.example.studentmanagement.repository.AssignmentRepository
import com.example.studentmanagement.utils.Constants
import com.example.studentmanagement.utils.DateUtils
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

import com.example.studentmanagement.auth.SessionManager

class AssignmentDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAssignmentDetailsBinding
    private lateinit var repository: AssignmentRepository
    private var assignmentId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAssignmentDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = AssignmentRepository(this)
        assignmentId = intent.getStringExtra(Constants.EXTRA_ASSIGNMENT_ID) ?: ""

        setupToolbar()

        if (assignmentId.isNotEmpty()) {
            loadAssignmentDetails()
        } else {
            Toast.makeText(this, "Invalid assignment ID", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.btnMarkSubmitted.setOnClickListener {
            markAsSubmitted()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun loadAssignmentDetails() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            when (val result = repository.getAssignmentById(assignmentId)) {
                is AssignmentRepository.Result.Success -> {
                    binding.progressBar.visibility = View.GONE
                    populateUI(result.data)
                }
                is AssignmentRepository.Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@AssignmentDetailsActivity, result.message, Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun populateUI(assignment: AssignmentEntity) {
        binding.tvTitle.text = assignment.title
        binding.tvCourseInfo.text = "${assignment.courseName} • ${assignment.courseCode}"
        binding.tvDescription.text = assignment.description
        binding.tvAssignedDate.text = DateUtils.formatDisplayDate(assignment.assignedDate)
        binding.tvDeadline.text = DateUtils.formatDisplayDate(assignment.deadline)
        binding.tvRemainingTime.text = DateUtils.formatRemainingTime(assignment.deadline)

        val status = SubmissionStatus.fromString(assignment.submissionStatus)
        val isOverdue = DateUtils.isOverdue(assignment.deadline) && status != SubmissionStatus.SUBMITTED
        val isDueToday = DateUtils.isDueToday(assignment.deadline) && status != SubmissionStatus.SUBMITTED

        val displayStatus = when {
            status == SubmissionStatus.SUBMITTED -> SubmissionStatus.SUBMITTED
            isOverdue -> SubmissionStatus.OVERDUE
            isDueToday -> SubmissionStatus.DUE_TODAY
            else -> SubmissionStatus.NOT_SUBMITTED
        }

        binding.tvStatusText.text = displayStatus.name.replace("_", " ")

        val (statusColor, emoji) = when (displayStatus) {
            SubmissionStatus.SUBMITTED -> Color.parseColor("#1E8E3E") to "✅"
            SubmissionStatus.NOT_SUBMITTED -> Color.parseColor("#5F6368") to "📋"
            SubmissionStatus.OVERDUE -> Color.parseColor("#D93025") to "⚠️"
            SubmissionStatus.DUE_TODAY -> Color.parseColor("#F9AB00") to "⏳"
        }

        binding.tvStatusEmoji.text = emoji
        binding.tvStatusText.setTextColor(statusColor)
        
        val bannerBgColor = Color.argb(
            25,
            Color.red(statusColor),
            Color.green(statusColor),
            Color.blue(statusColor)
        )
        binding.cardStatusBanner.setCardBackgroundColor(bannerBgColor)

        if (status == SubmissionStatus.SUBMITTED) {
            binding.layoutSubmittedDate.visibility = View.VISIBLE
            binding.tvSubmittedDate.text = assignment.submittedDate?.let { DateUtils.formatDisplayDate(it) } ?: "N/A"
            binding.btnMarkSubmitted.isEnabled = false
            binding.btnMarkSubmitted.text = "SUBMITTED"
            binding.cardRemainingTime.visibility = View.GONE
        } else {
            binding.layoutSubmittedDate.visibility = View.GONE
            binding.btnMarkSubmitted.isEnabled = true
            binding.btnMarkSubmitted.text = "MARK AS SUBMITTED"
            binding.cardRemainingTime.visibility = View.VISIBLE
            
            // Color remaining time based on status
            val remainingTimeBg = Color.argb(25, Color.red(statusColor), Color.green(statusColor), Color.blue(statusColor))
            binding.cardRemainingTime.setCardBackgroundColor(remainingTimeBg)
            binding.tvRemainingTime.setTextColor(statusColor)
        }
    }

    private fun markAsSubmitted() {
        binding.btnMarkSubmitted.isEnabled = false
        binding.btnMarkSubmitted.text = "SUBMITTING..."
        
        lifecycleScope.launch {
            val studentUid = SessionManager(this@AssignmentDetailsActivity).getUid()
            when (val result = repository.submitAssignment(assignmentId, studentUid)) {
                is AssignmentRepository.Result.Success -> {
                    Snackbar.make(binding.root, "Assignment marked as submitted", Snackbar.LENGTH_SHORT).show()
                    loadAssignmentDetails() // Reload UI
                }
                is AssignmentRepository.Result.Error -> {
                    binding.btnMarkSubmitted.isEnabled = true
                    binding.btnMarkSubmitted.text = "MARK AS SUBMITTED"
                    Snackbar.make(binding.root, result.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }
}
