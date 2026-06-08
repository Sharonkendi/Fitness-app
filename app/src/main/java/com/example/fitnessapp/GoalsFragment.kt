package com.example.fitnessapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment

class GoalsFragment : Fragment() {

    private lateinit var progressBar: ProgressBar
    private lateinit var tvPercent: TextView
    private val checkBoxes = mutableListOf<CheckBox>()

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
            cb.setOnCheckedChangeListener { _, _ -> updateProgress() }
        }

        updateProgress()
        return view
    }

    private fun updateProgress() {
        val completed = checkBoxes.count { it.isChecked }
        progressBar.progress = completed
        val percent = (completed * 100) / 6
        tvPercent.text = getString(R.string.progress_completed, percent)
    }
}
