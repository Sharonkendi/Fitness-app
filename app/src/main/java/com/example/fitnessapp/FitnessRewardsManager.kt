package com.example.fitnessapp

import android.content.Context
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FitnessRewardsManager {
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    fun checkAchievements(context: Context) {
        if (userId == null) return

        // Check for "First Step" achievement
        db.collection("steps_history").document(userId).collection("entries").get()
            .addOnSuccessListener { snapshots ->
                if (!snapshots.isEmpty && snapshots.size() == 1) {
                    awardBadge(context, "First Step", "Logged your first activity!")
                }
            }

        // Check for "Hydration Hero" (Logged water 5 times)
        db.collection("water_tracker").document(userId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val current = doc.getLong("current") ?: 0
                    if (current >= 2000) {
                        awardBadge(context, "Hydration Hero", "Reached daily water goal!")
                    }
                }
            }
    }

    private fun awardBadge(context: Context, title: String, message: String) {
        if (userId == null) return
        val data = mapOf(
            "title" to title,
            "message" to message,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("achievements").document(userId).collection("badges").add(data)
            .addOnSuccessListener {
                Toast.makeText(context, "New Achievement: $title!", Toast.LENGTH_LONG).show()
            }
    }
}
