package com.example.fitnessapp

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

object SecurityHelper {
    private const val PREF_NAME = "secure_prefs"
    private const val KEY_PIN = "user_pin"
    private const val KEY_AUTH_METHOD = "auth_method"

    enum class AuthMethod {
        PASSWORD, PIN, BIOMETRIC, PASSWORD_BIOMETRIC, PIN_BIOMETRIC
    }

    private fun getPrefs(context: Context) = EncryptedSharedPreferences.create(
        PREF_NAME,
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun savePin(context: Context, pin: String) {
        getPrefs(context).edit().putString(KEY_PIN, pin).apply()
    }

    fun getPin(context: Context): String? {
        return getPrefs(context).getString(KEY_PIN, null)
    }

    fun saveAuthMethod(context: Context, method: AuthMethod) {
        getPrefs(context).edit().putString(KEY_AUTH_METHOD, method.name).apply()
    }

    fun getAuthMethod(context: Context): AuthMethod {
        val name = getPrefs(context).getString(KEY_AUTH_METHOD, AuthMethod.PASSWORD.name)
        return try { AuthMethod.valueOf(name!!) } catch (e: Exception) { AuthMethod.PASSWORD }
    }
}
