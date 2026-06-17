package com.example.fitnessapp

import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class WaterTrackerActivity : AppCompatActivity() {
    private var currentWater = 1000
    private val targetWater = 2500
    
    private lateinit var progressBar: ProgressBar
    private lateinit var tvAmount: TextView
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_water_tracker)

        progressBar = findViewById(R.id.waterProgress)
        tvAmount = findViewById(R.id.tvWaterAmount)
        val btnAdd = findViewById<Button>(R.id.btnAddWater)

        savedInstanceState?.let {
            currentWater = it.getInt("CURRENT_WATER", 1000)
        }
        
        loadWaterFromFirestore()
        updateUI()

        btnAdd.setOnClickListener {
            currentWater += 250
            if (currentWater > targetWater) currentWater = targetWater
            updateUI()
            saveWaterToFirestore()
        }
    }

    private fun loadWaterFromFirestore() {
        if (userId == null) return
        db.collection("water_tracker").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    currentWater = document.getLong("current")?.toInt() ?: 1000
                    updateUI()
                }
            }
    }

    private fun saveWaterToFirestore() {
        if (userId == null) return
        val data = mapOf(
            "current" to currentWater,
            "target" to targetWater,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("water_tracker").document(userId).set(data)
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to sync water: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUI() {
        val progress = (currentWater * 100) / targetWater
        progressBar.progress = progress
        tvAmount.text = getString(R.string.water_amount_format, currentWater, targetWater)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("CURRENT_WATER", currentWater)
    }
}
