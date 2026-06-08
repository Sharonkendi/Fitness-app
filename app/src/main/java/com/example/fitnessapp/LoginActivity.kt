package com.example.fitnessapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)
        val etName = findViewById<EditText>(R.id.etName)

        btnLogin.setOnClickListener {
            val name = etName.text.toString().ifEmpty { "Fitness Enthusiast" }
            
            // Save name to SharedPreferences
            val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            sharedPref.edit {
                putString("USER_NAME", name)
            }

            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
