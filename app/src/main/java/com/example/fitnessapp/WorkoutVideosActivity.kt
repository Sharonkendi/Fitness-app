package com.example.fitnessapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class WorkoutVideosActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_videos)

        val category = intent.getStringExtra("CATEGORY") ?: "Workout"
        findViewById<TextView>(R.id.tvWorkoutTitle).text = "$category Library"

        val rv = findViewById<RecyclerView>(R.id.rvExerciseList)
        rv.layoutManager = LinearLayoutManager(this)
        
        val exercises = when(category) {
            "Strength" -> listOf("Pushups - 3 sets of 15", "Squats - 4 sets of 12", "Deadlifts - 3 sets of 8", "Bench Press - 3 sets of 10")
            "Cardio" -> listOf("Jumping Jacks - 5 mins", "Burpees - 3 sets of 10", "Mountain Climbers - 3 sets of 30s")
            "Yoga" -> listOf("Downward Dog - 1 min", "Warrior Pose - 1 min each side", "Sun Salutation - 5 rounds")
            "HIIT" -> listOf("Sprints - 30s on/30s off", "Box Jumps - 3 sets of 15", "Kettlebell Swings - 3 sets of 20")
            else -> listOf("Warm-up: Jog in place", "Dynamic Stretching", "Standard Exercise A", "Standard Exercise B")
        }
        
        rv.adapter = ExerciseAdapter(exercises) { exerciseName ->
            openYouTube(exerciseName)
        }

        findViewById<View>(R.id.videoPlaceholder).setOnClickListener {
            openYouTube("$category full workout")
        }
    }

    private fun openYouTube(query: String) {
        val intent = Intent(Intent.ACTION_SEARCH)
        intent.setPackage("com.google.android.youtube")
        intent.putExtra("query", query + " workout")
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            // Fallback: search in browser
            val webIntent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://www.youtube.com/results?search_query=" + query.replace(" ", "+") + "+workout"))
            startActivity(webIntent)
        }
    }

    class ExerciseAdapter(private val items: List<String>, private val onItemClick: (String) -> Unit) : RecyclerView.Adapter<ExerciseAdapter.ViewHolder>() {
        class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val tv: TextView = v.findViewById(android.R.id.text1)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
            return ViewHolder(v)
        }
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.tv.text = item
            holder.itemView.setOnClickListener { onItemClick(item) }
        }
        override fun getItemCount() = items.size
    }
}
