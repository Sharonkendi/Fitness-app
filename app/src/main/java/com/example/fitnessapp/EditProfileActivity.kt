package com.example.fitnessapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        val etFullName = findViewById<EditText>(R.id.etFullName)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etAge = findViewById<EditText>(R.id.etAge)
        val etGender = findViewById<EditText>(R.id.etGender)
        val etHeight = findViewById<EditText>(R.id.etHeight)
        val etWeight = findViewById<EditText>(R.id.etWeight)
        val etFitnessGoal = findViewById<EditText>(R.id.etFitnessGoal)
        val etActivityLevel = findViewById<EditText>(R.id.etActivityLevel)
        val etCalorieTarget = findViewById<EditText>(R.id.etCalorieTarget)
        val etDiet = findViewById<EditText>(R.id.etDiet)
        val etAllergies = findViewById<EditText>(R.id.etAllergies)
        val btnSave = findViewById<Button>(R.id.btnSaveProfile)

        loadProfileData(etFullName, etPhone, etAge, etGender, etHeight, etWeight, etFitnessGoal, etActivityLevel, etCalorieTarget, etDiet, etAllergies)

        btnSave.setOnClickListener {
            saveProfileData(
                etFullName.text.toString(),
                etPhone.text.toString(),
                etAge.text.toString().toIntOrNull() ?: 0,
                etGender.text.toString(),
                etHeight.text.toString().toDoubleOrNull() ?: 0.0,
                etWeight.text.toString().toDoubleOrNull() ?: 0.0,
                etFitnessGoal.text.toString(),
                etActivityLevel.text.toString(),
                etCalorieTarget.text.toString().toIntOrNull() ?: 2000,
                etDiet.text.toString(),
                etAllergies.text.toString()
            )
        }
    }

    private fun loadProfileData(vararg views: EditText) {
        if (userId == null) return
        db.collection("users").document(userId).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val profile = doc.toObject(UserAccount::class.java)
                profile?.let {
                    views[0].setText(it.fullName)
                    views[1].setText(it.phone)
                    views[2].setText(it.age.toString())
                    views[3].setText(it.gender)
                    views[4].setText(it.height.toString())
                    views[5].setText(it.weight.toString())
                    views[6].setText(it.fitnessGoals)
                    views[7].setText(it.activityLevel)
                    views[8].setText(it.dailyCalorieTarget.toString())
                    views[9].setText(it.dietaryPreferences)
                    views[10].setText(it.allergies)
                }
            }
        }
    }

    private fun saveProfileData(name: String, phone: String, age: Int, gender: String, h: Double, w: Double, goal: String, activity: String, calories: Int, diet: String, allergies: String) {
        if (userId == null) return
        val updates = mapOf(
            "fullName" to name,
            "phone" to phone,
            "age" to age,
            "gender" to gender,
            "height" to h,
            "weight" to w,
            "bmi" to if (h > 0) w / ((h/100)*(h/100)) else 0.0,
            "fitnessGoals" to goal,
            "activityLevel" to activity,
            "dailyCalorieTarget" to calories,
            "dietaryPreferences" to diet,
            "allergies" to allergies
        )
        db.collection("users").document(userId).update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile and Goals updated!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
