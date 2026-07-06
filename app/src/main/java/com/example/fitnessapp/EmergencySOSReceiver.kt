package com.example.fitnessapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.widget.Toast
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EmergencySOSReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val db = FirebaseFirestore.getInstance()

        if (userId != null) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    val locationUrl = if (location != null) {
                        "https://maps.google.com/?q=${location.latitude},${location.longitude}"
                    } else {
                        "Location unavailable"
                    }
                    
                    val message = "EMERGENCY SOS: I need help! My current status and location: $locationUrl"
                    
                    // Fetch Emergency Contact from Profile
                    db.collection("users").document(userId).get().addOnSuccessListener { doc ->
                        val emergencyContact = doc.getString("phone") // Using phone as fallback if dedicated contact not set
                        val fullName = doc.getString("fullName") ?: "A user"
                        
                        val finalMessage = "$message (Sent by $fullName via FitnessApp)"
                        
                        if (!emergencyContact.isNullOrEmpty()) {
                            sendSms(emergencyContact, finalMessage)
                        }
                    }

                    Toast.makeText(context, "Emergency Text Sent to Contacts!", Toast.LENGTH_LONG).show()
                }
            } catch (e: SecurityException) {
                Toast.makeText(context, "Location permission required for SOS", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendSms(phoneNumber: String, message: String) {
        try {
            val smsManager = SmsManager.getDefault()
            val parts = smsManager.divideMessage(message)
            smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
        } catch (e: Exception) {
            // Handle error or log
        }
    }
}
