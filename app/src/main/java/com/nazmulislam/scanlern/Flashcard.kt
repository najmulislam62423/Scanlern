package com.nazmulislam.scanlern

data class Flashcard(
    val id: String = "",
    val userId: String = "",
    val question: String = "",
    val answer: String = "",
    val stage: Int = 0,
    val nextReviewDate: Long = 0L,
    val createdAt: Long = 0L
)