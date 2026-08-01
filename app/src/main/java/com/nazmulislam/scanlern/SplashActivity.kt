package com.nazmulislam.scanlern

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser

            val intent = if (currentUser != null) {
                // User already logged in, direct Home-e pathao
                Intent(this, HomeActivity::class.java)
            } else {
                // Login screen-e pathao
                Intent(this, MainActivity::class.java)
            }

            startActivity(intent)
            finish()
        }, 2000) // 2000ms = 2 second
    }
}