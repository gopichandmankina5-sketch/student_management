package com.example.studentmanagement.assignment

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studentmanagement.R
import com.example.studentmanagement.auth.SessionManager
import com.example.studentmanagement.database.AssignmentEntity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FacultyAssignmentListActivity : AppCompatActivity() {

    private lateinit var adapter: FacultyAssignmentAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var etSearch: EditText
    
    private val firestore = FirebaseFirestore.getInstance()
    private var allAssignments = listOf<AssignmentEntity>()
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faculty_assignment_list)

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
        etSearch = findViewById(R.id.etSearch)

        setupRecyclerView()
        setupSearch()
        
        findViewById<FloatingActionButton>(R.id.fabCreateAssignment).setOnClickListener {
            val intent = Intent(this, CreateAssignmentActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadAssignments()
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewAssignments)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = FacultyAssignmentAdapter { assignment ->
            // Optionally open details
            Snackbar.make(findViewById(android.R.id.content), "Selected: ${assignment.title}", Snackbar.LENGTH_SHORT).show()
        }
        recyclerView.adapter = adapter
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterAssignments(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadAssignments() {
        progressBar.visibility = View.VISIBLE
        tvEmpty.visibility = View.GONE
        
        val uid = sessionManager.getUid()
        if (uid.isEmpty()) return
        
        lifecycleScope.launch {
            try {
                // Query assignments where facultyUid == currentUid
                val snapshot = firestore.collection("assignments")
                    .whereEqualTo("facultyUid", uid)
                    .get().await()
                    
                allAssignments = snapshot.documents.mapNotNull { doc ->
                    AssignmentEntity(
                        id = doc.id,
                        studentUid = doc.getString("studentUid") ?: "",
                        facultyUid = doc.getString("facultyUid") ?: "",
                        courseId = doc.getString("courseId") ?: "",
                        courseName = doc.getString("courseName") ?: "",
                        courseCode = doc.getString("courseCode") ?: "",
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        assignedDate = doc.getString("assignedDate") ?: "",
                        deadline = doc.getString("deadline") ?: "",
                        submissionStatus = doc.getString("submissionStatus") ?: "NOT_SUBMITTED",
                        submittedDate = doc.getString("submittedDate")
                    )
                }
                
                progressBar.visibility = View.GONE
                filterAssignments(etSearch.text.toString())
                
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Snackbar.make(findViewById(android.R.id.content), "Failed to load: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun filterAssignments(query: String) {
        val filtered = if (query.isBlank()) {
            allAssignments
        } else {
            val lowerQuery = query.lowercase()
            allAssignments.filter {
                it.title.lowercase().contains(lowerQuery) || 
                it.courseName.lowercase().contains(lowerQuery) ||
                it.courseCode.lowercase().contains(lowerQuery)
            }
        }
        
        adapter.setAssignments(filtered)
        tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }
}
