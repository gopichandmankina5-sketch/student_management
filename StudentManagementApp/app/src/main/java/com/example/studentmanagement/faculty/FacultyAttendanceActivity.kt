package com.example.studentmanagement.faculty

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studentmanagement.R
import com.example.studentmanagement.auth.SessionManager
import com.example.studentmanagement.dashboard.CourseItem
import com.example.studentmanagement.model.AttendanceRecord
import com.example.studentmanagement.repository.AttendanceRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FacultyAttendanceActivity : AppCompatActivity() {

    private lateinit var spinnerCourse: AutoCompleteTextView
    private lateinit var etDate: TextInputEditText
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnSave: MaterialButton

    private lateinit var adapter: FacultyAttendanceAdapter
    private val firestore = FirebaseFirestore.getInstance()
    private val attendanceRepository = AttendanceRepository()
    private lateinit var sessionManager: SessionManager

    private var coursesList = mutableListOf<CourseItem>()
    private var selectedCourseId: String? = null
    private var selectedDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faculty_attendance)

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
        etDate = findViewById(R.id.etDate)
        progressBar = findViewById(R.id.progressBar)
        tvEmpty = findViewById(R.id.tvEmpty)
        recyclerView = findViewById(R.id.recyclerViewAttendance)
        btnSave = findViewById(R.id.btnSave)

        etDate.setText(selectedDate)
        etDate.setOnClickListener { showDatePicker() }

        setupRecyclerView()
        loadCourses()

        spinnerCourse.setOnItemClickListener { _, _, position, _ ->
            selectedCourseId = coursesList[position].id
            loadAttendanceData()
        }

        btnSave.setOnClickListener { saveAttendance() }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = FacultyAttendanceAdapter()
        recyclerView.adapter = adapter
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            etDate.setText(selectedDate)
            
            if (selectedCourseId != null) {
                loadAttendanceData()
            }
        }
        DatePickerDialog(
            this, dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun loadCourses() {
        val facultyUid = sessionManager.getUid()
        lifecycleScope.launch {
            try {
                val snapshot = firestore.collection("courses")
                    .whereEqualTo("facultyUid", facultyUid)
                    .get().await()

                coursesList.clear()
                for (doc in snapshot.documents) {
                    coursesList.add(
                        CourseItem(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            code = doc.getString("code") ?: ""
                        )
                    )
                }

                val courseNames = coursesList.map { "${it.name} (${it.code})" }
                val arrayAdapter = ArrayAdapter(this@FacultyAttendanceActivity, android.R.layout.simple_dropdown_item_1line, courseNames)
                spinnerCourse.setAdapter(arrayAdapter)

            } catch (e: Exception) {
                Snackbar.make(findViewById(android.R.id.content), "Failed to load courses", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun loadAttendanceData() {
        val courseId = selectedCourseId ?: return
        val facultyUid = sessionManager.getUid()

        progressBar.visibility = View.VISIBLE
        tvEmpty.visibility = View.GONE
        recyclerView.visibility = View.GONE
        btnSave.isEnabled = false

        lifecycleScope.launch {
            try {
                // 1. Get enrolled students
                val enrollmentsSnapshot = firestore.collection("course_enrollments")
                    .whereEqualTo("courseId", courseId)
                    .get().await()

                val studentUids = enrollmentsSnapshot.documents.mapNotNull { it.getString("studentUid") }
                
                if (studentUids.isEmpty()) {
                    progressBar.visibility = View.GONE
                    tvEmpty.visibility = View.VISIBLE
                    tvEmpty.text = "No students enrolled in this course."
                    return@launch
                }

                // Fetch student names
                val studentNames = mutableMapOf<String, String>()
                for (uid in studentUids) {
                    val userDoc = firestore.collection("users").document(uid).get().await()
                    studentNames[uid] = userDoc.getString("name") ?: "Unknown Student"
                }

                // 2. Get existing attendance records for the date
                val existingAttendanceResult = attendanceRepository.getAttendanceForCourseAndDate(courseId, selectedDate)
                val existingRecordsMap = if (existingAttendanceResult is AttendanceRepository.Result.Success) {
                    existingAttendanceResult.data.associateBy { it.studentUid }
                } else {
                    emptyMap()
                }

                // 3. Merge data
                val recordsToDisplay = studentUids.map { uid ->
                    existingRecordsMap[uid] ?: AttendanceRecord(
                        courseId = courseId,
                        studentUid = uid,
                        facultyUid = facultyUid,
                        date = selectedDate,
                        status = "NOT_MARKED",
                        studentName = studentNames[uid] ?: "Unknown Student"
                    )
                }.sortedBy { it.studentName }

                adapter.setRecords(recordsToDisplay)
                
                progressBar.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                btnSave.isEnabled = true

            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                tvEmpty.visibility = View.VISIBLE
                tvEmpty.text = "Error: ${e.message}"
            }
        }
    }

    private fun saveAttendance() {
        val records = adapter.getRecords()
        if (records.isEmpty()) return

        // Verify all are marked? Optional. For now allow partial.
        
        btnSave.isEnabled = false
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            when (val result = attendanceRepository.saveAttendanceBatch(records)) {
                is AttendanceRepository.Result.Success -> {
                    progressBar.visibility = View.GONE
                    btnSave.isEnabled = true
                    Snackbar.make(findViewById(android.R.id.content), "Attendance saved successfully", Snackbar.LENGTH_LONG).show()
                }
                is AttendanceRepository.Result.Error -> {
                    progressBar.visibility = View.GONE
                    btnSave.isEnabled = true
                    Snackbar.make(findViewById(android.R.id.content), "Failed to save: ${result.message}", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }
}
