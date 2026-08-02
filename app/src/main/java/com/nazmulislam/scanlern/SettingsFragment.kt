package com.nazmulislam.scanlern

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val tvSettingsEmail = view.findViewById<TextView>(R.id.tvSettingsEmail)
        val btnChangePassword = view.findViewById<Button>(R.id.btnChangePassword)
        val btnDeleteAccount = view.findViewById<Button>(R.id.btnDeleteAccount)
        val btnLogoutSettings = view.findViewById<Button>(R.id.btnLogoutSettings)

        tvSettingsEmail.text = auth.currentUser?.email ?: "Unknown"

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
}