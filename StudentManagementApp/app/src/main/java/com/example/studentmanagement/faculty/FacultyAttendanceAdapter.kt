package com.example.studentmanagement.faculty

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studentmanagement.R
import com.example.studentmanagement.model.AttendanceRecord
import com.example.studentmanagement.model.AttendanceStatus

class FacultyAttendanceAdapter : RecyclerView.Adapter<FacultyAttendanceAdapter.ViewHolder>() {

    private var records = mutableListOf<AttendanceRecord>()

    fun setRecords(newRecords: List<AttendanceRecord>) {
        records.clear()
        records.addAll(newRecords)
        notifyDataSetChanged()
    }

    fun getRecords(): List<AttendanceRecord> = records

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_faculty_attendance, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(records[position], position)
    }

    override fun getItemCount(): Int = records.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvStudentName)
        private val rgStatus: RadioGroup = itemView.findViewById(R.id.rgAttendanceStatus)
        private val rbPresent: RadioButton = itemView.findViewById(R.id.rbPresent)
        private val rbAbsent: RadioButton = itemView.findViewById(R.id.rbAbsent)

        fun bind(record: AttendanceRecord, position: Int) {
            tvName.text = record.studentName

            // Temporarily remove listener to avoid triggering it while setting state
            rgStatus.setOnCheckedChangeListener(null)

            when (record.status) {
                AttendanceStatus.PRESENT.name -> rbPresent.isChecked = true
                AttendanceStatus.ABSENT.name -> rbAbsent.isChecked = true
                else -> rgStatus.clearCheck()
            }

            rgStatus.setOnCheckedChangeListener { _, checkedId ->
                val newStatus = when (checkedId) {
                    R.id.rbPresent -> AttendanceStatus.PRESENT.name
                    R.id.rbAbsent -> AttendanceStatus.ABSENT.name
                    else -> AttendanceStatus.NOT_MARKED.name
                }
                records[position] = records[position].copy(status = newStatus)
            }
        }
    }
}
