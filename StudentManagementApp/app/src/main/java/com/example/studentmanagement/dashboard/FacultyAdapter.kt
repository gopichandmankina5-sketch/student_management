package com.example.studentmanagement.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studentmanagement.R

/** Data class for a faculty member displayed in the Admin faculty list. */
data class FacultyItem(
    val uid: String,
    val name: String,
    val email: String,
    val department: String
)

class FacultyAdapter(
    private val onFacultyClick: (FacultyItem) -> Unit
) : RecyclerView.Adapter<FacultyAdapter.FacultyViewHolder>() {

    private val items = mutableListOf<FacultyItem>()

    fun setFaculty(newItems: List<FacultyItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FacultyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_faculty, parent, false)
        return FacultyViewHolder(view)
    }

    override fun onBindViewHolder(holder: FacultyViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
        holder.itemView.setOnClickListener { onFacultyClick(item) }
    }

    override fun getItemCount(): Int = items.size

    class FacultyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvInitials: TextView = itemView.findViewById(R.id.tvFacultyInitials)
        private val tvName: TextView = itemView.findViewById(R.id.tvFacultyName)
        private val tvEmail: TextView = itemView.findViewById(R.id.tvFacultyEmail)
        private val tvDept: TextView = itemView.findViewById(R.id.tvFacultyDepartment)

        fun bind(item: FacultyItem) {
            val initials = item.name
                .split(" ")
                .mapNotNull { it.firstOrNull()?.toString() }
                .take(2)
                .joinToString("")
                .uppercase()
            tvInitials.text = initials.ifEmpty { "F" }
            tvName.text = item.name
            tvEmail.text = item.email
            tvDept.text = item.department.ifEmpty { "No department set" }
        }
    }
}
