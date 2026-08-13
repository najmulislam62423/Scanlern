package com.nazmulislam.scanlern

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.FrameLayout
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.content.FileProvider
import java.io.File

class ChatFragment : Fragment(R.layout.fragment_chat) {

    private lateinit var recyclerViewChat: RecyclerView
    private lateinit var etChatInput: EditText
    private lateinit var btnSendChat: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnChatHistory: ImageView
    private lateinit var btnNewChat: FrameLayout
    private lateinit var btnAttachFile: TextView
    private lateinit var layoutAttachedFile: View
    private lateinit var tvAttachedFileName: TextView
    private lateinit var btnRemoveAttachment: TextView

    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter

    // এই session এ attach করা document/image এর extracted text
    private var attachedContext: String? = null
    private var attachedFileName: String? = null
    private var cameraPhotoUri: Uri? = null

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

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { handleImageAttachment(it, "Image") }
    }

    private val pdfLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { handlePdfAttachment(it) }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraPhotoUri != null) {
            handleImageAttachment(cameraPhotoUri!!, "Photo")
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
        btnAttachFile = view.findViewById(R.id.btnAttachFile)
        layoutAttachedFile = view.findViewById(R.id.layoutAttachedFile)
        tvAttachedFileName = view.findViewById(R.id.tvAttachedFileName)
        btnRemoveAttachment = view.findViewById(R.id.btnRemoveAttachment)

        adapter = ChatAdapter(messages)
        recyclerViewChat.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewChat.adapter = adapter

        loadCurrentSession()

        btnSendChat.setOnClickListener { sendMessage() }

        btnChatHistory.setOnClickListener {
            historyLauncher.launch(Intent(requireContext(), ChatHistoryActivity::class.java))
        }

        btnNewChat.setOnClickListener {
            ChatSessionManager.currentSessionId = null
            clearAttachment()
            showWelcomeMessage()
        }

        btnAttachFile.setOnClickListener {
            showAttachOptions()
        }

        btnRemoveAttachment.setOnClickListener {
            clearAttachment()
        }
    }

    private fun showAttachOptions() {
        val options = arrayOf("📷 Take Photo", "🖼️ Choose Image", "📄 Choose PDF")
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Attach a file")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> launchCamera()
                    1 -> galleryLauncher.launch("image/*")
                    2 -> pdfLauncher.launch("application/pdf")
                }
            }
            .show()
    }

    private fun launchCamera() {
        val photoFile = File(requireContext().cacheDir, "chat_photo_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            requireContext(), "${requireContext().packageName}.fileprovider", photoFile
        )
        cameraPhotoUri = uri
        cameraLauncher.launch(uri)
    }

    private fun handleImageAttachment(uri: Uri, label: String) {
        progressBar.visibility = View.VISIBLE
        DocumentTextExtractor.extractFromImage(
            context = requireContext(),
            uri = uri,
            onResult = { text ->
                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    if (text.isBlank()) {
                        Toast.makeText(requireContext(), "No text found in image", Toast.LENGTH_SHORT).show()
                    } else {
                        setAttachment(text, label)
                    }
                }
            },
            onError = { error ->
                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Failed: $error", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun handlePdfAttachment(uri: Uri) {
        progressBar.visibility = View.VISIBLE
        DocumentTextExtractor.extractFromPdf(
            context = requireContext(),
            uri = uri,
            onProgress = { current, total ->
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Reading page $current of $total...", Toast.LENGTH_SHORT).show()
                }
            },
            onResult = { text ->
                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    if (text.isBlank()) {
                        Toast.makeText(requireContext(), "No text found in PDF", Toast.LENGTH_SHORT).show()
                    } else {
                        setAttachment(text, "PDF Document")
                    }
                }
            },
            onError = { error ->
                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Failed: $error", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun setAttachment(text: String, name: String) {
        attachedContext = text
        attachedFileName = name
        tvAttachedFileName.text = "📎 $name attached — ask your question"
        layoutAttachedFile.visibility = View.VISIBLE
    }

    private fun clearAttachment() {
        attachedContext = null
        attachedFileName = null
        layoutAttachedFile.visibility = View.GONE
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
                text = "Hi! Ask me anything about your studies — I'm here to help explain concepts, solve doubts, or clarify topics. You can also attach a photo or PDF using 📎.",
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
            ChatHelper.createNewSession(
                firstMessageText = question,
                onResult = { newSessionId ->
                    ChatSessionManager.currentSessionId = newSessionId
                    ChatHelper.saveMessage(userMessage, newSessionId)
                    askAI(question, newSessionId)
                },
                onError = { askAI(question, "") }
            )
        } else {
            ChatHelper.saveMessage(userMessage, existingSessionId)
            askAI(question, existingSessionId)
        }
    }

    private fun askAI(question: String, sessionId: String) {
        val context = attachedContext

        val onResultCallback: (String) -> Unit = { answer ->
            requireActivity().runOnUiThread {
                progressBar.visibility = View.GONE
                val aiMessage = ChatMessage(text = answer, isUser = false)
                adapter.addMessage(aiMessage)
                if (sessionId.isNotEmpty()) {
                    ChatHelper.saveMessage(aiMessage, sessionId)
                }
                recyclerViewChat.scrollToPosition(messages.size - 1)
            }
        }

        val onErrorCallback: (String) -> Unit = { error ->
            requireActivity().runOnUiThread {
                progressBar.visibility = View.GONE
                adapter.addMessage(ChatMessage(text = "Sorry, something went wrong: $error", isUser = false))
                recyclerViewChat.scrollToPosition(messages.size - 1)
            }
        }


        if (context != null) {
            GroqHelper.askQuestionWithContext(question, context, onResultCallback, onErrorCallback)
        } else {
            GroqHelper.askQuestion(question, onResultCallback, onErrorCallback)
        }
    }
}