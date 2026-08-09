package com.nazmulislam.scanlern

data class QuizAnswerRecord(
    val question: String = "",
    val wasCorrect: Boolean = false
)

data class QuizResult(
    val id: String = "",
    val userId: String = "",
    val score: Int = 0,
    val total: Int = 0,
    val timestamp: Long = 0L,
    val answers: List<QuizAnswerRecord> = emptyList()
)