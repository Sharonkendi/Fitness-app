package com.example.fitnessapp

import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class WaterTrackerActivity : AppCompatActivity() {
    private var currentWater = 1000
    private val targetWater = 2500
    
    private lateinit var progressBar: ProgressBar
    private lateinit var tvAmount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_water_tracker)

        progressBar = findViewById(R.id.waterProgress)
        tvAmount = findViewById(R.id.tvWaterAmount)
        val btnAdd = findViewById<Button>(R.id.btnAddWater)

        savedInstanceState?.let {
            currentWater = it.getInt("CURRENT_WATER", 1000)
        }
        
        updateUI()

        btnAdd.setOnClickListener {
            currentWater += 250
            if (currentWater > targetWater) currentWater = targetWater
            updateUI()
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
