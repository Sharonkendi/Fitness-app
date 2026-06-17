package com.example.fitnessapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Theme is set in Manifest

        Handler(Looper.getMainLooper()).postDelayed({
            checkUserAndInternet()
        }, 2000) // 2 seconds delay
    }

    private fun checkUserAndInternet() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            // User is signed in, check internet
            lifecycleScope.launch {
                val hasAccess = withContext(Dispatchers.IO) {
                    NetworkUtils.isNetworkAvailable(this@SplashActivity) && NetworkUtils.hasInternetAccess()
                }

                if (hasAccess) {
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                } else {
                    Toast.makeText(this@SplashActivity, "No internet connection. Cannot start app.", Toast.LENGTH_LONG).show()
                    // Stay on splash or go to Login to try again
                    startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                }
                finish()
            }
        } else {
            // No user, go to login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
