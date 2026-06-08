package com.example.fitnessapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView

class HomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val tvWelcome = view.findViewById<TextView>(R.id.tvWelcome)
        
        // Load name from SharedPreferences
        val sharedPref = activity?.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val name = sharedPref?.getString("USER_NAME", "Fitness Enthusiast")
        tvWelcome.text = getString(R.string.welcome_message, name)

        view.findViewById<MaterialCardView>(R.id.cardWaterTracker).setOnClickListener {
            startActivity(Intent(activity, WaterTrackerActivity::class.java))
        }

        view.findViewById<MaterialCardView>(R.id.cardBMICalculator).setOnClickListener {
            startActivity(Intent(activity, BMIActivity::class.java))
        }

        view.findViewById<MaterialCardView>(R.id.cardSleepTracker).setOnClickListener {
            startActivity(Intent(activity, SleepTrackerActivity::class.java))
        }

        view.findViewById<MaterialCardView>(R.id.cardHistory).setOnClickListener {
            startActivity(Intent(activity, HistoryActivity::class.java))
        }

        return view
    }
}
