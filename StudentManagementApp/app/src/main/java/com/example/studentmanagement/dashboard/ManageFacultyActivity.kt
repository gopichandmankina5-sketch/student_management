package com.example.studentmanagement.dashboard

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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Admin-only screen: lists all faculty members from Firestore.
 * Provides a FAB to create new faculty accounts via [CreateFacultyActivity].
 */
class ManageFacultyActivity : AppCompatActivity() {

    private lateinit var adapter: FacultyAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_faculty)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        progressBar = findViewById(R.id.progressBar)
        tvEmpty = findViewById(R.id.tvEmpty)

        setupRecyclerView()

        findViewById<FloatingActionButton>(R.id.fabAddFaculty).setOnClickListener {
            startActivity(Intent(this, CreateFacultyActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadFaculty()
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewFaculty)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = FacultyAdapter { faculty ->
            Snackbar.make(
                findViewById(android.R.id.content),
                "${faculty.name} — ${faculty.email}",
                Snackbar.LENGTH_SHORT
            ).show()
        }
        recyclerView.adapter = adapter
    }

    private fun loadFaculty() {
        progressBar.visibility = View.VISIBLE
        tvEmpty.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val facultyMap = mutableMapOf<String, FacultyItem>()

                // 1. Get from users collection
                val usersSnapshot = firestore.collection("users")
                    .whereEqualTo("role", "FACULTY")
                    .get()
                    .await()

                for (doc in usersSnapshot.documents) {
                    val uid = doc.id
                    val name = doc.getString("name") ?: continue
                    val email = doc.getString("email") ?: ""
                    val department = doc.getString("department") ?: ""
                    facultyMap[uid] = FacultyItem(uid, name, email, department)
                }

                // 2. Get from faculty collection
                val facultySnapshot = firestore.collection("faculty").get().await()
                for (doc in facultySnapshot.documents) {
                    val uid = doc.id
                    if (!facultyMap.containsKey(uid)) {
                        val name = doc.getString("name") ?: continue
                        val email = doc.getString("email") ?: ""
                        val department = doc.getString("department") ?: ""
                        facultyMap[uid] = FacultyItem(uid, name, email, department)
                    }
                }

                val facultyList = facultyMap.values.toList().sortedBy { it.name }

                progressBar.visibility = View.GONE

                if (facultyList.isEmpty()) {
                    tvEmpty.visibility = View.VISIBLE
                } else {
                    tvEmpty.visibility = View.GONE
                    adapter.setFaculty(facultyList)
                }
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Failed to load faculty: ${e.localizedMessage}",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }
}
