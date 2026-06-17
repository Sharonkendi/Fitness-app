package com.example.fitnessapp

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import java.net.InetSocketAddress
import java.net.Socket

object NetworkUtils {
    /**
     * Checks if there is any active network connection.
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /**
     * Performs a more thorough check by trying to connect to a known server (e.g., Google DNS).
     * Run this on a background thread if possible, but here we provide it for critical checks.
     */
    fun hasInternetAccess(): Boolean {
        return try {
            val timeoutMs = 1500
            val socket = Socket()
            val socketAddress = InetSocketAddress("8.8.8.8", 53)
            socket.connect(socketAddress, timeoutMs)
            socket.close()
            true
        } catch (e: Exception) {
            Log.e("NetworkUtils", "No internet access: ${e.message}")
            false
        }
    }
}
