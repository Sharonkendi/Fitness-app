package com.example.fitnessapp

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*

class AnalyticsActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)

        loadStepsData()
        loadWaterData()
        setupStaticHeartRateChart() // Simulated since live HR needs hardware device
    }

    private fun loadStepsData() {
        if (userId == null) return
        val chart = findViewById<BarChart>(R.id.stepsChart)
        
        db.collection("steps_history").document(userId).collection("entries")
            .orderBy("timestamp", Query.Direction.DESCENDING).limit(7)
            .get().addOnSuccessListener { snapshots ->
                val entries = ArrayList<BarEntry>()
                var index = 0f
                snapshots.documents.reversed().forEach { doc ->
                    val steps = doc.getString("steps")?.toFloatOrNull() ?: 0f
                    entries.add(BarEntry(index++, steps))
                }
                
                if (entries.isNotEmpty()) {
                    val set = BarDataSet(entries, "Daily Steps")
                    set.color = Color.parseColor("#4CAF50")
                    chart.data = BarData(set)
                    chart.description.isEnabled = false
                    chart.animateY(1000)
                    chart.invalidate()
                }
            }
    }

    private fun loadWaterData() {
        if (userId == null) return
        val chart = findViewById<BarChart>(R.id.waterChart)
        
        // Using water_tracker which currently stores only 'current' total
        // In a real app, we'd have a daily history collection
        db.collection("water_tracker").document(userId).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val current = doc.getLong("current")?.toFloat() ?: 0f
                val target = doc.getLong("target")?.toFloat() ?: 2500f
                
                val entries = ArrayList<BarEntry>()
                entries.add(BarEntry(0f, current))
                entries.add(BarEntry(1f, target))
                
                val set = BarDataSet(entries, "Current vs Target (ml)")
                set.setColors(Color.parseColor("#2196F3"), Color.parseColor("#BBDEFB"))
                chart.data = BarData(set)
                chart.description.isEnabled = false
                chart.invalidate()
            }
        }
    }

    private fun setupStaticHeartRateChart() {
        val chart = findViewById<LineChart>(R.id.heartRateChart)
        val entries = ArrayList<Entry>()
        // Mock data representing a typical exercise session
        entries.add(Entry(0f, 70f))
        entries.add(Entry(1f, 85f))
        entries.add(Entry(2f, 120f))
        entries.add(Entry(3f, 155f))
        entries.add(Entry(4f, 140f))
        entries.add(Entry(5f, 95f))
        entries.add(Entry(6f, 75f))

        val set = LineDataSet(entries, "Exercise Heart Rate (BPM)")
        set.color = Color.parseColor("#FF4081")
        set.setCircleColor(Color.parseColor("#FF4081"))
        set.lineWidth = 3f
        set.setDrawFilled(true)
        set.fillColor = Color.parseColor("#FF4081")
        set.mode = LineDataSet.Mode.CUBIC_BEZIER
        
        chart.data = LineData(set)
        chart.description.isEnabled = false
        chart.animateX(1000)
        chart.invalidate()
    }
}
