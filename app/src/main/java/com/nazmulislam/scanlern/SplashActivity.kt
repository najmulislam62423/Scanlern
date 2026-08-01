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
            val prefs = getSharedPreferences("ScanlernPrefs", MODE_PRIVATE)
            val onboardingCompleted = prefs.getBoolean("onboarding_completed", false)

            val intent = if (!onboardingCompleted) {
                // Prothom bar, Onboarding dekhao
                Intent(this, OnboardingActivity::class.java)
            } else {
                val auth = FirebaseAuth.getInstance()
                val currentUser = auth.currentUser

                if (currentUser != null) {
                    Intent(this, HomeActivity::class.java)
                } else {
                    Intent(this, MainActivity::class.java)
                }
            }

            startActivity(intent)
            finish()
        }, 2000)
    }
}