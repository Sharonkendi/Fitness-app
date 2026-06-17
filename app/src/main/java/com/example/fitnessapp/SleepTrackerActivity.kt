package com.example.fitnessapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class SleepTrackerActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sleep_tracker)

        val etSleepDuration = findViewById<EditText>(R.id.etSleepDuration)
        val btnSaveSleep = findViewById<Button>(R.id.btnSaveSleep)
        val tvLastSleep = findViewById<TextView>(R.id.tvLastSleepAmount)

        loadLastSleep(tvLastSleep)

        btnSaveSleep.setOnClickListener {
            val duration = etSleepDuration.text.toString()
            if (duration.isNotEmpty()) {
                saveSleepToFirestore(duration)
                tvLastSleep.text = duration
            } else {
                Toast.makeText(this, "Please enter sleep duration", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadLastSleep(textView: TextView) {
        if (userId == null) return
        db.collection("sleep_history").document(userId).collection("entries")
            .orderBy("timestamp", Query.Direction.DESCENDING).limit(1)
            .get().addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val lastEntry = querySnapshot.documents.first()
                    textView.text = lastEntry.getString("duration")
                }
            }
    }

    private fun saveSleepToFirestore(duration: String) {
        if (userId == null) return
        val data = mapOf(
            "duration" to duration,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("sleep_history").document(userId).collection("entries")
            .add(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Sleep saved!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save sleep: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
