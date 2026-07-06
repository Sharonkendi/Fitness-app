package com.example.fitnessapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class ProfileFragment : Fragment() {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    
    private lateinit var ivProfile: ImageView
    private lateinit var tvLocation: TextView

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uploadImage(it) }
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val bitmap = result.data?.extras?.get("data") as? Bitmap
            if (bitmap != null) {
                uploadBitmap(bitmap)
            } else {
                val uri = result.data?.data
                val lat = result.data?.getDoubleExtra("lat", 0.0) ?: 0.0
                val lng = result.data?.getDoubleExtra("lng", 0.0) ?: 0.0
                uri?.let { uploadImageWithLocation(it, lat, lng) }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val tvName = view.findViewById<TextView>(R.id.tvProfileName)
        val tvEmail = view.findViewById<TextView>(R.id.tvProfileEmail)
        tvLocation = view.findViewById(R.id.tvPictureLocation)
        ivProfile = view.findViewById(R.id.ivProfile)
        
        loadProfileHeader(tvName, tvEmail)
        
        view.findViewById<Button>(R.id.btnUpdateAccount).setOnClickListener {
            startActivity(Intent(activity, EditProfileActivity::class.java))
        }

        view.findViewById<Button>(R.id.btnLogout).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(activity, LoginActivity::class.java))
            activity?.finish()
        }

        view.findViewById<Button>(R.id.btnDeleteAccount).setOnClickListener {
            showDeleteConfirmDialog()
        }

        view.findViewById<Button>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(activity, SettingsActivity::class.java))
        }

        ivProfile.setOnClickListener {
            showImagePickerOptions()
        }

        return view
    }

    private fun showImagePickerOptions() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        AlertDialog.Builder(requireContext())
            .setTitle("Profile Picture")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        val intent = Intent(activity, CameraActivity::class.java)
                        cameraLauncher.launch(intent)
                    }
                    1 -> galleryLauncher.launch(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI))
                }
            }
            .show()
    }

    private fun uploadImage(uri: Uri) {
        if (userId == null) return
        val ref = storage.reference.child("profile_pics/$userId")
        ref.putFile(uri).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener { downloadUri ->
                db.collection("users").document(userId).update("profilePictureUrl", downloadUri.toString())
                Glide.with(this).load(downloadUri).into(ivProfile)
                Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadImageWithLocation(uri: Uri, lat: Double, lng: Double) {
        if (userId == null) return
        val ref = storage.reference.child("profile_pics/$userId")
        ref.putFile(uri).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener { downloadUri ->
                val updates = mapOf(
                    "profilePictureUrl" to downloadUri.toString(),
                    "profilePictureLat" to lat,
                    "profilePictureLng" to lng
                )
                db.collection("users").document(userId).update(updates)
                Glide.with(this).load(downloadUri).into(ivProfile)
                tvLocation.text = "Last photo taken at: $lat, $lng"
                Toast.makeText(context, "Profile updated with location!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadBitmap(bitmap: Bitmap) {
        if (userId == null) return
        val ref = storage.reference.child("profile_pics/$userId")
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        ref.putBytes(data).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener { downloadUri ->
                db.collection("users").document(userId).update("profilePictureUrl", downloadUri.toString())
                Glide.with(this).load(downloadUri).into(ivProfile)
                Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadProfileHeader(nameTv: TextView, emailTv: TextView) {
        if (userId == null) return
        db.collection("users").document(userId).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                nameTv.text = doc.getString("fullName") ?: "Sharon Kendi"
                emailTv.text = doc.getString("email") ?: "sharonkendi55@gmail.com"
                
                val photoUrl = doc.getString("profilePictureUrl")
                if (!photoUrl.isNullOrEmpty()) {
                    Glide.with(this).load(photoUrl).placeholder(R.drawable.ic_launcher_foreground).into(ivProfile)
                }
                
                val lat = doc.getDouble("profilePictureLat")
                val lng = doc.getDouble("profilePictureLng")
                if (lat != null && lng != null) {
                    tvLocation.text = "Last photo taken at: $lat, $lng"
                }
            }
        }
    }

    private fun showDeleteConfirmDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to permanently delete your account?")
            .setPositiveButton("Delete") { _, _ ->
                FirebaseAuth.getInstance().currentUser?.delete()?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Account Deleted!", Toast.LENGTH_LONG).show()
                        startActivity(Intent(activity, LoginActivity::class.java))
                        activity?.finish()
                    } else {
                        Toast.makeText(context, "Error: \${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
