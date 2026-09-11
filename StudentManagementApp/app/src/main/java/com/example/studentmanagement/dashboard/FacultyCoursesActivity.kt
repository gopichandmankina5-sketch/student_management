package com.example.studentmanagement.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studentmanagement.R
import com.example.studentmanagement.auth.SessionManager
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Faculty-only screen: lists courses assigned to the logged-in faculty member.
 * Reads from Firestore `courses` collection where `facultyUid == currentUid`.
 */
class FacultyCoursesActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var recyclerView: RecyclerView
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faculty_courses)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        progressBar = findViewById(R.id.progressBar)
        tvEmpty = findViewById(R.id.tvEmpty)
        recyclerView = findViewById(R.id.recyclerViewCourses)
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadCourses()
    }

    private fun loadCourses() {
        progressBar.visibility = View.VISIBLE
        tvEmpty.visibility = View.GONE

        val uid = auth.currentUser?.uid
            ?: SessionManager(this).getUid().takeIf { it.isNotEmpty() }

        if (uid == null) {
            progressBar.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
            tvEmpty.text = "Not authenticated. Please log in again."
            return
        }

        lifecycleScope.launch {
            try {
                val snapshot = firestore.collection("courses")
                    .whereEqualTo("facultyUid", uid)
                    .get()
                    .await()

                progressBar.visibility = View.GONE

                val courses = snapshot.documents.mapNotNull { doc ->
                    val name = doc.getString("name") ?: return@mapNotNull null
                    val code = doc.getString("code") ?: ""
                    val dept = doc.getString("department") ?: ""
                    CourseItem(id = doc.id, name = name, code = code, department = dept)
                }

                if (courses.isEmpty()) {
                    tvEmpty.visibility = View.VISIBLE
                } else {
                    tvEmpty.visibility = View.GONE
                    recyclerView.adapter = CourseAdapter(courses)
                }
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Failed to load courses: ${e.localizedMessage}",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Inline Course data class and Adapter (simple, self-contained)
    // ─────────────────────────────────────────────────────────────

    data class CourseItem(val id: String, val name: String, val code: String, val department: String)

    inner class CourseAdapter(private val items: List<CourseItem>) :
        RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

        inner class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvName: TextView = itemView.findViewById(R.id.tvCourseName)
            val tvCode: TextView = itemView.findViewById(R.id.tvCourseCode)
            val tvDept: TextView = itemView.findViewById(R.id.tvCourseDept)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_faculty_course, parent, false)
            return CourseViewHolder(view)
        }

        override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
            val item = items[position]
            holder.tvName.text = item.name
            holder.tvCode.text = item.code
            holder.tvDept.text = item.department
        }

        override fun getItemCount(): Int = items.size
    }
}
