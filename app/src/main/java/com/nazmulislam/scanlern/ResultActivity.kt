package com.nazmulislam.scanlern

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ResultActivity : AppCompatActivity() {

    private lateinit var tvExtractedText: TextView
    private lateinit var btnSave: Button
    private lateinit var btnBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        tvExtractedText = findViewById(R.id.tvExtractedText)
        btnSave = findViewById(R.id.btnSave)
        btnBack = findViewById(R.id.btnBack)

        // ScanActivity theke pathano text receive kora
        val extractedText = intent.getStringExtra("EXTRACTED_TEXT") ?: "No text found"
        tvExtractedText.text = extractedText

        btnSave.setOnClickListener {
            // TODO: Firestore-e save korবো (porer feature)
            Toast.makeText(this, "Save feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        btnBack.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}