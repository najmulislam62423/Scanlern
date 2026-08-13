package com.nazmulislam.scanlern

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ReviewFlashcardsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: android.widget.ProgressBar
    private lateinit var tvCardCounter: android.widget.TextView
    private lateinit var emptyStateLayout: View
    private lateinit var btnBack: View

    private val dueCards = mutableListOf<Flashcard>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review_flashcards)

        recyclerView = findViewById(R.id.recyclerViewReview)
        progressBar = findViewById(R.id.progressBar)
        tvCardCounter = findViewById(R.id.tvCardCounter)
        emptyStateLayout = findViewById(R.id.emptyStateLayout)
        btnBack = findViewById(R.id.btnBack)

        recyclerView.layoutManager = LinearLayoutManager(this)

        btnBack.setOnClickListener { finish() }

        loadDueCards()
    }

    private fun loadDueCards() {
        progressBar.visibility = View.VISIBLE
        FlashcardHelper.getDueFlashcards { cards ->
            runOnUiThread {
                progressBar.visibility = View.GONE
                dueCards.clear()
                dueCards.addAll(cards)
                setupAdapter()
                updateCounter()
            }
        }
    }

    private fun setupAdapter() {
        if (dueCards.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyStateLayout.visibility = View.VISIBLE
            return
        }
        recyclerView.visibility = View.VISIBLE
        emptyStateLayout.visibility = View.GONE

        recyclerView.adapter = FlashcardAdapter(
            flashcards = dueCards,
            showRatingButtons = true,
            onRate = { card, rating ->
                FlashcardHelper.updateAfterRating(card, rating)
                dueCards.remove(card)
                recyclerView.adapter?.notifyDataSetChanged()
                updateCounter()
                if (dueCards.isEmpty()) {
                    recyclerView.visibility = View.GONE
                    emptyStateLayout.visibility = View.VISIBLE
                }
            }
        )
    }

    private fun updateCounter() {
        tvCardCounter.text = "${dueCards.size} card${if (dueCards.size == 1) "" else "s"} to review"
    }
}