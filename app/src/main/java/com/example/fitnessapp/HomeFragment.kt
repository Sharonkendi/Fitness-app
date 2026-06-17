package com.example.fitnessapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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

        view.findViewById<MaterialCardView>(R.id.cardDailyGoals).setOnClickListener {
            showStepsInputDialog()
        }

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

    private fun showStepsInputDialog() {
        val context = context ?: return
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Enter Daily Steps")

        val input = android.widget.EditText(context)
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        builder.setView(input)

        builder.setPositiveButton("Save") { _, _ ->
            val steps = input.text.toString()
            if (steps.isNotEmpty()) {
                saveStepsToFirestore(steps)
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun saveStepsToFirestore(steps: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        val data = mapOf(
            "steps" to steps,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("steps_history").document(userId).collection("entries")
            .add(data)
            .addOnSuccessListener {
                Toast.makeText(context, "Steps saved!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to save steps: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
