package com.example.fitnessapp

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import android.content.Intent
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class HistoryActivity : AppCompatActivity() {

    private lateinit var rvHistory: RecyclerView
    private val historyList = mutableListOf<HistoryItem>()
    private lateinit var adapter: HistoryAdapter
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        rvHistory = findViewById(R.id.rvHistory)
        rvHistory.layoutManager = LinearLayoutManager(this)
        adapter = HistoryAdapter(historyList)
        rvHistory.adapter = adapter

        findViewById<Button>(R.id.btnExportPdf).setOnClickListener {
            exportToPDF()
        }

        findViewById<View>(R.id.btnShare).setOnClickListener {
            shareHistory()
        }

        loadAllHistoryFromFirestore()
    }

    private fun shareHistory() {
        val shareText = "I've logged ${historyList.size} activities on my Fitness App! Join me in reaching our goals."
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(intent, "Share Progress"))
    }

    private fun loadAllHistoryFromFirestore() {
        if (userId == null) return
        val db = FirebaseFirestore.getInstance()

        // Fetch Workouts
        db.collection("workouts").document(userId).collection("entries")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                val workoutItems = snapshots?.mapNotNull { ds ->
                    val plan = ds.getString("plan") ?: ""
                    val weight = ds.getString("weight") ?: ""
                    val height = ds.getString("height") ?: ""
                    val time = ds.getLong("timestamp") ?: 0L
                    HistoryItem("WORKOUT", plan, "Weight: ${weight}kg | Height: ${height}cm", time)
                } ?: emptyList()
                updateHistoryList("WORKOUT", workoutItems)
            }

        // Fetch Sleep
        db.collection("sleep_history").document(userId).collection("entries")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                val sleepItems = snapshots?.mapNotNull { ds ->
                    val duration = ds.getString("duration") ?: ""
                    val time = ds.getLong("timestamp") ?: 0L
                    HistoryItem("SLEEP", duration, "Recorded Duration", time)
                } ?: emptyList()
                updateHistoryList("SLEEP", sleepItems)
            }

        // Fetch Steps
        db.collection("steps_history").document(userId).collection("entries")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                val stepItems = snapshots?.mapNotNull { ds ->
                    val steps = ds.getString("steps") ?: ""
                    val time = ds.getLong("timestamp") ?: 0L
                    HistoryItem("STEPS", "$steps Steps", "Daily Movement", time)
                } ?: emptyList()
                updateHistoryList("STEPS", stepItems)
            }

        // Fetch Water
        db.collection("water_tracker").document(userId).addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
            val current = snapshot.getLong("current") ?: 0
            val target = snapshot.getLong("target") ?: 0
            val time = snapshot.getLong("timestamp") ?: 0L
            if (time > 0) {
                val waterItem = HistoryItem("WATER", "${current}ml / ${target}ml", "Daily Total", time)
                updateHistoryList("WATER", listOf(waterItem))
            }
        }

        // Fetch BMI
        db.collection("bmi_history").document(userId).collection("entries")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                val bmiItems = snapshots?.mapNotNull { ds ->
                    val weight = ds.getString("weight") ?: ""
                    val bmi = ds.getString("bmi") ?: ""
                    val time = ds.getLong("timestamp") ?: 0L
                    HistoryItem("BMI", bmi, "Weight: ${weight}kg", time)
                } ?: emptyList()
                updateHistoryList("BMI", bmiItems)
            }

        // Fetch Cycle
        db.collection("cycle_history").document(userId).collection("entries")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                val cycleItems = snapshots?.mapNotNull { ds ->
                    val length = ds.getLong("cycleLength") ?: 28L
                    val time = ds.getLong("timestamp") ?: 0L
                    HistoryItem("CYCLE", "$length Day Cycle", "Last Period Logged", time)
                } ?: emptyList()
                updateHistoryList("CYCLE", cycleItems)
            }
    }

    private fun updateHistoryList(type: String, newItems: List<HistoryItem>) {
        historyList.removeAll { it.type == type }
        historyList.addAll(newItems)
        historyList.sortByDescending { it.timestamp }
        adapter.notifyDataSetChanged()
        
        // Check for achievements when history updates
        FitnessRewardsManager.checkAchievements(this)
    }

    private fun exportToPDF() {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()
        
        paint.textSize = 12f
        canvas.drawText("Fitness App - History Report", 10f, 25f, paint)
        
        var y = 50f
        for (item in historyList.take(20)) {
            canvas.drawText("${item.type}: ${item.mainInfo}", 10f, y, paint)
            y += 20f
            if (y > 580f) break
        }
        
        pdfDocument.finishPage(page)
        
        val file = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "FitnessHistory.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(this, "PDF Exported to: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Export Failed: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }

    data class HistoryItem(
        val type: String,
        val mainInfo: String,
        val subInfo: String,
        val timestamp: Long
    )

    inner class HistoryAdapter(private val items: List<HistoryItem>) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvType: TextView = view.findViewById(R.id.tvType)
            val tvMain: TextView = view.findViewById(R.id.tvMainInfo)
            val tvSub: TextView = view.findViewById(R.id.tvSubInfo)
            val tvDate: TextView = view.findViewById(R.id.tvDate)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.tvType.text = item.type
            holder.tvMain.text = item.mainInfo
            holder.tvSub.text = item.subInfo
            
            val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
            holder.tvDate.text = sdf.format(Date(item.timestamp))

            val colorRes = when (item.type) {
                "WORKOUT" -> R.color.workoutPurple
                "SLEEP" -> R.color.sleepIndigo
                "WATER" -> R.color.waterBlue
                "STEPS" -> R.color.accentColor
                "BMI" -> R.color.primaryColor
                else -> R.color.primaryColor
            }
            holder.tvType.setTextColor(ContextCompat.getColor(holder.itemView.context, colorRes))
        }

        override fun getItemCount() = items.size
    }
}
