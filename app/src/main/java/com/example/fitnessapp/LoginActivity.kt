package com.example.fitnessapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executor

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        executor = ContextCompat.getMainExecutor(this)

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnBiometric = findViewById<Button>(R.id.btnBiometric)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val tvForgot = findViewById<TextView>(R.id.tvForgotPassword)

        setupBiometric()

        val authMethod = SecurityHelper.getAuthMethod(this)

        // Always show email and password fields by default to allow "either" choice
        etEmail.visibility = View.VISIBLE
        etPassword.visibility = View.VISIBLE
        btnLogin.text = "Login"

        if (authMethod == SecurityHelper.AuthMethod.BIOMETRIC || 
            authMethod == SecurityHelper.AuthMethod.PASSWORD_BIOMETRIC || 
            authMethod == SecurityHelper.AuthMethod.PIN_BIOMETRIC) {
            btnBiometric.visibility = View.VISIBLE
            // Prompt immediately for pure biometric login
            if (authMethod == SecurityHelper.AuthMethod.BIOMETRIC) {
                etEmail.visibility = View.GONE
                etPassword.visibility = View.GONE
                btnLogin.visibility = View.GONE
                biometricPrompt.authenticate(promptInfo)
            }
        } else {
            btnBiometric.visibility = View.GONE
        }

        if (authMethod == SecurityHelper.AuthMethod.PIN || authMethod == SecurityHelper.AuthMethod.PIN_BIOMETRIC) {
            btnLogin.text = "Login with PIN"
            tvForgot.text = "Use Password or Reset"
        }

        btnLogin.setOnClickListener {
            if (btnLogin.text == "Login with PIN") {
                showPinEntryDialog()
            } else {
                val email = etEmail.text.toString().trim()
                val password = etPassword.text.toString().trim()
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                } else {
                    performLogin(email, password)
                }
            }
        }

        btnBiometric.setOnClickListener {
            biometricPrompt.authenticate(promptInfo)
        }

        tvForgot.setOnClickListener {
            if (btnLogin.text == "Login with PIN") {
                // Toggle back to password
                btnLogin.text = "Login"
                etEmail.visibility = View.VISIBLE
                etPassword.visibility = View.VISIBLE
            } else {
                showForgotPasswordDialog()
            }
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun showPinEntryDialog() {
        val pinInput = EditText(this)
        pinInput.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
        AlertDialog.Builder(this)
            .setTitle("Enter 4-Digit PIN")
            .setView(pinInput)
            .setPositiveButton("Login") { _, _ ->
                val enteredPin = pinInput.text.toString()
                val savedPin = SecurityHelper.getPin(this)
                if (enteredPin == savedPin) {
                    if (auth.currentUser != null) {
                        goToMain()
                    } else {
                        Toast.makeText(this, "Session expired, please login with password once.", Toast.LENGTH_LONG).show()
                        SecurityHelper.saveAuthMethod(this, SecurityHelper.AuthMethod.PASSWORD)
                        recreate()
                    }
                } else {
                    Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showForgotPasswordDialog() {
        val emailInput = EditText(this)
        emailInput.hint = "Enter your email"
        AlertDialog.Builder(this)
            .setTitle("Reset Password")
            .setView(emailInput)
            .setPositiveButton("Send") { _, _ ->
                val email = emailInput.text.toString().trim()
                if (email.isNotEmpty()) {
                    auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                        if (task.isSuccessful) Toast.makeText(this, "Reset link sent!", Toast.LENGTH_SHORT).show()
                        else Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupBiometric() {
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    if (auth.currentUser != null) goToMain()
                    else Toast.makeText(applicationContext, "Please login with password once.", Toast.LENGTH_SHORT).show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Secure Login")
            .setSubtitle("Log in using Fingerprint or Face")
            .setAllowedAuthenticators(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()
    }

    private fun performLogin(email: String, password: String) {
        lifecycleScope.launch {
            val hasAccess = withContext(Dispatchers.IO) {
                NetworkUtils.isNetworkAvailable(this@LoginActivity) && NetworkUtils.hasInternetAccess()
            }
            if (!hasAccess) {
                Toast.makeText(this@LoginActivity, "No internet connection.", Toast.LENGTH_SHORT).show()
                return@launch
            }
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this@LoginActivity) { task ->
                    if (task.isSuccessful) {
                        goToMain()
                    } else {
                        Toast.makeText(this@LoginActivity, "Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
