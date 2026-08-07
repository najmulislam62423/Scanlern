package com.nazmulislam.scanlern

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var step1Layout: LinearLayout
    private lateinit var step2Layout: LinearLayout
    private lateinit var etForgotEmail: EditText
    private lateinit var etResetCode: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var btnSendCode: Button
    private lateinit var btnConfirmReset: Button
    private lateinit var tvInstructions: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        auth = FirebaseAuth.getInstance()

        val btnCloseForgot = findViewById<TextView>(R.id.btnCloseForgot)
        step1Layout = findViewById(R.id.step1Layout)
        step2Layout = findViewById(R.id.step2Layout)
        etForgotEmail = findViewById(R.id.etForgotEmail)
        etResetCode = findViewById(R.id.etResetCode)
        etNewPassword = findViewById(R.id.etNewPassword)
        btnSendCode = findViewById(R.id.btnSendCode)
        btnConfirmReset = findViewById(R.id.btnConfirmReset)
        tvInstructions = findViewById(R.id.tvInstructions)

        btnCloseForgot.setOnClickListener { finish() }

        btnSendCode.setOnClickListener {
            sendResetCode()
        }

        btnConfirmReset.setOnClickListener {
            confirmReset()
        }
    }

    private fun sendResetCode() {
        val email = etForgotEmail.text.toString().trim()
        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            return
        }

        btnSendCode.isEnabled = false

        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                btnSendCode.isEnabled = true
                Toast.makeText(this, "Code sent! Check your email (and spam folder).", Toast.LENGTH_LONG).show()

                step1Layout.visibility = View.GONE
                step2Layout.visibility = View.VISIBLE
                tvInstructions.text = "Open the email, copy the code from the reset link, paste it below with your new password."
            }
            .addOnFailureListener { e ->
                btnSendCode.isEnabled = true
                Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun confirmReset() {
        val code = etResetCode.text.toString().trim()
        val newPassword = etNewPassword.text.toString().trim()

        if (code.isEmpty()) {
            Toast.makeText(this, "Please enter the reset code", Toast.LENGTH_SHORT).show()
            return
        }
        if (newPassword.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        btnConfirmReset.isEnabled = false

        auth.confirmPasswordReset(code, newPassword)
            .addOnSuccessListener {
                Toast.makeText(this, "Password reset successful! Please login.", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener { e ->
                btnConfirmReset.isEnabled = true
                Toast.makeText(this, "Invalid or expired code: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}