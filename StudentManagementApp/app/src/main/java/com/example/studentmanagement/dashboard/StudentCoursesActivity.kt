package com.example.studentmanagement.dashboard

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studentmanagement.R
import com.example.studentmanagement.auth.SessionManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class StudentCoursesActivity : AppCompatActivity() {

    private lateinit var adapter: CourseAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_courses)

        sessionManager = SessionManager(this)
        if (sessionManager.getRole() != "STUDENT") {
            finish()
            return
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        progressBar = findViewById(R.id.progressBar)
        tvEmpty = findViewById(R.id.tvEmpty)

        setupRecyclerView()
        loadCourses()
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewCourses)
        recyclerView.layoutManager = LinearLayoutManager(this)
        // We can reuse the CourseAdapter we created for Admin Course Management,
        // since it expects a list of CourseItem and binds cleanly.
        adapter = CourseAdapter { course ->
            Snackbar.make(findViewById(android.R.id.content), "Selected: ${course.name}", Snackbar.LENGTH_SHORT).show()
        }
        recyclerView.adapter = adapter
    }

    private fun loadCourses() {
        progressBar.visibility = View.VISIBLE
        tvEmpty.visibility = View.GONE
        
        val uid = sessionManager.getUid()
        if (uid.isEmpty()) return
        
        lifecycleScope.launch {
            try {
                // Since there is no actual course_enrollments populated right now (Member 2 stub),
                // we'll attempt a query and handle empty state gracefully.
                // In future: query course_enrollments where studentUid == uid.
                
                // For a fallback implementation if no enrollments collection is available yet:
                // We'll show an empty list indicating the student isn't enrolled.
                val snapshot = firestore.collection("course_enrollments")
                    .whereEqualTo("studentUid", uid)
                    .get().await()
                    
                val courses = snapshot.documents.mapNotNull { doc ->
                    CourseItem(
                        id = doc.getString("courseId") ?: "",
                        name = doc.getString("courseName") ?: "",
                        code = doc.getString("courseCode") ?: "",
                        department = "",
                        facultyUid = "",
                        facultyName = ""
                    )
                }
                
                progressBar.visibility = View.GONE
                
                if (courses.isEmpty()) {
                    tvEmpty.visibility = View.VISIBLE
                } else {
                    adapter.setCourses(courses)
                }
                
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Snackbar.make(findViewById(android.R.id.content), "Failed to load courses: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }
}
