package com.nazmulislam.scanlern

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object StreakHelper {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun updateAndGetStreak(onResult: (Int) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val firestore = FirebaseFirestore.getInstance()
        val userDoc = firestore.collection("users").document(userId)

        val today = dateFormat.format(System.currentTimeMillis())

        userDoc.get().addOnSuccessListener { document ->
            val lastActiveDate = document.getString("lastActiveDate")
            val currentStreak = document.getLong("streakCount")?.toInt() ?: 0

            when {
                lastActiveDate == today -> {
                    // Already counted today
                    onResult(currentStreak.coerceAtLeast(1))
                }
                lastActiveDate == getYesterday() -> {
                    // Consecutive day, increase streak
                    val newStreak = currentStreak + 1
                    userDoc.set(mapOf("lastActiveDate" to today, "streakCount" to newStreak))
                    onResult(newStreak)
                }
                else -> {
                    // Missed a day or first time, reset to 1
                    userDoc.set(mapOf("lastActiveDate" to today, "streakCount" to 1))
                    onResult(1)
                }
            }
        }.addOnFailureListener {
            onResult(1)
        }
    }

    private fun getYesterday(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        return dateFormat.format(calendar.time)
    }
}