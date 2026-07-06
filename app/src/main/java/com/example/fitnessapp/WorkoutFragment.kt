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
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class WorkoutFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

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
        val rvCategories = view.findViewById<RecyclerView>(R.id.rvCategories)
        
        val etManualWorkout = view.findViewById<EditText>(R.id.etManualWorkout)
        val btnLogManual = view.findViewById<Button>(R.id.btnLogManualWorkout)

        val categories = listOf(
            "Strength", "Cardio", "Yoga", "HIIT", 
            "Running", "Walking", "Cycling", "Home Workouts",
            "Stretching", "Beginner", "Intermediate", "Advanced"
        )
        rvCategories.adapter = CategoryAdapter(categories)

        btnGenerate.setOnClickListener {
            val weight = etWeight.text.toString()
            val height = etHeight.text.toString()
            
            if (weight.isNotEmpty() && height.isNotEmpty()) {
                val suggestion = generateAISuggestion(weight.toDoubleOrNull() ?: 0.0, height.toDoubleOrNull() ?: 0.0)
                tvResult.text = suggestion
                saveWorkoutToFirestore(weight, height, "AI_COACH", suggestion)
            } else {
                Toast.makeText(context, "Please enter height and weight", Toast.LENGTH_SHORT).show()
            }
        }

        btnLogManual.setOnClickListener {
            val workout = etManualWorkout.text.toString().trim()
            if (workout.isNotEmpty()) {
                saveLastWorkout(workout)
                Toast.makeText(context, "Workout logged! Check Food tab for nutrition.", Toast.LENGTH_LONG).show()
                etManualWorkout.text.clear()
            } else {
                Toast.makeText(context, "Please enter what you did", Toast.LENGTH_SHORT).show()
            }
        }
        
        return view
    }

    private fun saveLastWorkout(category: String) {
        if (userId == null) return
        db.collection("users").document(userId).update("lastWorkoutCategory", category)
    }

    private fun generateAISuggestion(weight: Double, height: Double): String {
        val bmi = weight / ((height / 100) * (height / 100))
        return when {
            bmi < 18.5 -> "Focus on Strength training and high protein intake to build lean muscle."
            bmi < 25.0 -> "Maintain your balance with a mix of HIIT and Cardio twice a week."
            else -> "Prioritize Cardio and HIIT to burn calories, supplemented by Walking and Yoga."
        }
    }

    private fun saveWorkoutToFirestore(weight: String, height: String, type: String, plan: String) {
        if (userId == null) return
        val workoutData = mapOf(
            "weight" to weight,
            "height" to height,
            "type" to type,
            "plan" to plan,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("workouts").document(userId).collection("entries").add(workoutData)
            .addOnSuccessListener {
                Toast.makeText(context, "Plan synced with Firestore!", Toast.LENGTH_SHORT).show()
            }
    }

    inner class CategoryAdapter(private val items: List<String>) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {
        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val tv: TextView = v.findViewById(R.id.tvCategoryName)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = 
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_workout_category, parent, false))
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val category = items[position]
            holder.tv.text = category
            holder.itemView.setOnClickListener {
                saveLastWorkout(category)
                val intent = android.content.Intent(context, WorkoutVideosActivity::class.java)
                intent.putExtra("CATEGORY", category)
                context?.startActivity(intent)
            }
        }

        override fun getItemCount() = items.size
    }
}
