package com.example.studentmanagement.faculty

import android.content.Intent
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FacultyStudentsActivity : AppCompatActivity() {

    private lateinit var adapter: FacultyStudentAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var recyclerView: RecyclerView

    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faculty_students)

        sessionManager = SessionManager(this)
        
        if (sessionManager.getRole() != "FACULTY") {
            finish()
            return
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        progressBar = findViewById(R.id.progressBar)
        tvEmpty = findViewById(R.id.tvEmpty)
        recyclerView = findViewById(R.id.recyclerViewStudents)

        setupRecyclerView()
        loadMyStudents()
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = FacultyStudentAdapter { student ->
            // On click, maybe navigate to their details or assignments.
            Snackbar.make(recyclerView, "Selected ${student.studentName}", Snackbar.LENGTH_SHORT).show()
        }
        recyclerView.adapter = adapter
    }

    private fun loadMyStudents() {
        progressBar.visibility = View.VISIBLE
        tvEmpty.visibility = View.GONE
        recyclerView.visibility = View.GONE

        val facultyUid = sessionManager.getUid()
        if (facultyUid.isEmpty()) return

        lifecycleScope.launch {
            try {
                // 1. Get courses assigned to this faculty
                val coursesSnapshot = firestore.collection("courses")
                    .whereEqualTo("facultyUid", facultyUid)
                    .get().await()

                if (coursesSnapshot.isEmpty) {
                    progressBar.visibility = View.GONE
                    tvEmpty.visibility = View.VISIBLE
                    tvEmpty.text = "You are not assigned to any courses."
                    return@launch
                }

                val courseMap = coursesSnapshot.documents.associate { doc ->
                    doc.id to Pair(doc.getString("name") ?: "", doc.getString("code") ?: "")
                }

                // 2. Get enrollments for these courses
                val studentsList = mutableListOf<FacultyStudentItem>()

                for ((courseId, courseDetails) in courseMap) {
                    val enrollmentsSnapshot = firestore.collection("course_enrollments")
                        .whereEqualTo("courseId", courseId)
                        .get().await()

                    for (enrollDoc in enrollmentsSnapshot.documents) {
                        val studentUid = enrollDoc.getString("studentUid") ?: continue

                        // Fetch student details from users collection
                        val userDoc = firestore.collection("users").document(studentUid).get().await()
                        
                        if (userDoc.exists() && userDoc.getString("role") == "STUDENT") {
                            studentsList.add(
                                FacultyStudentItem(
                                    studentUid = studentUid,
                                    studentName = userDoc.getString("name") ?: "Unknown Student",
                                    studentEmail = userDoc.getString("email") ?: "",
                                    courseId = courseId,
                                    courseName = courseDetails.first,
                                    courseCode = courseDetails.second
                                )
                            )
                        }
                    }
                }

                progressBar.visibility = View.GONE
                
                if (studentsList.isEmpty()) {
                    tvEmpty.visibility = View.VISIBLE
                    tvEmpty.text = "No students are currently enrolled in your courses."
                } else {
                    recyclerView.visibility = View.VISIBLE
                    // Sort by Course, then Name
                    adapter.setStudents(studentsList.sortedWith(compareBy({ it.courseName }, { it.studentName })))
                }

            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                tvEmpty.visibility = View.VISIBLE
                tvEmpty.text = "Failed to load students: ${e.message}"
            }
        }
    }
}
