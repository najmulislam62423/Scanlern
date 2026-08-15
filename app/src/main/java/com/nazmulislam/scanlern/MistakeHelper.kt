package com.nazmulislam.scanlern

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object MistakeHelper {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun addMistake(question: QuizQuestion) {
        val userId = auth.currentUser?.uid ?: return

        // একই প্রশ্ন আগে থেকে থাকলে duplicate না বানিয়ে শুধু timestamp আপডেট করা
        firestore.collection("mistakes")
            .whereEqualTo("userId", userId)
            .whereEqualTo("question", question.question)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    val docRef = firestore.collection("mistakes").document()
                    val mistake = Mistake(
                        id = docRef.id,
                        userId = userId,
                        question = question.question,
                        options = question.options,
                        correctAnswer = question.correctAnswer,
                        timestamp = System.currentTimeMillis()
                    )
                    docRef.set(mistake)
                } else {
                    result.documents.first().reference.update("timestamp", System.currentTimeMillis())
                }
            }
    }

    fun removeMistakeByQuestionText(questionText: String) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("mistakes")
            .whereEqualTo("userId", userId)
            .whereEqualTo("question", questionText)
            .get()
            .addOnSuccessListener { result ->
                for (doc in result.documents) {
                    doc.reference.delete()
                }
            }
    }

    fun getAllMistakes(onResult: (List<Mistake>) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onResult(emptyList())
            return
        }
        firestore.collection("mistakes")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val mistakes = result.documents
                    .mapNotNull { it.toObject(Mistake::class.java) }
                    .sortedByDescending { it.timestamp }
                onResult(mistakes)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    fun getMistakeCount(onResult: (Int) -> Unit) {
        getAllMistakes { onResult(it.size) }
    }

    fun removeMistake(id: String) {
        firestore.collection("mistakes").document(id).delete()
    }
}