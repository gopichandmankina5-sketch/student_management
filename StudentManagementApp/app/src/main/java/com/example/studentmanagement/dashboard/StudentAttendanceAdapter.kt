package com.example.studentmanagement.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studentmanagement.R

data class StudentAttendanceStat(
    val courseId: String,
    val courseName: String,
    val courseCode: String,
    val presentCount: Int,
    val absentCount: Int
) {
    val total: Int get() = presentCount + absentCount
    val percentage: Int get() = if (total > 0) (presentCount * 100) / total else 0
}

class StudentAttendanceAdapter : RecyclerView.Adapter<StudentAttendanceAdapter.ViewHolder>() {

    private var stats = listOf<StudentAttendanceStat>()

    fun setStats(newStats: List<StudentAttendanceStat>) {
        stats = newStats
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student_attendance, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(stats[position])
    }

    override fun getItemCount(): Int = stats.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCourseName: TextView = itemView.findViewById(R.id.tvCourseName)
        private val tvCourseCode: TextView = itemView.findViewById(R.id.tvCourseCode)
        private val tvPresentCount: TextView = itemView.findViewById(R.id.tvPresentCount)
        private val tvAbsentCount: TextView = itemView.findViewById(R.id.tvAbsentCount)
        private val tvPercentage: TextView = itemView.findViewById(R.id.tvPercentage)

        fun bind(stat: StudentAttendanceStat) {
            tvCourseName.text = stat.courseName
            tvCourseCode.text = stat.courseCode
            tvPresentCount.text = stat.presentCount.toString()
            tvAbsentCount.text = stat.absentCount.toString()
            tvPercentage.text = "${stat.percentage}%"
        }
    }
}
