package com.example.healthconnectexercise.domain.model

data class ExerciseLog(
    val id: String,
    val type: String,
    val start: Long,
    val end: Long,
    val calories: Int? = null,
    val source: Source,
    val conflicted: Boolean = false
)

enum class Source {
    MANUAL,
    HEALTH_CONNECT
}
