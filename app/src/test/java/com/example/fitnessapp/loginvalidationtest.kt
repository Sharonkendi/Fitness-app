

package com.example.fitnessapp

import org.junit.Assert.assertTrue
import org.junit.Test

class LoginValidationTest {

    @Test
    fun loginWithValidCredentials() {

        val username = "kendi"
        val password = "12345"

        val isValid = username == "kendi" && password == "12345"

        assertTrue(isValid)
    }
}