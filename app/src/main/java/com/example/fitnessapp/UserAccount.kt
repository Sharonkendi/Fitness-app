package com.example.fitnessapp

data class UserAccount(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val gender: String = "",
    val age: Int = 0,
    val dob: String = "",
    val height: Double = 0.0,
    val weight: Double = 0.0,
    val bmi: Double = 0.0,
    val activityLevel: String = "",
    val fitnessGoals: String = "",
    val dietaryPreferences: String = "",
    val allergies: String = "",
    val dailyCalorieTarget: Int = 2000,
    val emergencyContacts: String = "",
    val profilePictureUrl: String = "",
    val profilePictureLat: Double? = null,
    val profilePictureLng: Double? = null,
    val lastWorkoutCategory: String = "General"
)
