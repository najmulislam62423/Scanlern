package com.nazmulislam.scanlern

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.ProgressBar

class HomeFragment : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        val userId = auth.currentUser?.uid   // ✅ সবার আগে declare

        val tvUserEmail = view.findViewById<TextView>(R.id.tvUserEmail)
        val tvTotalNotes = view.findViewById<TextView>(R.id.tvTotalNotes)
        val tvGoalProgress = view.findViewById<TextView>(R.id.tvGoalProgress)
        val goalProgressBar = view.findViewById<ProgressBar>(R.id.goalProgressBar)
        val tvWeeklyMinutes = view.findViewById<TextView>(R.id.tvWeeklyMinutes)
        val tvWeeklyNotes = view.findViewById<TextView>(R.id.tvWeeklyNotes)
        val tvStreakDays = view.findViewById<TextView>(R.id.tvStreakDays)
        val cardScan = view.findViewById<View>(R.id.cardScan)
        val cardHistory = view.findViewById<View>(R.id.cardHistory)
        val adView = view.findViewById<AdView>(R.id.adView)
        val cardStudyPlan = view.findViewById<View>(R.id.cardStudyPlan)
        val tvStudyPlanContent = view.findViewById<TextView>(R.id.tvStudyPlanContent)

        tvUserEmail.text = auth.currentUser?.email ?: ""

        // ---- Today's study goal ----
        StudyGoalHelper.getTodayMinutes { minutes ->
            val goal = StudyGoalHelper.DAILY_GOAL_MINUTES
            val displayMinutes = minutes.coerceAtMost(goal)
            tvGoalProgress.text = "$minutes/$goal min"
            goalProgressBar.progress = ((displayMinutes.toFloat() / goal) * 100).toInt()
        }

        // ---- Weekly minutes ----
        StudyGoalHelper.getWeeklyMinutes { totalMinutes ->
            tvWeeklyMinutes.text = totalMinutes.toString()
        }

        // ---- Total notes ----
        if (userId != null) {
            firestore.collection("notes")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { result ->
                    tvTotalNotes.text = result.size().toString()
                }
        }

        // ---- Weekly notes (last 7 days) ----
        if (userId != null) {
            val weekAgo = java.util.Calendar.getInstance()
            weekAgo.add(java.util.Calendar.DAY_OF_YEAR, -7)

            firestore.collection("notes")
                .whereEqualTo("userId", userId)
                .whereGreaterThan("createdAt", weekAgo.time)
                .get()
                .addOnSuccessListener { result ->
                    tvWeeklyNotes.text = result.size().toString()
                }
        }

        // ---- Streak ----
        StreakHelper.updateAndGetStreak { streak ->
            tvStreakDays.text = streak.toString()
        }

        // ---- Today's Study Plan ----
        StudyPlanHelper.getActivePlan { plan ->
            if (plan == null) {
                tvStudyPlanContent.text = "No active study plan. Tap to create one!"
            } else {
                val todayDay = StudyPlanHelper.getTodayDayNumber(plan)
                val todayTask = plan.tasks.find { it.day == todayDay }

                if (todayTask != null) {
                    tvStudyPlanContent.text = "${todayTask.topic} — ${todayTask.task}"
                } else if (todayDay > plan.tasks.size) {
                    tvStudyPlanContent.text = "Plan completed! Tap to create a new one."
                } else {
                    tvStudyPlanContent.text = "Tap to view your study plan"
                }
            }
        }

        // ---- Ads ----
        MobileAds.initialize(requireContext()) {}
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        // ---- Click listeners ----
        cardScan.setOnClickListener {
            val intent = Intent(requireContext(), ScanActivity::class.java)
            startActivity(intent)
        }
        cardStudyPlan.setOnClickListener {
            val intent = Intent(requireContext(), StudyPlanActivity::class.java)
            startActivity(intent)
        }

        cardHistory.setOnClickListener {
            val bottomNav = requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNav)
            bottomNav.selectedItemId = R.id.nav_history
        }
    }
}