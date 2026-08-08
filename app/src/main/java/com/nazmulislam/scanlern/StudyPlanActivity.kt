package com.nazmulislam.scanlern

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray

class StudyPlanActivity : AppCompatActivity() {

    private lateinit var etExamTitle: EditText
    private lateinit var etDays: EditText
    private lateinit var etTopics: EditText
    private lateinit var btnGeneratePlan: View
    private lateinit var progressBar: ProgressBar
    private lateinit var layoutInputForm: LinearLayout
    private lateinit var layoutPlanResult: LinearLayout
    private lateinit var tvPlanTitle: TextView
    private lateinit var layoutTaskList: LinearLayout
    private lateinit var btnBack: View

    private lateinit var btnNewPlan: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study_plan)

        etExamTitle = findViewById(R.id.etExamTitle)
        etDays = findViewById(R.id.etDays)
        etTopics = findViewById(R.id.etTopics)
        btnGeneratePlan = findViewById(R.id.btnGeneratePlan)
        progressBar = findViewById(R.id.progressBar)
        layoutInputForm = findViewById(R.id.layoutInputForm)
        layoutPlanResult = findViewById(R.id.layoutPlanResult)
        tvPlanTitle = findViewById(R.id.tvPlanTitle)
        layoutTaskList = findViewById(R.id.layoutTaskList)
        btnBack = findViewById(R.id.btnBack)
        btnNewPlan = findViewById(R.id.btnNewPlan)

        btnBack.setOnClickListener { finish() }

        btnNewPlan.setOnClickListener {
            // form খালি করে আবার input screen দেখাও
            etExamTitle.text.clear()
            etDays.text.clear()
            etTopics.text.clear()
            layoutPlanResult.visibility = View.GONE
            layoutInputForm.visibility = View.VISIBLE
        }

        btnGeneratePlan.setOnClickListener {
            generatePlan()
        }

        // যদি আগে থেকেই কোনো active plan থাকে, সেটা দেখাও
        StudyPlanHelper.getActivePlan { plan ->
            if (plan != null) {
                showPlan(plan)
            }
        }
    }

    private fun generatePlan() {
        val examTitle = etExamTitle.text.toString().trim()
        val daysText = etDays.text.toString().trim()
        val topics = etTopics.text.toString().trim()

        if (examTitle.isEmpty() || daysText.isEmpty() || topics.isEmpty()) {
            Toast.makeText(this, "সব ঘর পূরণ করো", Toast.LENGTH_SHORT).show()
            return
        }

        val days = daysText.toIntOrNull()
        if (days == null || days <= 0 || days > 60) {
            Toast.makeText(this, "সঠিক দিন সংখ্যা দাও (1-60)", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        btnGeneratePlan.isEnabled = false

        GroqHelper.generateStudyPlan(
            examTitle = examTitle,
            daysUntilExam = days,
            topics = topics,
            onResult = { jsonResult ->
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    btnGeneratePlan.isEnabled = true
                    parseAndSavePlan(examTitle, jsonResult)
                }
            },
            onError = { error ->
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    btnGeneratePlan.isEnabled = true
                    Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    private fun parseAndSavePlan(examTitle: String, jsonResult: String) {
        try {
            // AI অনেক সময় markdown code block দিয়ে ঘিরে দেয়, সেটা পরিষ্কার করা
            val cleanJson = jsonResult
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val jsonArray = JSONArray(cleanJson)
            val tasks = mutableListOf<StudyTask>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                tasks.add(
                    StudyTask(
                        day = obj.getInt("day"),
                        topic = obj.getString("topic"),
                        task = obj.getString("task")
                    )
                )
            }

            val plan = StudyPlan(
                examTitle = examTitle,
                createdAt = System.currentTimeMillis(),
                tasks = tasks
            )

            StudyPlanHelper.savePlan(
                plan = plan,
                onSuccess = {
                    Toast.makeText(this, "Study plan তৈরি হয়েছে!", Toast.LENGTH_SHORT).show()
                    showPlan(plan)
                },
                onError = { error ->
                    Toast.makeText(this, "Save failed: $error", Toast.LENGTH_LONG).show()
                }
            )
        } catch (e: Exception) {
            Toast.makeText(this, "Plan তৈরি করতে সমস্যা হয়েছে, আবার চেষ্টা করো", Toast.LENGTH_LONG).show()
        }
    }

    private fun showPlan(plan: StudyPlan) {
        layoutInputForm.visibility = View.GONE
        layoutPlanResult.visibility = View.VISIBLE
        tvPlanTitle.text = plan.examTitle

        layoutTaskList.removeAllViews()

        for (task in plan.tasks) {
            val taskView = layoutInflater.inflate(R.layout.item_study_task, layoutTaskList, false)
            val tvDay = taskView.findViewById<TextView>(R.id.tvTaskDay)
            val tvTopic = taskView.findViewById<TextView>(R.id.tvTaskTopic)
            val tvTaskDesc = taskView.findViewById<TextView>(R.id.tvTaskDesc)

            tvDay.text = "Day ${task.day}"
            tvTopic.text = task.topic
            tvTaskDesc.text = task.task

            if (task.isCompleted) {
                taskView.alpha = 0.5f
            }

            layoutTaskList.addView(taskView)
        }
    }
}