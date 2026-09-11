package com.example.studentmanagement.assignment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studentmanagement.R
import com.example.studentmanagement.database.AssignmentEntity

/**
 * Adapter for Faculty Assignment List screen.
 */
class FacultyAssignmentAdapter(private val onAssignmentClick: (AssignmentEntity) -> Unit) :
    RecyclerView.Adapter<FacultyAssignmentAdapter.ViewHolder>() {

    private val assignments = mutableListOf<AssignmentEntity>()

    fun setAssignments(newAssignments: List<AssignmentEntity>) {
        assignments.clear()
        assignments.addAll(newAssignments)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_faculty_assignment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val assignment = assignments[position]
        holder.bind(assignment)
        holder.itemView.setOnClickListener { onAssignmentClick(assignment) }
    }

    override fun getItemCount(): Int = assignments.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvAssignmentTitle)
        private val tvCourse: TextView = itemView.findViewById(R.id.tvAssignmentCourse)
        private val tvStudent: TextView = itemView.findViewById(R.id.tvAssignmentStudent)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvAssignmentStatus)
        private val tvDeadline: TextView = itemView.findViewById(R.id.tvAssignmentDeadline)

        fun bind(assignment: AssignmentEntity) {
            tvTitle.text = assignment.title
            tvCourse.text = "${assignment.courseName} (${assignment.courseCode})"
            tvStudent.text = "Student UID: ${assignment.studentUid}"
            tvStatus.text = "Status: ${assignment.submissionStatus}"
            tvDeadline.text = "Due: ${assignment.deadline}"
        }
    }
}
