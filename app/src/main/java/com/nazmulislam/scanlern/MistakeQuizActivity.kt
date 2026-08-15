package com.nazmulislam.scanlern

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MistakeQuizActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var quizContentLayout: LinearLayout
    private lateinit var tvQuestion: TextView
    private lateinit var tvQuestionCounter: TextView
    private lateinit var optionsContainer: LinearLayout
    private lateinit var btnNextQuestion: Button
    private lateinit var btnCloseQuiz: TextView

    private var mistakesList: List<Mistake> = emptyList()
    private var currentIndex = 0
    private var answered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        progressBar = findViewById(R.id.progressBar)
        quizContentLayout = findViewById(R.id.quizContentLayout)
        tvQuestion = findViewById(R.id.tvQuestion)
        tvQuestionCounter = findViewById(R.id.tvQuestionCounter)
        optionsContainer = findViewById(R.id.optionsContainer)
        btnNextQuestion = findViewById(R.id.btnNextQuestion)
        btnCloseQuiz = findViewById(R.id.btnCloseQuiz)

        btnCloseQuiz.setOnClickListener { finish() }

        loadMistakes()

        btnNextQuestion.setOnClickListener {
            currentIndex++
            if (currentIndex < mistakesList.size) {
                showQuestion()
            } else {
                Toast.makeText(this, "Revision complete!", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun loadMistakes() {
        progressBar.visibility = View.VISIBLE
        MistakeHelper.getAllMistakes { mistakes ->
            runOnUiThread {
                progressBar.visibility = View.GONE
                mistakesList = mistakes
                if (mistakesList.isEmpty()) {
                    Toast.makeText(this, "No mistakes to revise! 🎉", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    quizContentLayout.visibility = View.VISIBLE
                    showQuestion()
                }
            }
        }
    }

    private fun showQuestion() {
        answered = false
        btnNextQuestion.isEnabled = false
        btnNextQuestion.text = if (currentIndex == mistakesList.size - 1) "Finish" else "Next Question"

        val m = mistakesList[currentIndex]
        tvQuestion.text = m.question
        tvQuestionCounter.text = "Mistake ${currentIndex + 1} of ${mistakesList.size}"

        optionsContainer.removeAllViews()

        m.options.forEachIndexed { index, optionText ->
            val optionView = TextView(this)
            optionView.text = optionText
            optionView.textSize = 15f
            optionView.setTextColor(resources.getColor(R.color.text_primary, theme))
            optionView.setPadding(40, 32, 40, 32)
            optionView.setBackgroundResource(R.drawable.bg_option_default)

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.bottomMargin = 24
            optionView.layoutParams = params

            optionView.setOnClickListener {
                if (!answered) {
                    answered = true
                    checkAnswer(index, m.correctAnswer, optionsContainer, m)
                    btnNextQuestion.isEnabled = true
                }
            }

            optionsContainer.addView(optionView)
        }
    }

    private fun checkAnswer(selectedIndex: Int, correctIndex: Int, container: LinearLayout, mistake: Mistake) {
        for (i in 0 until container.childCount) {
            val child = container.getChildAt(i) as TextView
            when {
                i == correctIndex -> child.setBackgroundResource(R.drawable.bg_option_correct)
                i == selectedIndex -> child.setBackgroundResource(R.drawable.bg_option_wrong)
            }
        }

        if (selectedIndex == correctIndex) {
            MistakeHelper.removeMistake(mistake.id)
        }
    }
}