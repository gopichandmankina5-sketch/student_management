package com.example.studentmanagement.dashboard

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.studentmanagement.R
import com.example.studentmanagement.auth.SessionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CourseDetailActivity : AppCompatActivity() {

    private lateinit var etCourseName: TextInputEditText
    private lateinit var etCourseCode: TextInputEditText
    private lateinit var etCourseDept: TextInputEditText
    private lateinit var spinnerFaculty: Spinner
    private lateinit var tvCurrentFaculty: TextView
    private lateinit var btnSaveCourse: MaterialButton
    private lateinit var btnDeleteCourse: MaterialButton
    private lateinit var progressBar: ProgressBar

    private val firestore = FirebaseFirestore.getInstance()
    private var courseId: String? = null
    private var facultyList = mutableListOf<FacultyItem>()
    
    // For spinner selection
    data class FacultyItem(val uid: String, val name: String, val email: String) {
        override fun toString(): String = "$name ($email)"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_detail)

        // Security check
        val sessionManager = SessionManager(this)
        if (sessionManager.getRole() != "ADMIN") {
            finish()
            return
        }

        courseId = intent.getStringExtra("COURSE_ID")

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (courseId == null) "New Course" else "Edit Course"
        toolbar.setNavigationOnClickListener { finish() }

        etCourseName = findViewById(R.id.etCourseName)
        etCourseCode = findViewById(R.id.etCourseCode)
        etCourseDept = findViewById(R.id.etCourseDept)
        spinnerFaculty = findViewById(R.id.spinnerFaculty)
        tvCurrentFaculty = findViewById(R.id.tvCurrentFaculty)
        btnSaveCourse = findViewById(R.id.btnSaveCourse)
        btnDeleteCourse = findViewById(R.id.btnDeleteCourse)
        progressBar = findViewById(R.id.progressBar)

        setupButtons()
        loadData()
    }

    private fun setupButtons() {
        btnSaveCourse.setOnClickListener {
            saveCourse()
        }

        btnDeleteCourse.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Course")
                .setMessage("Are you sure you want to delete this course?")
                .setPositiveButton("Delete") { _, _ -> deleteCourse() }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun loadData() {
        progressBar.visibility = View.VISIBLE
        btnSaveCourse.isEnabled = false
        
        lifecycleScope.launch {
            try {
                // 1. Load Faculty for the spinner
                val facultyMap = mutableMapOf<String, FacultyItem>()

                // 1. Get from users collection
                val usersSnapshot = firestore.collection("users")
                    .whereEqualTo("role", "FACULTY")
                    .get().await()

                for (doc in usersSnapshot.documents) {
                    val uid = doc.id
                    val name = doc.getString("name") ?: continue
                    val email = doc.getString("email") ?: ""
                    facultyMap[uid] = FacultyItem(uid, name, email)
                }

                // 2. Get from faculty collection
                val facultyColSnapshot = firestore.collection("faculty").get().await()
                for (doc in facultyColSnapshot.documents) {
                    val uid = doc.id
                    if (!facultyMap.containsKey(uid)) {
                        val name = doc.getString("name") ?: continue
                        val email = doc.getString("email") ?: ""
                        facultyMap[uid] = FacultyItem(uid, name, email)
                    }
                }

                val facultyListSorted = facultyMap.values.toList().sortedBy { it.name }
                facultyList.clear()
                // Add an "Unassigned" option first
                facultyList.add(FacultyItem("", "None / Unassigned", ""))
                facultyList.addAll(facultyListSorted)
                
                val adapter = ArrayAdapter(
                    this@CourseDetailActivity, 
                    android.R.layout.simple_spinner_dropdown_item, 
                    facultyList
                )
                spinnerFaculty.adapter = adapter
                
                // 2. Load Course data if editing
                if (courseId != null) {
                    btnDeleteCourse.visibility = View.VISIBLE
                    val courseDoc = firestore.collection("courses").document(courseId!!).get().await()
                    
                    if (courseDoc.exists()) {
                        etCourseName.setText(courseDoc.getString("name"))
                        etCourseCode.setText(courseDoc.getString("code"))
                        etCourseDept.setText(courseDoc.getString("department"))
                        
                        val assignedFacultyUid = courseDoc.getString("facultyUid") ?: ""
                        val assignedFacultyName = courseDoc.getString("facultyName") ?: ""
                        
                        if (assignedFacultyUid.isNotEmpty()) {
                            tvCurrentFaculty.text = "Currently assigned: $assignedFacultyName"
                            // Select in spinner
                            val index = facultyList.indexOfFirst { it.uid == assignedFacultyUid }
                            if (index >= 0) {
                                spinnerFaculty.setSelection(index)
                            }
                        } else {
                            tvCurrentFaculty.text = "Not assigned"
                        }
                    }
                } else {
                    btnDeleteCourse.visibility = View.GONE
                    tvCurrentFaculty.text = "Select a faculty to assign"
                }
                
                progressBar.visibility = View.GONE
                btnSaveCourse.isEnabled = true
                
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Snackbar.make(findViewById(android.R.id.content), "Failed to load data: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun saveCourse() {
        val name = etCourseName.text.toString().trim()
        val code = etCourseCode.text.toString().trim().uppercase()
        val dept = etCourseDept.text.toString().trim()
        
        if (name.isEmpty() || code.isEmpty() || dept.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content), "Please fill all course details", Snackbar.LENGTH_SHORT).show()
            return
        }
        
        val selectedFaculty = spinnerFaculty.selectedItem as FacultyItem
        
        val courseData = hashMapOf(
            "name" to name,
            "code" to code,
            "department" to dept,
            "facultyUid" to selectedFaculty.uid,
            "facultyName" to (if (selectedFaculty.uid.isEmpty()) "" else selectedFaculty.name)
        )
        
        progressBar.visibility = View.VISIBLE
        btnSaveCourse.isEnabled = false
        
        lifecycleScope.launch {
            try {
                if (courseId == null) {
                    // Create new
                    firestore.collection("courses").add(courseData).await()
                } else {
                    // Update existing
                    firestore.collection("courses").document(courseId!!).update(courseData as Map<String, Any>).await()
                }
                Snackbar.make(findViewById(android.R.id.content), "Course saved successfully", Snackbar.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                btnSaveCourse.isEnabled = true
                Snackbar.make(findViewById(android.R.id.content), "Failed to save: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }
    
    private fun deleteCourse() {
        if (courseId == null) return
        
        progressBar.visibility = View.VISIBLE
        btnSaveCourse.isEnabled = false
        btnDeleteCourse.isEnabled = false
        
        lifecycleScope.launch {
            try {
                firestore.collection("courses").document(courseId!!).delete().await()
                Snackbar.make(findViewById(android.R.id.content), "Course deleted", Snackbar.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                btnSaveCourse.isEnabled = true
                btnDeleteCourse.isEnabled = true
                Snackbar.make(findViewById(android.R.id.content), "Failed to delete: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }
}
