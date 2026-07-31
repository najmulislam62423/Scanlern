package com.nazmulislam.scanlern

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class ResultActivity : AppCompatActivity() {

    private lateinit var tvExtractedText: TextView
    private lateinit var btnSave: Button
    private lateinit var btnBack: Button
    private lateinit var btnSummarize: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        tvExtractedText = findViewById(R.id.tvExtractedText)
        btnSave = findViewById(R.id.btnSave)
        btnBack = findViewById(R.id.btnBack)
        btnSummarize = findViewById(R.id.btnSummarize)
        progressBar = findViewById(R.id.progressBar)

        val extractedText = intent.getStringExtra("EXTRACTED_TEXT") ?: "No text found"
        tvExtractedText.text = extractedText

        btnSummarize.setOnClickListener {
            val currentText = tvExtractedText.text.toString()

            progressBar.visibility = View.VISIBLE
            btnSummarize.isEnabled = false

            GeminiHelper.summarizeText(
                inputText = currentText,
                onResult = { summary ->
                    runOnUiThread {
                        progressBar.visibility = View.GONE
                        btnSummarize.isEnabled = true
                        tvExtractedText.text = summary
                        Toast.makeText(this, "Summary generated!", Toast.LENGTH_SHORT).show()
                    }
                },
                onError = { error ->
                    runOnUiThread {
                        progressBar.visibility = View.GONE
                        btnSummarize.isEnabled = true
                        androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle("Error Details")
                            .setMessage(error)
                            .setPositiveButton("OK", null)
                            .show()
                    }
                }
            )
        }

        btnSave.setOnClickListener {
            saveNoteToFirestore()
        }

        btnBack.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun saveNoteToFirestore() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show()
            return
        }

        val noteText = tvExtractedText.text.toString()
        if (noteText.isBlank()) {
            Toast.makeText(this, "Nothing to save", Toast.LENGTH_SHORT).show()
            return
        }

        btnSave.isEnabled = false

        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(System.currentTimeMillis())

        val note = hashMapOf(
            "text" to noteText,
            "timestamp" to timestamp,
            "userId" to userId
        )

        firestore.collection("notes")
            .add(note)
            .addOnSuccessListener {
                btnSave.isEnabled = true
                Toast.makeText(this, "Note saved successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                btnSave.isEnabled = true
                Toast.makeText(this, "Save failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}