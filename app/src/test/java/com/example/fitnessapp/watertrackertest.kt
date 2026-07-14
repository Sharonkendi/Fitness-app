package com.example.fitnessapp

import org.junit.Assert.assertEquals
import org.junit.Test

class WaterTrackerTest {

    @Test
    fun addWaterGlass() {

        var glasses = 4

        glasses++

        assertEquals(5, glasses)
    }
}