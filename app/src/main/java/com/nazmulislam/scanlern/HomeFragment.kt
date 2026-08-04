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

class HomeFragment : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()

        val tvUserEmail = view.findViewById<TextView>(R.id.tvUserEmail)
        val tvTotalNotes = view.findViewById<TextView>(R.id.tvTotalNotes)
        val tvStreakDays = view.findViewById<TextView>(R.id.tvStreakDays)
        val cardScan = view.findViewById<View>(R.id.cardScan)
        val cardHistory = view.findViewById<View>(R.id.cardHistory)
        val adView = view.findViewById<AdView>(R.id.adView)


        tvUserEmail.text = auth.currentUser?.email ?: ""

        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("notes")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { result ->
                    tvTotalNotes.text = result.size().toString()
                }
        }
        StreakHelper.updateAndGetStreak { streak ->
            tvStreakDays.text = streak.toString()
        }

        MobileAds.initialize(requireContext()) {}
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        cardScan.setOnClickListener {
            val intent = Intent(requireContext(), ScanActivity::class.java)
            startActivity(intent)
        }

        cardHistory.setOnClickListener {
            val bottomNav = requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNav)
            bottomNav.selectedItemId = R.id.nav_history
        }
    }
}