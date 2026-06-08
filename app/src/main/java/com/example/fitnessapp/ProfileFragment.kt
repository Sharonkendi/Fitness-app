package com.example.fitnessapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

class ProfileFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val tvName = view.findViewById<TextView>(R.id.tvProfileName)
        val sharedPref = activity?.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        tvName.text = sharedPref?.getString("USER_NAME", "John Doe")
        
        view.findViewById<Button>(R.id.btnUpdateAccount).setOnClickListener {
            Toast.makeText(context, "Account Updated Successfully!", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<Button>(R.id.btnDeleteAccount).setOnClickListener {
            Toast.makeText(context, "Account Deleted!", Toast.LENGTH_LONG).show()
            startActivity(Intent(activity, LoginActivity::class.java))
            activity?.finish()
        }

        view.findViewById<Button>(R.id.btnLogout).setOnClickListener {
            startActivity(Intent(activity, LoginActivity::class.java))
            activity?.finish()
        }
        
        return view
    }
}
