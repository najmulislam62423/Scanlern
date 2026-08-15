package com.nazmulislam.scanlern

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.fragment.app.Fragment
import android.widget.ProgressBar

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var tvGoalProgress: TextView
    private lateinit var goalProgressBar: ProgressBar
    private lateinit var tvStudyPlanContent: TextView
    private lateinit var cardWeakTopics: View
    private lateinit var tvWeakTopicsContent: TextView
    private lateinit var cardReviewDue: View
    private lateinit var tvReviewDueCount: TextView
    private lateinit var cardMistakes: View
    private lateinit var tvMistakeCount: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        val userId = auth.currentUser?.uid

        val tvUserEmail = view.findViewById<TextView>(R.id.tvUserEmail)
        val tvTotalNotes = view.findViewById<TextView>(R.id.tvTotalNotes)
        tvGoalProgress = view.findViewById(R.id.tvGoalProgress)
        goalProgressBar = view.findViewById(R.id.goalProgressBar)
        val tvWeeklyMinutes = view.findViewById<TextView>(R.id.tvWeeklyMinutes)
        val tvWeeklyNotes = view.findViewById<TextView>(R.id.tvWeeklyNotes)
        val tvStreakDays = view.findViewById<TextView>(R.id.tvStreakDays)
        val cardScan = view.findViewById<View>(R.id.cardScan)
        val cardHistory = view.findViewById<View>(R.id.cardHistory)
        val adView = view.findViewById<AdView>(R.id.adView)
        val cardStudyPlan = view.findViewById<View>(R.id.cardStudyPlan)
        tvStudyPlanContent = view.findViewById(R.id.tvStudyPlanContent)
        cardWeakTopics = view.findViewById(R.id.cardWeakTopics)
        tvWeakTopicsContent = view.findViewById(R.id.tvWeakTopicsContent)
        cardReviewDue = view.findViewById(R.id.cardReviewDue)
        cardMistakes = view.findViewById(R.id.cardMistakes)
        tvMistakeCount = view.findViewById(R.id.tvMistakeCount)
        tvReviewDueCount = view.findViewById(R.id.tvReviewDueCount)

        tvUserEmail.text = auth.currentUser?.email ?: ""

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

        // ---- Ads ----
        MobileAds.initialize(requireContext()) {}
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        // ---- Click listeners ----
        cardScan.setOnClickListener {
            val intent = Intent(requireContext(), ScanActivity::class.java)
            startActivity(intent)
        }

        cardHistory.setOnClickListener {
            val bottomNav = requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNav)
            bottomNav.selectedItemId = R.id.nav_history
        }

        cardStudyPlan.setOnClickListener {
            val intent = Intent(requireContext(), StudyPlanActivity::class.java)
            startActivity(intent)
        }

        cardReviewDue.setOnClickListener {
            startActivity(Intent(requireContext(), ReviewFlashcardsActivity::class.java))
        }
        cardMistakes.setOnClickListener {
            startActivity(Intent(requireContext(), MistakeQuizActivity::class.java))
        }

        // Home screen এ প্রথমবার আসার সময়ও data load করো
        refreshDynamicData()
    }

    override fun onResume() {
        super.onResume()
        // প্রতিবার Home tab এ ফিরে আসলে সর্বশেষ data দেখানোর জন্য
        refreshDynamicData()
    }

    private fun refreshDynamicData() {
        // ---- Today's study goal ----
        StudyGoalHelper.getTodayMinutes { minutes ->
            val goal = StudyGoalHelper.DAILY_GOAL_MINUTES
            val displayMinutes = minutes.coerceAtMost(goal)
            tvGoalProgress.text = "$minutes/$goal min"
            goalProgressBar.progress = ((displayMinutes.toFloat() / goal) * 100).toInt()
        }

        // ---- Weekly minutes ----
        StudyGoalHelper.getWeeklyMinutes { totalMinutes ->
            view?.findViewById<TextView>(R.id.tvWeeklyMinutes)?.text = totalMinutes.toString()
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

        // ---- Weak Topics Insight ----
        QuizResultHelper.getInsight { insightText ->
            if (!insightText.isNullOrEmpty()) {
                cardWeakTopics.visibility = View.VISIBLE
                tvWeakTopicsContent.text = insightText
            }
        }

        // ---- Flashcards Due for Review ----
        FlashcardHelper.getDueCount { count ->
            if (count > 0) {
                cardReviewDue.visibility = View.VISIBLE
                tvReviewDueCount.text = "$count flashcard${if (count == 1) "" else "s"} to review"
            } else {
                cardReviewDue.visibility = View.GONE
            }
        }
        // ---- Mistake Journal ----
        MistakeHelper.getMistakeCount { count ->
            if (count > 0) {
                cardMistakes.visibility = View.VISIBLE
                tvMistakeCount.text = "$count mistake${if (count == 1) "" else "s"} to revise"
            } else {
                cardMistakes.visibility = View.GONE
            }
        }
    }
}