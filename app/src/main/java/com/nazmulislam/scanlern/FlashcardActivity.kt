package com.nazmulislam.scanlern

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class FlashcardActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnBackFlashcard: TextView

    private lateinit var tvCardCounter: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flashcard)

        recyclerView = findViewById(R.id.recyclerViewFlashcards)
        progressBar = findViewById(R.id.progressBar)
        btnBackFlashcard = findViewById(R.id.btnBackFlashcard)
        tvCardCounter = findViewById(R.id.tvCardCounter)

        recyclerView.layoutManager = LinearLayoutManager(this)

        val extractedText = intent.getStringExtra("EXTRACTED_TEXT") ?: ""

        generateFlashcards(extractedText)

        btnBackFlashcard.setOnClickListener {
            finish()
        }
    }

    private fun generateFlashcards(text: String) {
        progressBar.visibility = View.VISIBLE

        GroqHelper.generateFlashcards(
            inputText = text,
            onResult = { jsonResult ->
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    try {
                        // AI response-e majhe majhe extra text/markdown thakte pare, JSON part ta clean kori
                        val cleanJson = jsonResult
                            .substringAfter("[")
                            .substringBeforeLast("]")
                            .let { "[$it]" }

                        val type = object : TypeToken<List<Flashcard>>() {}.type
                        val flashcards: List<Flashcard> = Gson().fromJson(cleanJson, type)

                        if (flashcards.isEmpty()) {
                            Toast.makeText(this, "No flashcards generated", Toast.LENGTH_SHORT).show()
                        } else {
                            recyclerView.adapter = FlashcardAdapter(flashcards)
                            tvCardCounter.text = "Card 1 of ${flashcards.size}"
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this, "Failed to parse flashcards: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            },
            onError = { error ->
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                }
            }
        )
    }
}