package com.nazmulislam.scanlern

data class StudyTask(
    val day: Int = 0,
    val date: String = "",
    val topic: String = "",
    val task: String = "",
    val isCompleted: Boolean = false
)

data class StudyPlan(
    val id: String = "",
    val userId: String = "",
    val examTitle: String = "",
    val examDate: String = "",
    val createdAt: Long = 0L,
    val tasks: List<StudyTask> = emptyList()
)