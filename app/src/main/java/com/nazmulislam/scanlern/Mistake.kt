package com.nazmulislam.scanlern

data class Mistake(
    val id: String = "",
    val userId: String = "",
    val question: String = "",
    val options: List<String> = emptyList(),
    val correctAnswer: Int = 0,
    val timestamp: Long = 0L
)