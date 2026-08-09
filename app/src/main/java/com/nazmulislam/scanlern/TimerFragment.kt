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
    private lateinit var tvTimerStatus: TextView
    private lateinit var tvSessionCount: TextView
    private lateinit var btnTimerStartPause: Button
    private lateinit var btnTimerReset: Button
    private lateinit var circularTimerView: CircularTimerView

    private lateinit var sessionDots: List<View>

    private var countDownTimer: CountDownTimer? = null
    private var isRunning = false
    private var isFocusMode = true
    private var sessionNumber = 1

    private val focusDuration = 25 * 60 * 1000L  // 25 minutes
    private val breakDuration = 5 * 60 * 1000L   // 5 minutes
    private var totalDurationForCurrentMode = focusDuration
    private var timeLeftInMillis = focusDuration

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvTimerCountdown = view.findViewById(R.id.tvTimerCountdown)
        tvTimerMode = view.findViewById(R.id.tvTimerMode)
        tvTimerStatus = view.findViewById(R.id.tvTimerStatus)
        tvSessionCount = view.findViewById(R.id.tvSessionCount)
        btnTimerStartPause = view.findViewById(R.id.btnTimerStartPause)
        btnTimerReset = view.findViewById(R.id.btnTimerReset)
        circularTimerView = view.findViewById(R.id.circularTimerView)

        sessionDots = listOf(
            view.findViewById(R.id.dot1),
            view.findViewById(R.id.dot2),
            view.findViewById(R.id.dot3),
            view.findViewById(R.id.dot4)
        )

        updateDisplay()
        updateSessionDots()

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
        btnTimerStartPause.text = "⏸  Pause"
        tvTimerStatus.text = if (isFocusMode) "Stay focused..." else "Take a breather"

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
        btnTimerStartPause.text = "▶  Resume"
        tvTimerStatus.text = "Paused"
    }

    private fun resetTimer() {
        countDownTimer?.cancel()
        isRunning = false
        isFocusMode = true
        sessionNumber = 1
        totalDurationForCurrentMode = focusDuration
        timeLeftInMillis = focusDuration
        btnTimerStartPause.text = "▶  Start"
        tvTimerMode.text = "FOCUS TIME"
        tvTimerStatus.text = "Ready to focus"
        circularTimerView.setProgressColor(R.color.primary)
        updateDisplay()
        updateSessionCount()
        updateSessionDots()
    }

    private fun switchMode() {
        if (isFocusMode) {
            StudyGoalHelper.addStudyMinutes(25)
            isFocusMode = false
            totalDurationForCurrentMode = breakDuration
            timeLeftInMillis = breakDuration
            tvTimerMode.text = "BREAK TIME"
            circularTimerView.setProgressColor(R.color.accent_success)
        } else {
            isFocusMode = true
            sessionNumber++
            totalDurationForCurrentMode = focusDuration
            timeLeftInMillis = focusDuration
            tvTimerMode.text = "FOCUS TIME"
            circularTimerView.setProgressColor(R.color.primary)
        }
        updateSessionCount()
        updateSessionDots()
        updateDisplay()
        startTimer()
    }

    private fun updateSessionCount() {
        tvSessionCount.text = "Session $sessionNumber of 4"
    }

    private fun updateSessionDots() {
        val completedSessions = (sessionNumber - 1).coerceIn(0, sessionDots.size)
        sessionDots.forEachIndexed { index, dot ->
            dot.setBackgroundResource(
                if (index < completedSessions) R.drawable.circle_button_bg
                else R.drawable.circle_button_border
            )
        }
    }

    private fun updateDisplay() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        tvTimerCountdown.text = String.format("%02d:%02d", minutes, seconds)

        val fraction = timeLeftInMillis.toFloat() / totalDurationForCurrentMode.toFloat()
        circularTimerView.setProgress(fraction)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
    }
}