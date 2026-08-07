package com.nazmulislam.scanlern

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

class TimerFragment : Fragment(R.layout.fragment_timer) {

    private lateinit var tvTimerCountdown: TextView
    private lateinit var tvTimerMode: TextView
    private lateinit var tvSessionCount: TextView
    private lateinit var btnTimerStartPause: Button
    private lateinit var btnTimerReset: Button

    private var countDownTimer: CountDownTimer? = null
    private var isRunning = false
    private var isFocusMode = true
    private var sessionNumber = 1

    private val focusDuration = 25 * 60 * 1000L  // 25 minutes
    private val breakDuration = 5 * 60 * 1000L   // 5 minutes
    private var timeLeftInMillis = focusDuration

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvTimerCountdown = view.findViewById(R.id.tvTimerCountdown)
        tvTimerMode = view.findViewById(R.id.tvTimerMode)
        tvSessionCount = view.findViewById(R.id.tvSessionCount)
        btnTimerStartPause = view.findViewById(R.id.btnTimerStartPause)
        btnTimerReset = view.findViewById(R.id.btnTimerReset)

        updateDisplay()

        btnTimerStartPause.setOnClickListener {
            if (isRunning) {
                pauseTimer()
            } else {
                startTimer()
            }
        }

        btnTimerReset.setOnClickListener {
            resetTimer()
        }
    }

    private fun startTimer() {
        isRunning = true
        btnTimerStartPause.text = "Pause"

        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateDisplay()
            }

            override fun onFinish() {
                switchMode()
            }
        }.start()
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        isRunning = false
        btnTimerStartPause.text = "Resume"
    }

    private fun resetTimer() {
        countDownTimer?.cancel()
        isRunning = false
        isFocusMode = true
        sessionNumber = 1
        timeLeftInMillis = focusDuration
        btnTimerStartPause.text = "Start"
        tvTimerMode.text = "FOCUS TIME"
        updateDisplay()
        updateSessionCount()
    }

    private fun switchMode() {
        if (isFocusMode) {
            isFocusMode = false
            timeLeftInMillis = breakDuration
            tvTimerMode.text = "BREAK TIME"
        } else {
            isFocusMode = true
            sessionNumber++
            timeLeftInMillis = focusDuration
            tvTimerMode.text = "FOCUS TIME"
        }
        updateSessionCount()
        updateDisplay()
        startTimer()
    }

    private fun updateSessionCount() {
        tvSessionCount.text = "Session $sessionNumber of 4"
    }

    private fun updateDisplay() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        tvTimerCountdown.text = String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
    }
}