package com.example.studentmanagement.dashboard

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studentmanagement.R
import com.example.studentmanagement.repository.StudentRepository
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class ManageStudentsActivity : AppCompatActivity() {

    private lateinit var repository: StudentRepository
    private lateinit var adapter: StudentAdapter
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_students)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        repository = StudentRepository(this)
        progressBar = findViewById(R.id.progressBar)

        setupRecyclerView()
        loadStudents()
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewStudents)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = StudentAdapter { student ->
            Snackbar.make(findViewById(android.R.id.content), "Selected: ${student.name}", Snackbar.LENGTH_SHORT).show()
        }
        recyclerView.adapter = adapter
    }

    private fun loadStudents() {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            when (val result = repository.getAllStudents()) {
                is StudentRepository.RepositoryResult.Success -> {
                    progressBar.visibility = View.GONE
                    adapter.setStudents(result.data)
                }
                is StudentRepository.RepositoryResult.Error -> {
                    progressBar.visibility = View.GONE
                    Snackbar.make(findViewById(android.R.id.content), result.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }
}
