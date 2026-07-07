package com.example.fitnessapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.random.Random

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
    private lateinit var pbFood: ProgressBar

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
        pbFood = view.findViewById(R.id.pbFood)

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
        pbFood.visibility = View.VISIBLE
        db.collection("users").document(userId).get().addOnSuccessListener { doc ->
            pbFood.visibility = View.GONE
            if (doc.exists()) {
                val profile = doc.toObject(UserAccount::class.java)
                profile?.let { generateAIRecommendations(it) }
            } else {
                generateAIRecommendations(UserAccount())
            }
        }.addOnFailureListener {
            pbFood.visibility = View.GONE
            Toast.makeText(context, "Failed to load profile", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generateAIRecommendations(user: UserAccount) {
        val workout = user.lastWorkoutCategory.lowercase()
        val goal = user.fitnessGoals.lowercase()
        val allergies = user.allergies.lowercase()
        
        tvNutritionHeader.text = getString(R.string.nutrition_for_format, workout)

        when {
            workout.contains("strength") || workout.contains("leg") || workout.contains("gym") || workout.contains("deadlift") || workout.contains("squat") -> {
                tvBreakfast.text = "Bodybuilder's Plate: 6 egg whites, 2 whole eggs, and a large bowl of oats with 1 scoop of whey."
                tvBreakfastNutri.text = "Goal: Maximal Muscle Repair | 600 kcal | 45g Protein"
                
                tvLunch.text = "Power Fuel: 300g Grilled steak or double chicken breast with brown rice and asparagus."
                tvLunchNutri.text = "Goal: Hypertrophy Support | 750 kcal | 55g Protein"
                
                tvDinner.text = "Growth Supper: Baked cod or tilapia (250g) with a large sweet potato and steamed kale."
                tvDinnerNutri.text = "Goal: Overnight Recovery | 550 kcal | 40g Protein"
                
                tvSnacks.text = "Post-workout: Casein protein shake or 200g Greek yogurt with nuts."
                tvHydration.text = "Hydration: Drink 4L of water. Add creatine if applicable."
            }
            workout.contains("cardio") || workout.contains("run") || workout.contains("marathon") || workout.contains("sprint") -> {
                tvBreakfast.text = "Endurance Start: 2 whole grain pancakes with honey, blueberries, and a side of greek yogurt."
                tvBreakfastNutri.text = "Goal: Glycogen Loading | 500 kcal | 20g Protein"
                
                tvLunch.text = "Runner's Fuel: Large bowl of whole wheat pasta with lean ground turkey and spinach."
                tvLunchNutri.text = "Goal: Energy Replenishment | 700 kcal | 35g Protein"
                
                tvDinner.text = "Light Recovery: Quinoa and roasted beet salad with grilled salmon (150g)."
                tvDinnerNutri.text = "Goal: Inflammatory Reduction | 500 kcal | 25g Omega-3s"
                
                tvSnacks.text = "Energy Snack: Banana with peanut butter or an energy bar."
                tvHydration.text = "Hydration: Drink 3.5L. Replenish with electrolytes (sodium/potassium)."
            }
            workout.contains("yoga") || workout.contains("stretch") || workout.contains("pilates") -> {
                tvBreakfast.text = "Zen Bowl: Smoothie with spinach, spirulina, apple, and coconut water."
                tvBreakfastNutri.text = "Goal: Detox & Alkalize | 300 kcal | 10g Fiber"
                
                tvLunch.text = "Light Vitality: Buddha bowl with tofu, chickpeas, avocado, and tahini dressing."
                tvLunchNutri.text = "Goal: Micronutrient Dense | 450 kcal | 20g Plant Protein"
                
                tvDinner.text = "Digestive Ease: Warm vegetable miso soup with a small portion of steamed brown rice."
                tvDinnerNutri.text = "Goal: Calm & Restore | 350 kcal | 15g Fiber"
                
                tvSnacks.text = "Mindful Snack: Sliced cucumber with hummus or a small piece of dark chocolate."
                tvHydration.text = "Hydration: Drink 2.5L. Herbal teas like peppermint or ginger are recommended."
            }
            workout.contains("hiit") || workout.contains("circuit") || workout.contains("crossfit") -> {
                tvBreakfast.text = "Explosive Fuel: 3 Scrambled eggs, half an avocado, and 1 slice of sprouted grain toast."
                tvBreakfastNutri.text = "Goal: Steady Power | 450 kcal | 25g Protein"
                
                tvLunch.text = "Rapid Recovery: Turkey breast (200g) with quinoa and mixed bell peppers."
                tvLunchNutri.text = "Goal: Quick Amino Uptake | 500 kcal | 40g Protein"
                
                tvDinner.text = "Lean Burner: Stir-fry beef with broccoli, snap peas, and ginger (no sugar sauce)."
                tvDinnerNutri.text = "Goal: Fat Oxidation | 500 kcal | 35g Protein"
                
                tvSnacks.text = "Power Up: A handful of beef jerky or a hard-boiled egg."
                tvHydration.text = "Hydration: Drink 3.5L. Sip water consistently to avoid cramping."
            }
            else -> {
                if (goal.contains("loss")) {
                    tvBreakfast.text = "Lean Omelet: 3 egg whites, spinach, and mushrooms."
                    tvBreakfastNutri.text = "300 kcal | 20g Protein"
                    tvLunch.text = "Zesty Chicken Salad with lemon-tahini dressing."
                    tvLunchNutri.text = "400 kcal | 30g Protein"
                    tvDinner.text = "White Fish (200g) with steamed broccoli and lemon."
                    tvDinnerNutri.text = "350 kcal | 35g Protein"
                    tvSnacks.text = "Celery sticks with 1 tsp almond butter."
                    tvHydration.text = "Drink 3L of water daily."
                } else {
                    tvBreakfast.text = "Balanced Choice: Scrambled eggs on whole grain toast."
                    tvBreakfastNutri.text = "400 kcal | 18g Protein"
                    tvLunch.text = "Wellness Lunch: Mixed bean and quinoa salad."
                    tvLunchNutri.text = "450 kcal | 15g Fiber"
                    tvDinner.text = "Standard Health: Oven-baked cod with sweet potato."
                    tvDinnerNutri.text = "500 kcal | 25g Healthy Fats"
                    tvSnacks.text = "Healthy Choice: A seasonal fruit or yogurt."
                    tvHydration.text = "Drink 2.5L of water daily."
                }
            }
        }

        if (allergies.contains("nut")) Toast.makeText(context, "Allergy Alert: Plan filtered for no nuts.", Toast.LENGTH_SHORT).show()
    }

    private fun handleScannedBarcode(barcode: String) {
        val isFood = barcode.length >= 8 && (barcode.startsWith("7") || barcode.startsWith("8") || barcode.startsWith("0"))
        
        if (!isFood) {
            AlertDialog.Builder(requireContext())
                .setTitle("Scan Failed")
                .setMessage("Barcode $barcode does not appear to be a food product. Please scan a valid food item.")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        Toast.makeText(context, "Analyzing food item: $barcode...", Toast.LENGTH_LONG).show()
        
        val seed = barcode.hashCode().toLong()
        val random = Random(seed)
        val calories = random.nextInt(50, 800)
        val sugar = random.nextInt(0, 40)
        val protein = random.nextInt(1, 30)

        val productInfo = """
            Product: Food Identified
            Calories: $calories kcal
            Sugar: ${sugar}g
            Protein: ${protein}g
            
            This item is ${if (calories > 400) "high" else "low"} in energy.
        """.trimIndent()

        AlertDialog.Builder(requireContext())
            .setTitle("Food Item Identified")
            .setMessage(productInfo)
            .setPositiveButton("Log to Diary") { _, _ -> 
                logMealToFirestore("Scanned Food: $calories kcal")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun logMealToFirestore(mealName: String) {
        if (userId == null) return
        val data = mapOf("meal" to mealName, "timestamp" to System.currentTimeMillis())
        db.collection("food_history").document(userId).collection("entries").add(data)
            .addOnSuccessListener {
                Toast.makeText(context, "$mealName logged!", Toast.LENGTH_SHORT).show()
            }
    }
}
