package com.example.studentmanagement.assignment

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Spinner
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CreateAssignmentActivity : AppCompatActivity() {

    private lateinit var spinnerCourse: Spinner
    private lateinit var spinnerStudent: Spinner
    private lateinit var etAssignmentTitle: TextInputEditText
    private lateinit var etAssignmentDescription: TextInputEditText
    private lateinit var etAssignmentDeadline: TextInputEditText
    private lateinit var btnCreateAssignment: MaterialButton
    private lateinit var progressBar: ProgressBar

    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var sessionManager: SessionManager
    
    private var coursesList = mutableListOf<CourseItem>()
    private var studentsList = mutableListOf<StudentItem>()

    data class CourseItem(val id: String, val name: String, val code: String) {
        override fun toString(): String = "$name ($code)"
    }
    
    data class StudentItem(val uid: String, val name: String, val email: String) {
        override fun toString(): String = "$name ($email)"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_assignment)

        sessionManager = SessionManager(this)
        if (sessionManager.getRole() != "FACULTY") {
            finish()
            return
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        spinnerCourse = findViewById(R.id.spinnerCourse)
        spinnerStudent = findViewById(R.id.spinnerStudent)
        etAssignmentTitle = findViewById(R.id.etAssignmentTitle)
        etAssignmentDescription = findViewById(R.id.etAssignmentDescription)
        etAssignmentDeadline = findViewById(R.id.etAssignmentDeadline)
        btnCreateAssignment = findViewById(R.id.btnCreateAssignment)
        progressBar = findViewById(R.id.progressBar)

        setupListeners()
        loadCourses()
    }

    private fun setupListeners() {
        spinnerCourse.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0) { // Skip "Select Course"
                    val selectedCourse = coursesList[position]
                    loadStudentsForCourse(selectedCourse.id)
                } else {
                    studentsList.clear()
                    studentsList.add(StudentItem("", "Select Student", ""))
                    updateStudentSpinner()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        btnCreateAssignment.setOnClickListener {
            createAssignment()
        }
    }

    private fun loadCourses() {
        progressBar.visibility = View.VISIBLE
        btnCreateAssignment.isEnabled = false
        
        val uid = sessionManager.getUid()
        
        lifecycleScope.launch {
            try {
                // Only load courses assigned to this faculty
                val snapshot = firestore.collection("courses")
                    .whereEqualTo("facultyUid", uid)
                    .get().await()
                    
                coursesList.clear()
                coursesList.add(CourseItem("", "Select Course", ""))
                
                snapshot.documents.forEach { doc ->
                    coursesList.add(
                        CourseItem(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            code = doc.getString("code") ?: ""
                        )
                    )
                }
                
                val adapter = ArrayAdapter(this@CreateAssignmentActivity, android.R.layout.simple_spinner_dropdown_item, coursesList)
                spinnerCourse.adapter = adapter
                
                progressBar.visibility = View.GONE
                
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Snackbar.make(findViewById(android.R.id.content), "Failed to load courses: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun loadStudentsForCourse(courseId: String) {
        progressBar.visibility = View.VISIBLE
        btnCreateAssignment.isEnabled = false
        
        lifecycleScope.launch {
            try {
                // In a full implementation, we'd query course_enrollments.
                // For now, load all students since enrollments might not be fully seeded yet.
                // NOTE: This assumes Faculty can assign to any student if enrollment isn't enforced strictly yet.
                val snapshot = firestore.collection("users")
                    .whereEqualTo("role", "STUDENT")
                    .get().await()
                    
                studentsList.clear()
                studentsList.add(StudentItem("", "Select Student", ""))
                
                snapshot.documents.forEach { doc ->
                    studentsList.add(
                        StudentItem(
                            uid = doc.getString("uid") ?: doc.id,
                            name = doc.getString("name") ?: "Unknown",
                            email = doc.getString("email") ?: ""
                        )
                    )
                }
                
                updateStudentSpinner()
                progressBar.visibility = View.GONE
                btnCreateAssignment.isEnabled = true
                
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Snackbar.make(findViewById(android.R.id.content), "Failed to load students: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }
    
    private fun updateStudentSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, studentsList)
        spinnerStudent.adapter = adapter
    }

    private fun createAssignment() {
        val selectedCourse = spinnerCourse.selectedItem as? CourseItem
        val selectedStudent = spinnerStudent.selectedItem as? StudentItem
        
        if (selectedCourse == null || selectedCourse.id.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content), "Please select a course", Snackbar.LENGTH_SHORT).show()
            return
        }
        
        if (selectedStudent == null || selectedStudent.uid.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content), "Please select a student", Snackbar.LENGTH_SHORT).show()
            return
        }
        
        val title = etAssignmentTitle.text.toString().trim()
        val desc = etAssignmentDescription.text.toString().trim()
        val deadline = etAssignmentDeadline.text.toString().trim()
        
        if (title.isEmpty() || desc.isEmpty() || deadline.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content), "Please fill all fields", Snackbar.LENGTH_SHORT).show()
            return
        }
        
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        
        val assignmentData = hashMapOf(
            "title" to title,
            "description" to desc,
            "courseId" to selectedCourse.id,
            "courseName" to selectedCourse.name,
            "courseCode" to selectedCourse.code,
            "studentUid" to selectedStudent.uid,
            "facultyUid" to sessionManager.getUid(),
            "assignedDate" to currentDate,
            "deadline" to deadline,
            "submissionStatus" to "NOT_SUBMITTED",
            "submittedDate" to null
        )
        
        progressBar.visibility = View.VISIBLE
        btnCreateAssignment.isEnabled = false
        
        lifecycleScope.launch {
            try {
                firestore.collection("assignments").add(assignmentData).await()
                Snackbar.make(findViewById(android.R.id.content), "Assignment created successfully", Snackbar.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                btnCreateAssignment.isEnabled = true
                Snackbar.make(findViewById(android.R.id.content), "Failed to create: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }
}
