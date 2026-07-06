package com.example.fitnessapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GoalsActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goals)

        val pbOverall = findViewById<ProgressBar>(R.id.pbOverall)
        val tvProgress = findViewById<TextView>(R.id.tvProgressText)
        val rvBadges = findViewById<RecyclerView>(R.id.rvBadges)
        val btnSetGoal = findViewById<Button>(R.id.btnSetNewGoal)

        loadProgress(pbOverall, tvProgress)
        loadBadges(rvBadges)

        btnSetGoal.setOnClickListener {
            Toast.makeText(this, "Goal setting coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadProgress(pb: ProgressBar, tv: TextView) {
        if (userId == null) return
        db.collection("goals").document(userId).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val total = doc.getLong("totalGoals") ?: 10L
                val completed = doc.getLong("completedGoals") ?: 4L
                val percent = (completed * 100 / total).toInt()
                pb.progress = percent
                tv.text = "$percent% of your goals completed"
            }
        }
    }

    private fun loadBadges(rv: RecyclerView) {
        if (userId == null) return
        db.collection("achievements").document(userId).collection("badges")
            .get().addOnSuccessListener { snapshots ->
                val badges = snapshots.mapNotNull { it.getString("title") }
                rv.adapter = BadgeAdapter(badges)
            }
    }

    inner class BadgeAdapter(private val items: List<String>) : RecyclerView.Adapter<BadgeAdapter.ViewHolder>() {
        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val iv: ImageView = v.findViewById(R.id.ivBadgeIcon)
            val tv: TextView = v.findViewById(R.id.tvBadgeName)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_badge, parent, false)
            return ViewHolder(v)
        }
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.tv.text = items[position]
            holder.iv.setImageResource(android.R.drawable.star_big_on)
        }
        override fun getItemCount() = items.size
    }
}
