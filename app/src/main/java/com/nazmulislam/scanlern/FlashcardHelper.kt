package com.nazmulislam.scanlern

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FlashcardHelper {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // stage অনুযায়ী কত দিন পর আবার দেখাবে (দিন হিসেবে)
    private val intervalsDays = listOf(1, 3, 7, 14, 30)

    fun saveFlashcards(flashcards: List<Flashcard>) {
        val userId = auth.currentUser?.uid ?: return
        val now = System.currentTimeMillis()

        for (card in flashcards) {
            val docRef = firestore.collection("flashcards").document()
            val cardWithMeta = card.copy(
                id = docRef.id,
                userId = userId,
                stage = 0,
                nextReviewDate = now,   // সাথে সাথেই review এর জন্য due থাকবে
                createdAt = now
            )
            docRef.set(cardWithMeta)
        }
    }

    fun getDueFlashcards(onResult: (List<Flashcard>) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onResult(emptyList())
            return
        }
        val now = System.currentTimeMillis()

        firestore.collection("flashcards")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val due = result.documents
                    .mapNotNull { it.toObject(Flashcard::class.java) }
                    .filter { it.nextReviewDate <= now }
                    .sortedBy { it.nextReviewDate }
                onResult(due)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    fun getDueCount(onResult: (Int) -> Unit) {
        getDueFlashcards { onResult(it.size) }
    }

    // rating: 0 = Hard, 1 = Medium, 2 = Easy
    fun updateAfterRating(card: Flashcard, rating: Int) {
        val newStage = when (rating) {
            0 -> 0
            2 -> (card.stage + 2).coerceAtMost(intervalsDays.size - 1)
            else -> (card.stage + 1).coerceAtMost(intervalsDays.size - 1)
        }
        val intervalDays = intervalsDays[newStage]
        val nextReview = System.currentTimeMillis() + (intervalDays * 24L * 60 * 60 * 1000)

        firestore.collection("flashcards").document(card.id)
            .update(mapOf("stage" to newStage, "nextReviewDate" to nextReview))
    }
}