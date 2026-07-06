package com.example.fitnessapp

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.biometric.BiometricManager
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val switchFingerprint = findViewById<SwitchMaterial>(R.id.switchFingerprint)
        val switchFace = findViewById<SwitchMaterial>(R.id.switchFaceUnlock)
        val tvBioStatus = findViewById<TextView>(R.id.tvBiometricStatus)
        
        val rgTheme = findViewById<RadioGroup>(R.id.rgTheme)
        val rgUnits = findViewById<RadioGroup>(R.id.rgUnits)
        val switchBg = findViewById<SwitchMaterial>(R.id.switchBackgroundTracking)
        val switchGps = findViewById<SwitchMaterial>(R.id.switchGpsPrecision)
        val btnSave = findViewById<Button>(R.id.btnSaveSettings)

        // Initial Hardware Check
        checkBiometricHardware(switchFingerprint, switchFace, tvBioStatus)

        // Load existing preferences
        val sharedPref = getSharedPreferences("AppSettings", MODE_PRIVATE)
        
        // Load Biometric Prefs
        val currentAuth = SecurityHelper.getAuthMethod(this)
        val isBioEnabled = currentAuth == SecurityHelper.AuthMethod.BIOMETRIC || 
                          currentAuth == SecurityHelper.AuthMethod.PASSWORD_BIOMETRIC
        
        switchFingerprint.isChecked = isBioEnabled
        switchFace.isChecked = isBioEnabled

        // Theme
        when (sharedPref.getInt("THEME_MODE", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)) {
            AppCompatDelegate.MODE_NIGHT_NO -> rgTheme.check(R.id.rbLight)
            AppCompatDelegate.MODE_NIGHT_YES -> rgTheme.check(R.id.rbDark)
            else -> rgTheme.check(R.id.rbSystem)
        }

        // Units
        if (sharedPref.getBoolean("USE_IMPERIAL", false)) {
            rgUnits.check(R.id.rbImperial)
        } else {
            rgUnits.check(R.id.rbMetric)
        }

        // Tracking
        switchBg.isChecked = sharedPref.getBoolean("BG_TRACKING", true)
        switchGps.isChecked = sharedPref.getBoolean("HIGH_PRECISION_GPS", true)

        btnSave.setOnClickListener {
            val editor = sharedPref.edit()
            
            // Handle Security
            val bioRequested = switchFingerprint.isChecked || switchFace.isChecked
            val method = if (bioRequested) SecurityHelper.AuthMethod.PASSWORD_BIOMETRIC else SecurityHelper.AuthMethod.PASSWORD
            SecurityHelper.saveAuthMethod(this, method)

            // Handle Theme
            val themeMode = when (rgTheme.checkedRadioButtonId) {
                R.id.rbLight -> AppCompatDelegate.MODE_NIGHT_NO
                R.id.rbDark -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            editor.putInt("THEME_MODE", themeMode)
            AppCompatDelegate.setDefaultNightMode(themeMode)

            // Handle Units
            editor.putBoolean("USE_IMPERIAL", rgUnits.checkedRadioButtonId == R.id.rbImperial)

            // Handle Switches
            editor.putBoolean("BG_TRACKING", switchBg.isChecked)
            editor.putBoolean("HIGH_PRECISION_GPS", switchGps.isChecked)

            editor.apply()
            Toast.makeText(this, "Settings updated and applied!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun checkBiometricHardware(f: SwitchMaterial, face: SwitchMaterial, status: TextView) {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                status.text = "Biometric hardware is ready."
                f.isEnabled = true
                face.isEnabled = true
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                status.text = "No biometric hardware detected on this device."
                f.isEnabled = false
                face.isEnabled = false
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                status.text = "Biometric hardware is currently unavailable."
                f.isEnabled = false
                face.isEnabled = false
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                status.text = "No biometrics enrolled. Please set them up in System Settings."
                // Keep enabled so user can toggle, but show warning
            }
            else -> {
                status.text = "Biometric status unknown."
            }
        }
    }
}
