package com.example.fitnessapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable Firebase persistence
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)

        setContentView(R.layout.activity_main)

        val navView = findViewById<NavigationBarView>(R.id.bottom_navigation)
            ?: findViewById<NavigationBarView>(R.id.navigation_rail)

        // Set default fragment only if first time
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        navView?.setOnItemSelectedListener { item ->
            val fragment: Fragment? = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_workout -> WorkoutFragment()
                R.id.nav_food -> FoodFragment()
                R.id.nav_goals -> GoalsFragment()
                R.id.nav_profile -> ProfileFragment()
                else -> null
            }

            fragment?.let {
                loadFragment(it)
                true
            } ?: false
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
