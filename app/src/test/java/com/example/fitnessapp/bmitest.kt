package com.example.fitnessapp

import org.junit.Assert.assertEquals
import org.junit.Test

class BMITest {

    @Test
    fun calculateBMI() {

        val weight = 69.0
        val height = 1.57

        val bmi = weight / (height * height)

        assertEquals(28.0, bmi, 0.1)
    }
}