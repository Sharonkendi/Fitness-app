package com.example.fitnessapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FoodFragment : Fragment() {
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    private lateinit var tvBreakfast: TextView
    private lateinit var tvBreakfastNutri: TextView
    private lateinit var tvLunch: TextView
    private lateinit var tvLunchNutri: TextView
    private lateinit var tvDinner: TextView
    private lateinit var tvDinnerNutri: TextView
    private lateinit var tvSnacks: TextView
    private lateinit var tvHydration: TextView
    private lateinit var tvNutritionHeader: TextView

    private val scanLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val barcode = result.data?.getStringExtra("barcode") ?: "Unknown"
            handleScannedBarcode(barcode)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_food, container, false)

        tvBreakfast = view.findViewById(R.id.tvBreakfastItems)
        tvBreakfastNutri = view.findViewById(R.id.tvBreakfastNutri)
        tvLunch = view.findViewById(R.id.tvLunchItems)
        tvLunchNutri = view.findViewById(R.id.tvLunchNutri)
        tvDinner = view.findViewById(R.id.tvDinnerItems)
        tvDinnerNutri = view.findViewById(R.id.tvDinnerNutri)
        tvSnacks = view.findViewById(R.id.tvSnackItems)
        tvHydration = view.findViewById(R.id.tvHydrationAdvice)
        tvNutritionHeader = view.findViewById(R.id.tvNutritionTitle)

        val btnScan = view.findViewById<Button>(R.id.btnScan)
        val btnRefresh = view.findViewById<ImageButton>(R.id.btnRefresh)
        val btnLog = view.findViewById<Button>(R.id.btnLogMeal)

        loadProfileAndGenerateAI()

        btnScan.setOnClickListener {
            val intent = Intent(requireContext(), CameraActivity::class.java)
            scanLauncher.launch(intent)
        }

        btnRefresh.setOnClickListener {
            loadProfileAndGenerateAI()
            Toast.makeText(context, "Recommendations Refreshed!", Toast.LENGTH_SHORT).show()
        }

        btnLog.setOnClickListener {
            logMealToFirestore("AI Balanced Day Plan")
        }

        return view
    }

    private fun loadProfileAndGenerateAI() {
        if (userId == null) return
        db.collection("users").document(userId).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val profile = doc.toObject(UserAccount::class.java)
                profile?.let { generateAIRecommendations(it) }
            } else {
                generateAIRecommendations(UserAccount())
            }
        }
    }

    private fun generateAIRecommendations(user: UserAccount) {
        val workout = user.lastWorkoutCategory.lowercase()
        val goal = user.fitnessGoals.lowercase()
        val allergies = user.allergies.lowercase()
        
        tvNutritionHeader.text = "Nutrition for $workout"

        when {
            workout.contains("strength") || workout.contains("leg") || workout.contains("gym") || workout.contains("arm") -> {
                tvBreakfast.text = "High-Protein Omelet: 4 egg whites, 1 whole egg, spinach, and mushrooms."
                tvBreakfastNutri.text = "Goal: Muscle Repair | 450 kcal | 35g Protein"
                
                tvLunch.text = "Lean Muscle Fuel: 250g Grilled chicken, brown rice (1 cup), and broccoli."
                tvLunchNutri.text = "Goal: Glycogen Refill | 550 kcal | 40g Protein"
                
                tvDinner.text = "Amino Recovery: 200g Baked salmon or lean steak with sweet potato."
                tvDinnerNutri.text = "Goal: Cell Regeneration | 600 kcal | 35g Protein"
                
                tvSnacks.text = "Post-workout whey protein shake + 10 raw almonds."
                tvHydration.text = "Hydration: Drink 3.5L of water. Focus on electrolyte intake."
            }
            workout.contains("cardio") || workout.contains("run") || workout.contains("cycle") || workout.contains("walk") -> {
                tvBreakfast.text = "Energy Charger: Whole grain bagel with natural peanut butter and a banana."
                tvBreakfastNutri.text = "Goal: Endurance Prep | 500 kcal | 15g Protein"
                
                tvLunch.text = "Carb Refuel: Whole wheat pasta with lean turkey mince and marinara sauce."
                tvLunchNutri.text = "Goal: Replenish Energy | 600 kcal | 30g Protein"
                
                tvDinner.text = "Light Recovery: Quinoa bowl with roasted Mediterranean veggies and chickpeas."
                tvDinnerNutri.text = "Goal: Digestive Comfort | 450 kcal | 20g Fiber"
                
                tvSnacks.text = "Watermelon slices or an orange with greek yogurt."
                tvHydration.text = "Hydration: Drink 3-4L. Sip 500ml every hour after your session."
            }
            workout.contains("yoga") || workout.contains("stretch") || workout.contains("home") -> {
                tvBreakfast.text = "Mindful Bowl: Greek yogurt with mixed forest berries and chia seeds."
                tvBreakfastNutri.text = "Goal: Anti-inflammatory | 350 kcal | 20g Protein"
                
                tvLunch.text = "Vitality Salad: Large kale and spinach salad with avocado, tofu, and seeds."
                tvLunchNutri.text = "Goal: Micronutrient Boost | 400 kcal | 15g Healthy Fats"
                
                tvDinner.text = "Soothing Soup: Red lentil dhal with a small side of brown rice."
                tvDinnerNutri.text = "Goal: Gut Health | 450 kcal | 18g Fiber"
                
                tvSnacks.text = "Apple slices with almond butter or green tea."
                tvHydration.text = "Hydration: Drink 2.5L. Sip coconut water for natural potassium."
            }
            workout.contains("hiit") || workout.contains("sprint") -> {
                tvBreakfast.text = "Metabolic Pancakes: Made with cottage cheese and oats, topped with berries."
                tvBreakfastNutri.text = "Goal: High Burn | 450 kcal | 30g Protein"
                
                tvLunch.text = "Performance Wrap: Turkey, avocado, and spinach in a whole-wheat wrap."
                tvLunchNutri.text = "Goal: Quick Recovery | 500 kcal | 25g Protein"
                
                tvDinner.text = "Lean Leaner: Beef stir-fry with colorful peppers and ginger."
                tvDinnerNutri.text = "Goal: Metabolism Support | 550 kcal | 30g Protein"
                
                tvSnacks.text = "Low-fat cottage cheese with pineapple or a hard-boiled egg."
                tvHydration.text = "Hydration: Drink 3.5L. Drink 250ml every 15 mins during HIIT."
            }
            else -> {
                // Default based on General Goal if no specific workout entered recently
                if (goal.contains("loss")) {
                    tvBreakfast.text = "Veggie Omelet (3 egg whites) + Green Tea"
                    tvBreakfastNutri.text = "350 kcal | 25g Protein"
                    tvLunch.text = "Grilled Chicken Salad + Light Vinaigrette"
                    tvLunchNutri.text = "450 kcal | 35g Protein"
                    tvDinner.text = "Baked Salmon + Steamed Asparagus"
                    tvDinnerNutri.text = "400 kcal | 30g Healthy Fats"
                    tvSnacks.text = "Apple slices or 10 raw almonds."
                    tvHydration.text = "Drink 3L of water daily."
                } else {
                    tvBreakfast.text = "Balanced Start: Scrambled eggs on whole grain toast."
                    tvBreakfastNutri.text = "400 kcal | 18g Protein"
                    tvLunch.text = "Wellness Lunch: Mixed bean and quinoa salad."
                    tvLunchNutri.text = "450 kcal | 15g Fiber"
                    tvDinner.text = "Standard Health: Oven-baked cod with sweet potato."
                    tvDinnerNutri.text = "500 kcal | 25g Healthy Fats"
                    tvSnacks.text = "Handful of grapes or a small orange."
                    tvHydration.text = "Drink 2.5L of water daily."
                }
            }
        }

        if (allergies.contains("nut")) Toast.makeText(context, "Allergy Alert: Plan filtered for no nuts.", Toast.LENGTH_SHORT).show()
    }

    private fun handleScannedBarcode(barcode: String) {
        Toast.makeText(context, "Analyzing barcode: $barcode...", Toast.LENGTH_LONG).show()
        val simulatedInfo = "Nutritional Info for $barcode: 150 kcal, 2g Sugar. Perfect for your active session!"
        AlertDialog.Builder(requireContext())
            .setTitle("Product Analyzed")
            .setMessage(simulatedInfo)
            .setPositiveButton("Log Meal") { _, _ -> logMealToFirestore("Barcode: $barcode") }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun logMealToFirestore(mealName: String) {
        if (userId == null) return
        val data = mapOf(
            "meal" to mealName,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("food_history").document(userId).collection("entries").add(data)
            .addOnSuccessListener {
                Toast.makeText(context, "Meal details saved to history!", Toast.LENGTH_SHORT).show()
            }
    }
}
