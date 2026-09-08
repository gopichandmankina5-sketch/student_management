package com.example.studentmanagement.assignment

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studentmanagement.auth.SessionManager
import com.example.studentmanagement.database.AssignmentEntity
import com.example.studentmanagement.databinding.ActivityAssignmentBinding
import com.example.studentmanagement.model.SubmissionStatus
import com.example.studentmanagement.repository.AssignmentRepository
import com.example.studentmanagement.utils.Constants
import com.example.studentmanagement.utils.DateUtils
import com.google.android.material.chip.Chip
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AssignmentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAssignmentBinding
    private lateinit var repository: AssignmentRepository
    private lateinit var adapter: AssignmentAdapter
    private lateinit var sessionManager: SessionManager

    private var allAssignments: List<AssignmentEntity> = emptyList()
    private var currentFilterId = com.example.studentmanagement.R.id.chipAll

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAssignmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = AssignmentRepository(this)
        sessionManager = SessionManager(this)

        setupToolbar()
        setupRecyclerView()
        setupSearchAndFilter()

        val studentId = sessionManager.getStudentId().toInt()
        loadAssignments(studentId)
        observeAssignments(studentId)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = AssignmentAdapter { assignment ->
            val intent = Intent(this, AssignmentDetailsActivity::class.java).apply {
                putExtra(Constants.EXTRA_ASSIGNMENT_ID, assignment.id)
            }
            startActivity(intent)
        }
        binding.rvAssignments.layoutManager = LinearLayoutManager(this)
        binding.rvAssignments.adapter = adapter
    }

    private fun setupSearchAndFilter() {
        // Search
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFiltersAndSearch()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Filter Chips
        binding.chipGroupFilter.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                currentFilterId = checkedIds.first()
                applyFiltersAndSearch()
            }
        }
        
        binding.btnRetry.setOnClickListener {
            val studentId = sessionManager.getStudentId().toInt()
            loadAssignments(studentId)
        }
    }

    private fun loadAssignments(studentId: Int) {
        binding.progressBar.visibility = View.VISIBLE
        binding.layoutError.visibility = View.GONE
        binding.layoutEmpty.visibility = View.GONE
        binding.rvAssignments.visibility = View.GONE

        lifecycleScope.launch {
            when (val result = repository.loadAssignments(studentId)) {
                is AssignmentRepository.Result.Success -> {
                    // Handled by observer
                }
                is AssignmentRepository.Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.layoutError.visibility = View.VISIBLE
                    binding.tvErrorMessage.text = result.message
                }
            }
        }
    }

    private fun observeAssignments(studentId: Int) {
        lifecycleScope.launch {
            repository.observeAssignments(studentId).collectLatest { assignments ->
                binding.progressBar.visibility = View.GONE
                allAssignments = assignments
                applyFiltersAndSearch()
            }
        }
    }

    private fun applyFiltersAndSearch() {
        val query = binding.etSearch.text.toString().trim().lowercase()
        
        var filteredList = allAssignments

        // Apply Search
        if (query.isNotEmpty()) {
            filteredList = filteredList.filter {
                it.title.lowercase().contains(query) ||
                it.courseName.lowercase().contains(query) ||
                it.courseCode.lowercase().contains(query)
            }
        }

        // Apply Status Filter
        filteredList = filteredList.filter { assignment ->
            val status = SubmissionStatus.fromString(assignment.submissionStatus)
            val isOverdue = DateUtils.isOverdue(assignment.deadline) && status != SubmissionStatus.SUBMITTED
            val isDueToday = DateUtils.isDueToday(assignment.deadline) && status != SubmissionStatus.SUBMITTED
            
            when (currentFilterId) {
                com.example.studentmanagement.R.id.chipAll -> true
                com.example.studentmanagement.R.id.chipUpcoming -> DateUtils.isUpcoming(assignment.deadline) && status != SubmissionStatus.SUBMITTED
                com.example.studentmanagement.R.id.chipDueToday -> isDueToday
                com.example.studentmanagement.R.id.chipSubmitted -> status == SubmissionStatus.SUBMITTED
                com.example.studentmanagement.R.id.chipNotSubmitted -> status == SubmissionStatus.NOT_SUBMITTED && !isOverdue && !isDueToday
                com.example.studentmanagement.R.id.chipOverdue -> isOverdue
                else -> true
            }
        }

        updateUIState(filteredList)
    }

    private fun updateUIState(list: List<AssignmentEntity>) {
        if (list.isEmpty()) {
            binding.rvAssignments.visibility = View.GONE
            binding.layoutEmpty.visibility = View.VISIBLE
        } else {
            binding.rvAssignments.visibility = View.VISIBLE
            binding.layoutEmpty.visibility = View.GONE
            adapter.submitList(list)
        }
    }
}
