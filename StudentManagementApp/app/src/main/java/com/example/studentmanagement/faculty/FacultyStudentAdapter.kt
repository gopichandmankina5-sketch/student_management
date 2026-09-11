package com.example.studentmanagement.faculty

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studentmanagement.R

data class FacultyStudentItem(
    val studentUid: String,
    val studentName: String,
    val studentEmail: String,
    val courseId: String,
    val courseName: String,
    val courseCode: String
)

class FacultyStudentAdapter(
    private val onItemClick: (FacultyStudentItem) -> Unit
) : RecyclerView.Adapter<FacultyStudentAdapter.ViewHolder>() {

    private var students = listOf<FacultyStudentItem>()

    fun setStudents(newStudents: List<FacultyStudentItem>) {
        students = newStudents
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_faculty_student, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val student = students[position]
        holder.bind(student)
        holder.itemView.setOnClickListener { onItemClick(student) }
    }

    override fun getItemCount(): Int = students.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvStudentName)
        private val tvEmail: TextView = itemView.findViewById(R.id.tvStudentEmail)
        private val tvCourse: TextView = itemView.findViewById(R.id.tvCourseInfo)

        fun bind(student: FacultyStudentItem) {
            tvName.text = student.studentName
            tvEmail.text = student.studentEmail
            tvCourse.text = "${student.courseName} (${student.courseCode})"
        }
    }
}
