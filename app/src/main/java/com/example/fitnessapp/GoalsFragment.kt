package com.example.fitnessapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GoalsFragment : Fragment() {

    private lateinit var progressBar: ProgressBar
    private lateinit var tvPercent: TextView
    private val checkBoxes = mutableListOf<CheckBox>()
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_goals, container, false)

        progressBar = view.findViewById(R.id.pbGoals)
        tvPercent = view.findViewById(R.id.tvProgressPercent)

        val ids = listOf(R.id.cbMon, R.id.cbTue, R.id.cbWed, R.id.cbThu, R.id.cbFri, R.id.cbSat)
        for (id in ids) {
            val cb = view.findViewById<CheckBox>(id)
            checkBoxes.add(cb)
            cb.setOnCheckedChangeListener { _, _ -> 
                updateProgress()
                saveGoalsToFirestore()
            }
        }

        loadGoalsFromFirestore()
        updateProgress()
        return view
    }

    private fun loadGoalsFromFirestore() {
        if (userId == null) return
        db.collection("goals").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val checkedIndices = document.get("checkedIndices") as? List<Long>
                    checkedIndices?.forEach { index ->
                        if (index.toInt() in checkBoxes.indices) {
                            checkBoxes[index.toInt()].isChecked = true
                        }
                    }
                    updateProgress()
                }
            }
    }

    private fun saveGoalsToFirestore() {
        if (userId == null) return
        val checkedIndices = checkBoxes.mapIndexedNotNull { index, cb -> if (cb.isChecked) index else null }
        val data = mapOf(
            "checkedIndices" to checkedIndices,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("goals").document(userId).set(data)
            .addOnFailureListener { e ->
                context?.let { Toast.makeText(it, "Failed to sync goals: ${e.message}", Toast.LENGTH_SHORT).show() }
            }
    }

    private fun updateProgress() {
        val completed = checkBoxes.count { it.isChecked }
        progressBar.progress = completed
        val percent = if (checkBoxes.isNotEmpty()) (completed * 100) / checkBoxes.size else 0
        tvPercent.text = getString(R.string.progress_completed, percent)
    }
}
