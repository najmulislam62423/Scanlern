package com.nazmulislam.scanlern

data class Note(
    val id: String = "",
    val text: String = "",
    val timestamp: String = "",
    val userId: String = "",
    val category: String = "General",
    val isPinned: Boolean = false
)