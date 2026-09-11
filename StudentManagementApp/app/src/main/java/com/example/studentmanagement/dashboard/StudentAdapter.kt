package com.example.studentmanagement.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studentmanagement.R
import com.example.studentmanagement.model.Student

class StudentAdapter(private val onStudentClick: (Student) -> Unit) : RecyclerView.Adapter<StudentAdapter.StudentViewHolder>() {

    private val students = mutableListOf<Student>()

    fun setStudents(newStudents: List<Student>) {
        students.clear()
        students.addAll(newStudents)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_student, parent, false)
        return StudentViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val student = students[position]
        holder.bind(student)
        holder.itemView.setOnClickListener { onStudentClick(student) }
    }

    override fun getItemCount(): Int = students.size

    class StudentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvStudentName: TextView = itemView.findViewById(R.id.tvStudentName)
        private val tvStudentEmail: TextView = itemView.findViewById(R.id.tvStudentEmail)
        private val tvStudentInfo: TextView = itemView.findViewById(R.id.tvStudentInfo)

        fun bind(student: Student) {
            tvStudentName.text = student.name
            tvStudentEmail.text = student.email
            tvStudentInfo.text = "Roll: ${student.rollNumber} | Dept: ${student.department} | Year: ${student.year}"
        }
    }
}
