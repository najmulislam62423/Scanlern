package com.nazmulislam.scanlern

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var tvUserEmail: TextView
    private lateinit var btnScan: Button
    private lateinit var btnHistory: Button
    private lateinit var btnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()

        tvUserEmail = findViewById(R.id.tvUserEmail)
        btnScan = findViewById(R.id.btnScan)
        btnHistory = findViewById(R.id.btnHistory)
        btnLogout = findViewById(R.id.btnLogout)

        // Current logged-in user-er email dekhano
        val currentUser = auth.currentUser
        tvUserEmail.text = "Logged in as: ${currentUser?.email}"

        // Scan button - porer step e OCR feature e niye jabe
        btnScan.setOnClickListener {
            // TODO: OCR screen e navigate korbo (porer feature)
        }

        // History button - porer step e history screen e niye jabe
        btnHistory.setOnClickListener {
            // TODO: History screen e navigate korbo (porer feature)
        }

        // Logout button
        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}