package com.example.fitnessapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

class WorkoutFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_workout, container, false)
        
        val etWeight = view.findViewById<EditText>(R.id.etWeight)
        val etHeight = view.findViewById<EditText>(R.id.etHeight)
        val btnGenerate = view.findViewById<Button>(R.id.btnGeneratePlan)
        val tvResult = view.findViewById<TextView>(R.id.tvPlanResult)
        
        btnGenerate.setOnClickListener {
            val weight = etWeight.text.toString()
            val height = etHeight.text.toString()
            
            if (weight.isNotEmpty() && height.isNotEmpty()) {
                val plan = generateRandomPlan()
                tvResult.text = getString(R.string.recommended_plan, plan)
            } else {
                Toast.makeText(context, R.string.fill_fields, Toast.LENGTH_SHORT).show()
            }
        }
        
        return view
    }

    private fun generateRandomPlan(): String {
        val plans = listOf(
            "30 mins Cardio (Jogging) + 15 mins Bodyweight Core",
            "45 mins Strength Training (Upper Body) + 10 mins HIIT",
            "20 mins Power Yoga + 20 mins Cycling",
            "Full Body Circuit: 3 rounds of Squats, Pushups, Lunges, and Planks",
            "Swimming for 45 mins + Stretching session"
        )
        return plans.random()
    }
}
