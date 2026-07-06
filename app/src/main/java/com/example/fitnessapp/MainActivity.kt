package com.example.fitnessapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.navigation.NavigationBarView
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        setupBackgroundSync()

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

    private fun setupBackgroundSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "FitnessSync",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }

    fun startCamera() {
        startActivity(Intent(this, CameraActivity::class.java))
    }

    fun triggerSOS() {
        sendBroadcast(Intent(this, EmergencySOSReceiver::class.java))
        Toast.makeText(this, "Emergency SOS Triggered!", Toast.LENGTH_LONG).show()
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
