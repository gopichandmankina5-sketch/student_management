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
import com.example.studentmanagement.model.AttendanceStatus
import com.example.studentmanagement.repository.AttendanceRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class StudentAttendanceActivity : AppCompatActivity() {

    private lateinit var adapter: StudentAttendanceAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var recyclerView: RecyclerView

    private val firestore = FirebaseFirestore.getInstance()
    private val attendanceRepository = AttendanceRepository()
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_attendance)

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
        recyclerView = findViewById(R.id.recyclerViewAttendance)

        setupRecyclerView()
        loadAttendance()
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = StudentAttendanceAdapter()
        recyclerView.adapter = adapter
    }

    private fun loadAttendance() {
        progressBar.visibility = View.VISIBLE
        tvEmpty.visibility = View.GONE
        recyclerView.visibility = View.GONE

        val studentUid = sessionManager.getUid()
        if (studentUid.isEmpty()) return

        lifecycleScope.launch {
            try {
                // 1. Get courses the student is enrolled in
                val enrollmentsSnapshot = firestore.collection("course_enrollments")
                    .whereEqualTo("studentUid", studentUid)
                    .get().await()

                if (enrollmentsSnapshot.isEmpty) {
                    progressBar.visibility = View.GONE
                    tvEmpty.visibility = View.VISIBLE
                    tvEmpty.text = "You are not enrolled in any courses."
                    return@launch
                }

                val courseDetailsMap = enrollmentsSnapshot.documents.mapNotNull { doc ->
                    val cId = doc.getString("courseId")
                    if (cId.isNullOrEmpty()) null else {
                        val cName = doc.getString("courseName") ?: "Unknown"
                        val cCode = doc.getString("courseCode") ?: ""
                        cId to Pair(cName, cCode)
                    }
                }.toMap()

                // 2. Get attendance records
                when (val result = attendanceRepository.getAttendanceForStudent(studentUid)) {
                    is AttendanceRepository.Result.Success -> {
                        val records = result.data
                        
                        // Group by course
                        val groupedByCourse = records.groupBy { it.courseId }
                        val statsList = mutableListOf<StudentAttendanceStat>()

                        for ((courseId, details) in courseDetailsMap) {
                            val courseRecords = groupedByCourse[courseId] ?: emptyList()
                            val presentCount = courseRecords.count { it.status == AttendanceStatus.PRESENT.name }
                            val absentCount = courseRecords.count { it.status == AttendanceStatus.ABSENT.name }

                            statsList.add(
                                StudentAttendanceStat(
                                    courseId = courseId,
                                    courseName = details.first,
                                    courseCode = details.second,
                                    presentCount = presentCount,
                                    absentCount = absentCount
                                )
                            )
                        }

                        progressBar.visibility = View.GONE
                        
                        if (statsList.isEmpty()) {
                            tvEmpty.visibility = View.VISIBLE
                        } else {
                            recyclerView.visibility = View.VISIBLE
                            adapter.setStats(statsList.sortedBy { it.courseName })
                        }
                    }
                    is AttendanceRepository.Result.Error -> {
                        progressBar.visibility = View.GONE
                        tvEmpty.visibility = View.VISIBLE
                        tvEmpty.text = "Error: ${result.message}"
                    }
                }

            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                tvEmpty.visibility = View.VISIBLE
                tvEmpty.text = "Failed to load attendance: ${e.message}"
            }
        }
    }
}
