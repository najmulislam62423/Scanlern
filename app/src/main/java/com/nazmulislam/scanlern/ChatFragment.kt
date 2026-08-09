package com.nazmulislam.scanlern

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.FrameLayout

class ChatFragment : Fragment(R.layout.fragment_chat) {

    private lateinit var recyclerViewChat: RecyclerView
    private lateinit var etChatInput: EditText
    private lateinit var btnSendChat: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnChatHistory: ImageView
    private lateinit var btnNewChat: FrameLayout

    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter

    private val historyLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val sessionId = result.data?.getStringExtra("sessionId")
            if (sessionId != null) {
                ChatSessionManager.currentSessionId = sessionId
                loadCurrentSession()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerViewChat = view.findViewById(R.id.recyclerViewChat)
        etChatInput = view.findViewById(R.id.etChatInput)
        btnSendChat = view.findViewById(R.id.btnSendChat)
        progressBar = view.findViewById(R.id.chatProgressBar)
        btnChatHistory = view.findViewById(R.id.btnChatHistory)
        btnNewChat = view.findViewById(R.id.btnNewChat)

        adapter = ChatAdapter(messages)
        recyclerViewChat.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewChat.adapter = adapter

        loadCurrentSession()

        btnSendChat.setOnClickListener {
            sendMessage()
        }

        btnChatHistory.setOnClickListener {
            historyLauncher.launch(Intent(requireContext(), ChatHistoryActivity::class.java))
        }

        btnNewChat.setOnClickListener {
            ChatSessionManager.currentSessionId = null
            showWelcomeMessage()
        }
    }

    private fun loadCurrentSession() {
        val sessionId = ChatSessionManager.currentSessionId

        if (sessionId == null) {
            showWelcomeMessage()
            return
        }

        ChatHelper.getMessagesForSession(sessionId) { history ->
            if (!isAdded) return@getMessagesForSession
            requireActivity().runOnUiThread {
                messages.clear()
                if (history.isEmpty()) {
                    showWelcomeMessage()
                } else {
                    messages.addAll(history)
                    adapter.notifyDataSetChanged()
                    recyclerViewChat.scrollToPosition(messages.size - 1)
                }
            }
        }
    }

    private fun showWelcomeMessage() {
        messages.clear()
        messages.add(
            ChatMessage(
                text = "Hi! Ask me anything about your studies — I'm here to help explain concepts, solve doubts, or clarify topics.",
                isUser = false
            )
        )
        adapter.notifyDataSetChanged()
    }

    private fun sendMessage() {
        val question = etChatInput.text.toString().trim()
        if (question.isEmpty()) return

        val userMessage = ChatMessage(text = question, isUser = true)
        adapter.addMessage(userMessage)
        recyclerViewChat.scrollToPosition(messages.size - 1)
        etChatInput.setText("")
        progressBar.visibility = View.VISIBLE

        val existingSessionId = ChatSessionManager.currentSessionId

        if (existingSessionId == null) {
            // এটাই এই session এর প্রথম message, তাই নতুন session তৈরি করো
            ChatHelper.createNewSession(
                firstMessageText = question,
                onResult = { newSessionId ->
                    ChatSessionManager.currentSessionId = newSessionId
                    ChatHelper.saveMessage(userMessage, newSessionId)
                    askAI(question, newSessionId)
                },
                onError = {
                    // session তৈরি না হলেও প্রশ্নের উত্তর তো দিতে হবে
                    askAI(question, "")
                }
            )
        } else {
            ChatHelper.saveMessage(userMessage, existingSessionId)
            askAI(question, existingSessionId)
        }
    }

    private fun askAI(question: String, sessionId: String) {
        GroqHelper.askQuestion(
            question = question,
            onResult = { answer ->
                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    val aiMessage = ChatMessage(text = answer, isUser = false)
                    adapter.addMessage(aiMessage)
                    if (sessionId.isNotEmpty()) {
                        ChatHelper.saveMessage(aiMessage, sessionId)
                    }
                    recyclerViewChat.scrollToPosition(messages.size - 1)
                }
            },
            onError = { error ->
                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    adapter.addMessage(ChatMessage(text = "Sorry, something went wrong: $error", isUser = false))
                    recyclerViewChat.scrollToPosition(messages.size - 1)
                }
            }
        )
    }
}