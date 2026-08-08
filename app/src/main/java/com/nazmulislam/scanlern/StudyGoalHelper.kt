package com.nazmulislam.scanlern

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

object StudyGoalHelper {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    const val DAILY_GOAL_MINUTES = 25

    fun addStudyMinutes(minutes: Int) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val firestore = FirebaseFirestore.getInstance()
        val today = dateFormat.format(System.currentTimeMillis())

        val docRef = firestore.collection("users").document(userId)
            .collection("studyLog").document(today)

        docRef.get().addOnSuccessListener { document ->
            val currentMinutes = document.getLong("minutes")?.toInt() ?: 0
            val newTotal = currentMinutes + minutes
            docRef.set(mapOf("minutes" to newTotal, "date" to today))
        }
    }

    fun getTodayMinutes(onResult: (Int) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val firestore = FirebaseFirestore.getInstance()
        val today = dateFormat.format(System.currentTimeMillis())

        firestore.collection("users").document(userId)
            .collection("studyLog").document(today)
            .get()
            .addOnSuccessListener { document ->
                val minutes = document.getLong("minutes")?.toInt() ?: 0
                onResult(minutes)
            }
            .addOnFailureListener {
                onResult(0)
            }
    }
    fun getWeeklyMinutes(onResult: (Int) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val firestore = FirebaseFirestore.getInstance()
        val calendar = java.util.Calendar.getInstance()

        var totalMinutes = 0
        var daysChecked = 0
        val totalDays = 7

        for (i in 0 until totalDays) {
            val dateStr = dateFormat.format(calendar.time)
            firestore.collection("users").document(userId)
                .collection("studyLog").document(dateStr)
                .get()
                .addOnSuccessListener { document ->
                    totalMinutes += document.getLong("minutes")?.toInt() ?: 0
                    daysChecked++
                    if (daysChecked == totalDays) {
                        onResult(totalMinutes)
                    }
                }
                .addOnFailureListener {
                    daysChecked++
                    if (daysChecked == totalDays) {
                        onResult(totalMinutes)
                    }
                }
            calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
        }
    }
}