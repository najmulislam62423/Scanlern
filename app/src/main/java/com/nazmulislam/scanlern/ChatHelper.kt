package com.nazmulislam.scanlern

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object ChatHelper {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun createNewSession(
        firstMessageText: String,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onError("User not logged in")
            return
        }
        val docRef = firestore.collection("chat_sessions").document()
        val title = if (firstMessageText.length > 40) firstMessageText.take(40) + "..." else firstMessageText
        val session = ChatSession(
            id = docRef.id,
            userId = userId,
            title = title,
            createdAt = System.currentTimeMillis()
        )

        docRef.set(session)
            .addOnSuccessListener { onResult(docRef.id) }
            .addOnFailureListener { e -> onError(e.message ?: "Failed to create session") }
    }

    fun saveMessage(message: ChatMessage, sessionId: String, onError: (String) -> Unit = {}) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onError("User not logged in")
            return
        }
        val messageWithSession = message.copy(userId = userId, sessionId = sessionId)

        firestore.collection("chat_messages")
            .add(messageWithSession)
            .addOnFailureListener { e ->
                android.util.Log.e("ChatHelper", "Save failed: ${e.message}")
                onError(e.message ?: "Failed to save message")
            }
    }

    fun getSessions(onResult: (List<ChatSession>) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onResult(emptyList())
            return
        }
        firestore.collection("chat_sessions")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val sessions = result.documents
                    .mapNotNull { it.toObject(ChatSession::class.java) }
                    .sortedByDescending { it.createdAt }
                onResult(sessions)
            }
            .addOnFailureListener { e ->
                android.util.Log.e("ChatHelper", "Load sessions failed: ${e.message}")
                onResult(emptyList())
            }
    }

    fun getMessagesForSession(sessionId: String, onResult: (List<ChatMessage>) -> Unit) {
        firestore.collection("chat_messages")
            .whereEqualTo("sessionId", sessionId)
            .get()
            .addOnSuccessListener { result ->
                val messages = result.documents
                    .mapNotNull { it.toObject(ChatMessage::class.java) }
                    .sortedBy { it.timestamp }
                onResult(messages)
            }
            .addOnFailureListener { e ->
                android.util.Log.e("ChatHelper", "Load messages failed: ${e.message}")
                onResult(emptyList())
            }
    }
}