package com.nazmulislam.scanlern

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var etSignupEmail: EditText
    private lateinit var etSignupPassword: EditText
    private lateinit var etSignupConfirmPassword: EditText
    private lateinit var btnSignup: Button
    private lateinit var tvBackToLogin: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()

        etSignupEmail = findViewById(R.id.etSignupEmail)
        etSignupPassword = findViewById(R.id.etSignupPassword)
        etSignupConfirmPassword = findViewById(R.id.etSignupConfirmPassword)
        btnSignup = findViewById(R.id.btnSignup)
        tvBackToLogin = findViewById(R.id.tvBackToLogin)
        progressBar = findViewById(R.id.signupProgressBar)

        btnSignup.setOnClickListener {
            attemptSignup()
        }

        tvBackToLogin.setOnClickListener {
            finish()  // MainActivity (Login) এ ফিরে যাবে
        }
    }

    private fun attemptSignup() {
        val email = etSignupEmail.text.toString().trim()
        val password = etSignupPassword.text.toString().trim()
        val confirmPassword = etSignupConfirmPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "সব ঘর পূরণ করো", Toast.LENGTH_SHORT).show()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "সঠিক email দাও", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password কমপক্ষে ৬ অক্ষরের হতে হবে", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Password মিলছে না", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = android.view.View.VISIBLE
        btnSignup.isEnabled = false

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                progressBar.visibility = android.view.View.GONE
                Toast.makeText(this, "Account তৈরি হয়েছে! Welcome to Scanlern", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                progressBar.visibility = android.view.View.GONE
                btnSignup.isEnabled = true
                Toast.makeText(this, "Signup failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}