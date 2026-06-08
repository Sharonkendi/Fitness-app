package com.example.fitnessapp

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)
        val etUsername = findViewById<EditText>(R.id.etUsername)

        btnRegister.setOnClickListener {
            val name = etUsername.text.toString().ifEmpty { "Fitness Enthusiast" }

            // Save name to SharedPreferences
            val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            with (sharedPref.edit()) {
                putString("USER_NAME", name)
                apply()
            }

            finish()
        }

        tvLogin.setOnClickListener {
            finish()
        }
    }
}
