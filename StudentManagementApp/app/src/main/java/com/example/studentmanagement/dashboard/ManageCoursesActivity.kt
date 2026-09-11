package com.example.studentmanagement.dashboard

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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ManageCoursesActivity : AppCompatActivity() {

    private lateinit var adapter: CourseAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var etSearch: EditText
    
    private val firestore = FirebaseFirestore.getInstance()
    private var allCourses = listOf<CourseItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_courses)

        // Security check
        val sessionManager = SessionManager(this)
        if (sessionManager.getRole() != "ADMIN") {
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
        
        findViewById<FloatingActionButton>(R.id.fabAddCourse).setOnClickListener {
            val intent = Intent(this, CourseDetailActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadCourses()
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewCourses)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = CourseAdapter { course ->
            val intent = Intent(this, CourseDetailActivity::class.java).apply {
                putExtra("COURSE_ID", course.id)
            }
            startActivity(intent)
        }
        recyclerView.adapter = adapter
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterCourses(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadCourses() {
        progressBar.visibility = View.VISIBLE
        tvEmpty.visibility = View.GONE
        
        lifecycleScope.launch {
            try {
                val snapshot = firestore.collection("courses").get().await()
                allCourses = snapshot.documents.mapNotNull { doc ->
                    CourseItem(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        code = doc.getString("code") ?: "",
                        department = doc.getString("department") ?: "",
                        facultyUid = doc.getString("facultyUid") ?: "",
                        facultyName = doc.getString("facultyName") ?: ""
                    )
                }
                
                progressBar.visibility = View.GONE
                filterCourses(etSearch.text.toString())
                
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Snackbar.make(findViewById(android.R.id.content), "Failed to load courses: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun filterCourses(query: String) {
        val filtered = if (query.isBlank()) {
            allCourses
        } else {
            val lowerQuery = query.lowercase()
            allCourses.filter {
                it.name.lowercase().contains(lowerQuery) || 
                it.code.lowercase().contains(lowerQuery) ||
                it.facultyName.lowercase().contains(lowerQuery)
            }
        }
        
        adapter.setCourses(filtered)
        tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }
}
