package com.nazmulislam.scanlern

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class QuizActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var quizContentLayout: LinearLayout
    private lateinit var tvQuestion: TextView
    private lateinit var tvQuestionCounter: TextView
    private lateinit var optionsContainer: LinearLayout
    private lateinit var btnNextQuestion: Button
    private lateinit var btnCloseQuiz: TextView

    private var quizList: List<QuizQuestion> = emptyList()
    private var currentIndex = 0
    private var score = 0
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

        val extractedText = intent.getStringExtra("EXTRACTED_TEXT") ?: ""
        generateQuiz(extractedText)

        btnNextQuestion.setOnClickListener {
            currentIndex++
            if (currentIndex < quizList.size) {
                showQuestion()
            } else {
                showFinalScore()
            }
        }
    }

    private fun generateQuiz(text: String) {
        GroqHelper.generateQuiz(
            inputText = text,
            onResult = { jsonResult ->
                runOnUiThread {
                    try {
                        val cleanJson = jsonResult
                            .substringAfter("[")
                            .substringBeforeLast("]")
                            .let { "[$it]" }

                        val type = object : TypeToken<List<QuizQuestion>>() {}.type
                        quizList = Gson().fromJson(cleanJson, type)

                        progressBar.visibility = View.GONE
                        if (quizList.isEmpty()) {
                            Toast.makeText(this, "No quiz generated", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            quizContentLayout.visibility = View.VISIBLE
                            showQuestion()
                        }
                    } catch (e: Exception) {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this, "Failed to parse quiz: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            },
            onError = { error ->
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    private fun showQuestion() {
        answered = false
        btnNextQuestion.isEnabled = false
        btnNextQuestion.text = if (currentIndex == quizList.size - 1) "Finish Quiz" else "Next Question"

        val q = quizList[currentIndex]
        tvQuestion.text = q.question
        tvQuestionCounter.text = "Question ${currentIndex + 1} of ${quizList.size}"

        optionsContainer.removeAllViews()

        q.options.forEachIndexed { index, optionText ->
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
                    checkAnswer(index, q.correctAnswer, optionsContainer)
                    btnNextQuestion.isEnabled = true
                }
            }

            optionsContainer.addView(optionView)
        }
    }

    private fun checkAnswer(selectedIndex: Int, correctIndex: Int, container: LinearLayout) {
        for (i in 0 until container.childCount) {
            val child = container.getChildAt(i) as TextView
            when {
                i == correctIndex -> child.setBackgroundResource(R.drawable.bg_option_correct)
                i == selectedIndex -> child.setBackgroundResource(R.drawable.bg_option_wrong)
            }
        }

        if (selectedIndex == correctIndex) {
            score++
        }
    }

    private fun showFinalScore() {
        Toast.makeText(this, "Quiz complete! Score: $score/${quizList.size}", Toast.LENGTH_LONG).show()
        finish()
    }
}