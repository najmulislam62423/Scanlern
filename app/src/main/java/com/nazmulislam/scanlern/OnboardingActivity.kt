package com.nazmulislam.scanlern

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var dotsLayout: LinearLayout
    private lateinit var btnNext: Button
    private lateinit var prefs: SharedPreferences

    private val onboardingItems = listOf(
        OnboardingItem(
            "Scan your notes",
            "Use your camera to instantly scan handwritten or printed notes and convert them to text.",
            R.mipmap.ic_launcher
        ),
        OnboardingItem(
            "AI-powered summaries",
            "Let AI summarize your notes into simple, easy to understand points for quick revision.",
            R.mipmap.ic_launcher
        ),
        OnboardingItem(
            "Save and access anytime",
            "All your scanned notes are saved securely so you can access them anytime, anywhere.",
            R.mipmap.ic_launcher
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        prefs = getSharedPreferences("ScanlernPrefs", MODE_PRIVATE)

        viewPager = findViewById(R.id.viewPager)
        dotsLayout = findViewById(R.id.dotsLayout)
        btnNext = findViewById(R.id.btnNext)

        viewPager.adapter = OnboardingAdapter(onboardingItems)
        setupDots(0)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                setupDots(position)
                btnNext.text = if (position == onboardingItems.size - 1) "Get Started" else "Next"
            }
        })

        btnNext.setOnClickListener {
            val currentItem = viewPager.currentItem
            if (currentItem < onboardingItems.size - 1) {
                viewPager.currentItem = currentItem + 1
            } else {
                finishOnboarding()
            }
        }
    }

    private fun setupDots(position: Int) {
        dotsLayout.removeAllViews()
        for (i in onboardingItems.indices) {
            val dot = android.widget.TextView(this)
            dot.text = "•"
            dot.textSize = 24f
            dot.setTextColor(
                if (i == position) resources.getColor(R.color.primary, theme)
                else resources.getColor(R.color.text_secondary, theme)
            )
            dotsLayout.addView(dot)
        }
    }

    private fun finishOnboarding() {
        prefs.edit().putBoolean("onboarding_completed", true).apply()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}