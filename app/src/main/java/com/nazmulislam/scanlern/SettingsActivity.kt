package com.nazmulislam.scanlern

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var tvSettingsEmail: TextView
    private lateinit var btnChangePassword: Button
    private lateinit var btnDeleteAccount: Button
    private lateinit var btnBackSettings: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        auth = FirebaseAuth.getInstance()

        tvSettingsEmail = findViewById(R.id.tvSettingsEmail)
        btnChangePassword = findViewById(R.id.btnChangePassword)
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount)
        btnBackSettings = findViewById(R.id.btnBackSettings)

        val currentUser = auth.currentUser
        tvSettingsEmail.text = currentUser?.email ?: "Unknown"

        btnChangePassword.setOnClickListener {
            sendPasswordResetEmail()
        }

        btnDeleteAccount.setOnClickListener {
            showDeleteAccountConfirmation()
        }

        btnBackSettings.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun sendPasswordResetEmail() {
        val email = auth.currentUser?.email
        if (email == null) {
            Toast.makeText(this, "No email found", Toast.LENGTH_SHORT).show()
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                Toast.makeText(this, "Password reset email sent to $email", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showDeleteAccountConfirmation() {
        AlertDialog.Builder(this)
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
                Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            ?.addOnFailureListener { e ->
                Toast.makeText(this, "Failed: ${e.message}. You may need to re-login before deleting.", Toast.LENGTH_LONG).show()
            }
    }
}