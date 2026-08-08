package com.nazmulislam.scanlern

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Calendar

object StudyPlanHelper {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun savePlan(plan: StudyPlan, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onError("User not logged in")
            return
        }
        val docRef = firestore.collection("study_plans").document()
        val planWithId = plan.copy(id = docRef.id, userId = userId)

        docRef.set(planWithId)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Failed to save plan") }
    }

    fun getActivePlan(onResult: (StudyPlan?) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onResult(null)
            return
        }
        // শুধু userId দিয়ে filter করছি (index লাগবে না),
        // sorting আমরা নিজেরাই code এ করে নিচ্ছি
        firestore.collection("study_plans")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val plans = result.documents.mapNotNull { it.toObject(StudyPlan::class.java) }
                val latestPlan = plans.maxByOrNull { it.createdAt }
                onResult(latestPlan)
            }
            .addOnFailureListener { e ->
                android.util.Log.e("StudyPlanHelper", "Failed to fetch plan: ${e.message}")
                onResult(null)
            }
    }

    // আজকে কোন দিনের task দেখাতে হবে সেটা বের করে
    fun getTodayDayNumber(plan: StudyPlan): Int {
        val createdCal = Calendar.getInstance()
        createdCal.timeInMillis = plan.createdAt

        val today = Calendar.getInstance()

        val diffDays = ((today.timeInMillis - createdCal.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
        return diffDays + 1  // Day 1, Day 2, ...
    }

    fun markTaskCompleted(plan: StudyPlan, dayNumber: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val updatedTasks = plan.tasks.map { task ->
            if (task.day == dayNumber) task.copy(isCompleted = true) else task
        }
        firestore.collection("study_plans").document(plan.id)
            .update("tasks", updatedTasks)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Failed to update task") }
    }
}