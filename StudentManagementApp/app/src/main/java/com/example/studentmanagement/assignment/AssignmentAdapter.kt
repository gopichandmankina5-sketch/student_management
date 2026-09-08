package com.example.studentmanagement.assignment

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.studentmanagement.database.AssignmentEntity
import com.example.studentmanagement.databinding.ItemAssignmentBinding
import com.example.studentmanagement.model.SubmissionStatus
import com.example.studentmanagement.utils.DateUtils

class AssignmentAdapter(
    private val onItemClick: (AssignmentEntity) -> Unit
) : ListAdapter<AssignmentEntity, AssignmentAdapter.AssignmentViewHolder>(AssignmentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssignmentViewHolder {
        val binding = ItemAssignmentBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AssignmentViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: AssignmentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AssignmentViewHolder(
        private val binding: ItemAssignmentBinding,
        private val onItemClick: (AssignmentEntity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(assignment: AssignmentEntity) {
            binding.root.setOnClickListener { onItemClick(assignment) }

            binding.tvAssignmentTitle.text = assignment.title
            binding.tvCourseName.text = "${assignment.courseName} • ${assignment.courseCode}"
            binding.tvDeadline.text = DateUtils.formatDisplayDate(assignment.deadline)

            // Status Badge
            val status = SubmissionStatus.fromString(assignment.submissionStatus)
            val isOverdue = DateUtils.isOverdue(assignment.deadline) && status != SubmissionStatus.SUBMITTED
            val isDueToday = DateUtils.isDueToday(assignment.deadline) && status != SubmissionStatus.SUBMITTED
            
            val displayStatus = when {
                status == SubmissionStatus.SUBMITTED -> SubmissionStatus.SUBMITTED
                isOverdue -> SubmissionStatus.OVERDUE
                isDueToday -> SubmissionStatus.DUE_TODAY
                else -> SubmissionStatus.NOT_SUBMITTED
            }

            binding.tvStatusBadge.text = displayStatus.name.replace("_", " ")

            val statusColor = when (displayStatus) {
                SubmissionStatus.SUBMITTED -> Color.parseColor("#1E8E3E") // Success Green
                SubmissionStatus.NOT_SUBMITTED -> Color.parseColor("#5F6368") // Grey
                SubmissionStatus.OVERDUE -> Color.parseColor("#D93025") // Error Red
                SubmissionStatus.DUE_TODAY -> Color.parseColor("#F9AB00") // Warning Yellow
            }

            binding.viewStatusDot.setBackgroundColor(statusColor)
            binding.tvStatusBadge.setTextColor(statusColor)
            // Lighter version of the color for badge background
            val badgeBgColor = Color.argb(
                25, // ~10% opacity
                Color.red(statusColor),
                Color.green(statusColor),
                Color.blue(statusColor)
            )
            binding.cardStatusBadge.setCardBackgroundColor(badgeBgColor)

            // Deadline Label (e.g., "In 3 days", "Today")
            binding.tvDeadlineLabel.text = DateUtils.formatDeadlineLabel(assignment.deadline)
            binding.tvDeadlineLabel.setTextColor(statusColor)
        }
    }

    class AssignmentDiffCallback : DiffUtil.ItemCallback<AssignmentEntity>() {
        override fun areItemsTheSame(oldItem: AssignmentEntity, newItem: AssignmentEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AssignmentEntity, newItem: AssignmentEntity): Boolean {
            return oldItem == newItem
        }
    }
}
