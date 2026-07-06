package com.example.fitnessapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class HomeFragment : Fragment() {

    private val speechLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0) ?: ""
            handleVoiceCommand(spokenText)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val tvWelcome = view.findViewById<TextView>(R.id.tvWelcome)
        val btnVoice = view.findViewById<ImageButton>(R.id.btnVoiceCommand)
        
        // Load name from SharedPreferences
        val sharedPref = activity?.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val name = sharedPref?.getString("USER_NAME", "Sharon Kendi")
        tvWelcome.text = getString(R.string.welcome_message, name)

        btnVoice.setOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something like 'log water' or 'start run'")
            }
            speechLauncher.launch(intent)
        }

        view.findViewById<MaterialCardView>(R.id.cardDailyGoals).setOnClickListener {
            showStepsInputDialog()
        }

        view.findViewById<MaterialCardView>(R.id.cardTracking).setOnClickListener {
            startActivity(Intent(activity, TrackingActivity::class.java))
        }

        view.findViewById<MaterialCardView>(R.id.cardMood).setOnClickListener {
            showMoodInputDialog()
        }

        view.findViewById<MaterialCardView>(R.id.cardCycleTracking).setOnClickListener {
            startActivity(Intent(activity, CycleTrackingActivity::class.java))
        }

        view.findViewById<MaterialCardView>(R.id.cardAnalytics).setOnClickListener {
            startActivity(Intent(activity, AnalyticsActivity::class.java))
        }

        view.findViewById<MaterialCardView>(R.id.cardSettings).setOnClickListener {
            startActivity(Intent(activity, SettingsActivity::class.java))
        }

        view.findViewById<Button>(R.id.btnSOS).setOnClickListener {
            (activity as? MainActivity)?.triggerSOS()
        }

        return view
    }

    private fun handleVoiceCommand(command: String) {
        val lowerCommand = command.lowercase()
        when {
            lowerCommand.contains("water") -> {
                Toast.makeText(context, "Logging water via voice...", Toast.LENGTH_SHORT).show()
            }
            lowerCommand.contains("run") || lowerCommand.contains("track") -> {
                startActivity(Intent(activity, TrackingActivity::class.java))
            }
            else -> {
                Toast.makeText(context, "Heard: '$command'. Try saying 'log water'.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showMoodInputDialog() {
        val moods = arrayOf("Happy", "Calm", "Stressed", "Anxious", "Tired", "Energetic")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("How are you feeling?")
        builder.setItems(moods) { _, which ->
            saveMoodToFirestore(moods[which])
        }
        builder.show()
    }

    private fun saveMoodToFirestore(mood: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val data = mapOf("mood" to mood, "timestamp" to System.currentTimeMillis())
        db.collection("mood_history").document(userId).collection("entries").add(data)
            .addOnSuccessListener { Toast.makeText(context, "Mood saved!", Toast.LENGTH_SHORT).show() }
    }

    private fun showStepsInputDialog() {
        val context = context ?: return
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Enter Daily Steps")

        val input = android.widget.EditText(context)
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        builder.setView(input)

        builder.setPositiveButton("Save") { _, _ ->
            val steps = input.text.toString()
            if (steps.isNotEmpty()) {
                saveStepsToFirestore(steps)
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun saveStepsToFirestore(steps: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        val data = mapOf(
            "steps" to steps,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("steps_history").document(userId).collection("entries")
            .add(data)
            .addOnSuccessListener {
                Toast.makeText(context, "Steps saved!", Toast.LENGTH_SHORT).show()
                FitnessRewardsManager.checkAchievements(requireContext())
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to save steps: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
