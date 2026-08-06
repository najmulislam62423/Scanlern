package com.nazmulislam.scanlern

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.ImageView

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val tvSettingsEmail = view.findViewById<TextView>(R.id.tvSettingsEmail)
        val btnChangePassword = view.findViewById<View>(R.id.btnChangePassword)
        val btnDeleteAccount = view.findViewById<View>(R.id.btnDeleteAccount)
        val btnLogoutSettings = view.findViewById<View>(R.id.btnLogoutSettings)

        tvSettingsEmail.text = auth.currentUser?.email ?: "Unknown"
        val tvSettingsName = view.findViewById<TextView>(R.id.tvSettingsName)
        val ivSettingsPhoto = view.findViewById<ImageView>(R.id.ivSettingsPhoto)

        loadProfileInfo(tvSettingsName, ivSettingsPhoto)
        val profileCard = view.findViewById<View>(R.id.profileCard)
        profileCard.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, ProfileFragment())
                .addToBackStack(null)
                .commit()
        }


        btnChangePassword.setOnClickListener {
            sendPasswordResetEmail()
        }

        btnDeleteAccount.setOnClickListener {
            showDeleteAccountConfirmation()
        }

        btnLogoutSettings.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun sendPasswordResetEmail() {
        val email = auth.currentUser?.email
        if (email == null) {
            Toast.makeText(requireContext(), "No email found", Toast.LENGTH_SHORT).show()
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Password reset email sent to $email", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showDeleteAccountConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Account")
            .setMessage("This will permanently delete your account. This action cannot be undone. Are you sure?")
            .setPositiveButton("Delete") { _, _ ->
                deleteAccount()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteAccount() {
        val user = auth.currentUser
        user?.delete()
            ?.addOnSuccessListener {
                Toast.makeText(requireContext(), "Account deleted", Toast.LENGTH_SHORT).show()
                val intent = Intent(requireContext(), MainActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
            ?.addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed: ${e.message}. You may need to re-login before deleting.", Toast.LENGTH_LONG).show()
            }
    }
    private fun loadProfileInfo(tvName: TextView, ivPhoto: ImageView) {
        val userId = auth.currentUser?.uid ?: return
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val name = document.getString("name")
                if (!name.isNullOrEmpty()) {
                    tvName.text = name
                } else {
                    tvName.text = "Welcome!"
                }

                val photoBase64 = document.getString("photoBase64")
                if (!photoBase64.isNullOrEmpty()) {
                    try {
                        val decodedBytes = Base64.decode(photoBase64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        ivPhoto.setImageBitmap(bitmap)
                    } catch (e: Exception) {
                        // ignore
                    }
                }
            }
    }
}