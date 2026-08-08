package com.nazmulislam.scanlern

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatHistoryActivity : AppCompatActivity() {

    private lateinit var layoutSessionList: LinearLayout
    private lateinit var tvEmptyState: TextView
    private lateinit var btnBack: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_history)

        layoutSessionList = findViewById(R.id.layoutSessionList)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        btnBack = findViewById(R.id.btnBack)

        btnBack.setOnClickListener { finish() }

        loadSessions()
    }

    private fun loadSessions() {
        ChatHelper.getSessions { sessions ->
            runOnUiThread {
                layoutSessionList.removeAllViews()

                if (sessions.isEmpty()) {
                    tvEmptyState.visibility = View.VISIBLE
                } else {
                    tvEmptyState.visibility = View.GONE
                    val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

                    for (session in sessions) {
                        val itemView = layoutInflater.inflate(R.layout.item_chat_session, layoutSessionList, false)
                        val tvTitle = itemView.findViewById<TextView>(R.id.tvSessionTitle)
                        val tvDate = itemView.findViewById<TextView>(R.id.tvSessionDate)

                        tvTitle.text = session.title
                        tvDate.text = dateFormat.format(Date(session.createdAt))

                        itemView.setOnClickListener {
                            val resultIntent = Intent()
                            resultIntent.putExtra("sessionId", session.id)
                            setResult(Activity.RESULT_OK, resultIntent)
                            finish()
                        }

                        layoutSessionList.addView(itemView)
                    }
                }
            }
        }
    }
}