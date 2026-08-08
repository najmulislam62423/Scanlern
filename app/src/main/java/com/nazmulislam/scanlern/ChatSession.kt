package com.nazmulislam.scanlern

data class ChatSession(
    val id: String = "",
    val userId: String = "",
    val title: String = "New Chat",
    val createdAt: Long = 0L
)