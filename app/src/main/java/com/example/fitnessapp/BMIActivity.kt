package com.example.fitnessapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class BMIActivity : AppCompatActivity() {
    
    private lateinit var etWeight: EditText
    private lateinit var etHeight: EditText
    private lateinit var tvBMI: TextView
    private lateinit var tvCalorie: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bmi)

        etWeight = findViewById(R.id.etWeight)
        etHeight = findViewById(R.id.etHeight)
        tvBMI = findViewById(R.id.tvBMIResult)
        tvCalorie = findViewById(R.id.tvCalorieResult)
        val btnCalculate = findViewById<Button>(R.id.btnCalculate)

        btnCalculate.setOnClickListener {
            calculateBMI()
        }

        if (savedInstanceState != null) {
            calculateBMI()
        }
    }

    private fun calculateBMI() {
        val weight = etWeight.text.toString().toFloatOrNull() ?: 0f
        val height = etHeight.text.toString().toFloatOrNull() ?: 0f

        if (weight > 0 && height > 0) {
            val bmi = weight / ((height / 100) * (height / 100))
            tvBMI.text = getString(R.string.bmi_result, bmi)
            
            val dailyCalories = 10 * weight + 6.25 * height - 5 * 25 + 5 // Simplified Miflin-St Jeor
            tvCalorie.text = getString(R.string.calorie_needs, dailyCalories)
        }
    }
}
