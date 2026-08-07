package com.nazmulislam.scanlern

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ChatFragment : Fragment(R.layout.fragment_chat) {

    private lateinit var recyclerViewChat: RecyclerView
    private lateinit var etChatInput: EditText
    private lateinit var btnSendChat: TextView
    private lateinit var progressBar: ProgressBar

    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerViewChat = view.findViewById(R.id.recyclerViewChat)
        etChatInput = view.findViewById(R.id.etChatInput)
        btnSendChat = view.findViewById(R.id.btnSendChat)
        progressBar = view.findViewById(R.id.chatProgressBar)

        adapter = ChatAdapter(messages)
        recyclerViewChat.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewChat.adapter = adapter

        messages.add(ChatMessage("Hi! Ask me anything about your studies — I'm here to help explain concepts, solve doubts, or clarify topics.", isUser = false))
        adapter.notifyItemInserted(0)

        btnSendChat.setOnClickListener {
            sendMessage()
        }
    }

    private fun sendMessage() {
        val question = etChatInput.text.toString().trim()
        if (question.isEmpty()) return

        adapter.addMessage(ChatMessage(question, isUser = true))
        recyclerViewChat.scrollToPosition(messages.size - 1)
        etChatInput.setText("")

        progressBar.visibility = View.VISIBLE

        GroqHelper.askQuestion(
            question = question,
            onResult = { answer ->
                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    adapter.addMessage(ChatMessage(answer, isUser = false))
                    recyclerViewChat.scrollToPosition(messages.size - 1)
                }
            },
            onError = { error ->
                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    adapter.addMessage(ChatMessage("Sorry, something went wrong: $error", isUser = false))
                    recyclerViewChat.scrollToPosition(messages.size - 1)
                }
            }
        )
    }
}