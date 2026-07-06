package com.example.fitnessapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class CycleTrackingActivity : AppCompatActivity() {

    private var selectedStartDate: Calendar = Calendar.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cycle_tracking)

        val calendarView = findViewById<CalendarView>(R.id.calendarCycle)
        val etLength = findViewById<EditText>(R.id.etCycleLength)
        val btnCalculate = findViewById<Button>(R.id.btnCalculateCycle)
        val cardPredictions = findViewById<MaterialCardView>(R.id.cardPredictions)
        val tvNextPeriod = findViewById<TextView>(R.id.tvNextPeriod)
        val tvOvulation = findViewById<TextView>(R.id.tvOvulation)

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedStartDate.set(year, month, dayOfMonth)
        }

        btnCalculate.setOnClickListener {
            val length = etLength.text.toString().toIntOrNull() ?: 28
            
            // Next Period Calculation
            val nextPeriod = selectedStartDate.clone() as Calendar
            nextPeriod.add(Calendar.DAY_OF_YEAR, length)

            // Ovulation Calculation (usually 14 days before next period)
            val ovulationDay = nextPeriod.clone() as Calendar
            ovulationDay.add(Calendar.DAY_OF_YEAR, -14)

            val sdf = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault())
            val nextPeriodStr = sdf.format(nextPeriod.time)
            val ovulationStr = sdf.format(ovulationDay.time)

            tvNextPeriod.text = "Next Period: $nextPeriodStr"
            tvOvulation.text = "Ovulation Day: $ovulationStr"
            
            cardPredictions.visibility = View.VISIBLE
            
            saveCycleToFirestore(selectedStartDate.timeInMillis, length)
        }
    }

    private fun saveCycleToFirestore(startTime: Long, length: Int) {
        if (userId == null) return
        val data = mapOf(
            "lastPeriodStart" to startTime,
            "cycleLength" to length,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("cycle_history").document(userId).collection("entries").add(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Cycle prediction saved!", Toast.LENGTH_SHORT).show()
            }
    }
}
