package com.example.studentmanagement.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studentmanagement.R

/**
 * Adapter for Admin Manage Courses screen.
 */
class CourseAdapter(private val onCourseClick: (CourseItem) -> Unit) :
    RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    private val courses = mutableListOf<CourseItem>()

    fun setCourses(newCourses: List<CourseItem>) {
        courses.clear()
        courses.addAll(newCourses)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = courses[position]
        holder.bind(course)
        holder.itemView.setOnClickListener { onCourseClick(course) }
    }

    override fun getItemCount(): Int = courses.size

    class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCourseName: TextView = itemView.findViewById(R.id.tvCourseName)
        private val tvCourseCode: TextView = itemView.findViewById(R.id.tvCourseCode)
        private val tvCourseFaculty: TextView = itemView.findViewById(R.id.tvCourseFaculty)

        fun bind(course: CourseItem) {
            tvCourseName.text = course.name
            tvCourseCode.text = "${course.code} | ${course.department}"
            
            if (course.facultyName.isNotEmpty()) {
                tvCourseFaculty.text = "Assigned to: ${course.facultyName}"
                tvCourseFaculty.setTextColor(itemView.context.getColor(R.color.colorPrimary))
            } else {
                tvCourseFaculty.text = "Not assigned"
                tvCourseFaculty.setTextColor(itemView.context.getColor(R.color.colorTextSecondary))
            }
        }
    }
}

/** Data class for the Admin course list view */
data class CourseItem(
    val id: String = "",
    val name: String = "",
    val code: String = "",
    val department: String = "",
    val facultyUid: String = "",
    val facultyName: String = ""
)
