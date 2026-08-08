package com.nazmulislam.scanlern

data class ChatMessage(
    val text: String = "",
    val isUser: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String = "",
    val sessionId: String = ""
)