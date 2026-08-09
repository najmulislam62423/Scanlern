package com.nazmulislam.scanlern

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object QuizResultHelper {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun saveQuizResult(score: Int, total: Int, answers: List<QuizAnswerRecord>, onComplete: () -> Unit = {}) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onComplete()
            return
        }

        val result = QuizResult(
            userId = userId,
            score = score,
            total = total,
            timestamp = System.currentTimeMillis(),
            answers = answers
        )

        firestore.collection("quiz_results")
            .add(result)
            .addOnSuccessListener {
                onComplete()
            }
            .addOnFailureListener { e ->
                android.util.Log.e("QuizResultHelper", "Save failed: ${e.message}")
                onComplete()   // save fail করলেও UI আটকে থাকবে না
            }
    }

    // সাম্প্রতিক কিছু quiz result থেকে সব ভুল হওয়া প্রশ্ন জোগাড় করে
    fun getRecentWrongAnswers(onResult: (List<String>) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onResult(emptyList())
            return
        }

        firestore.collection("quiz_results")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val allResults = result.documents
                    .mapNotNull { it.toObject(QuizResult::class.java) }
                    .sortedByDescending { it.timestamp }
                    .take(5)  // সবচেয়ে সাম্প্রতিক ৫টা quiz

                val wrongQuestions = allResults
                    .flatMap { it.answers }
                    .filter { !it.wasCorrect }
                    .map { it.question }

                onResult(wrongQuestions)
            }
            .addOnFailureListener { e ->
                android.util.Log.e("QuizResultHelper", "Load failed: ${e.message}")
                onResult(emptyList())
            }
    }

    // AI বিশ্লেষণ (insight) cache করে রাখার জন্য
    fun saveInsight(insightText: String) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("insights").document(userId)
            .set(mapOf("weakTopicsText" to insightText, "updatedAt" to System.currentTimeMillis()))
            .addOnFailureListener { e ->
                android.util.Log.e("QuizResultHelper", "Save insight failed: ${e.message}")
            }
    }

    fun getInsight(onResult: (String?) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onResult(null)
            return
        }
        firestore.collection("insights").document(userId).get()
            .addOnSuccessListener { doc ->
                onResult(doc.getString("weakTopicsText"))
            }
            .addOnFailureListener {
                onResult(null)
            }
    }
}