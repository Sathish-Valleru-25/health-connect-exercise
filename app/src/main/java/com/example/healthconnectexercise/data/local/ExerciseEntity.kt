package com.example.healthconnectexercise.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.healthconnectexercise.domain.model.ExerciseLog
import com.example.healthconnectexercise.domain.model.Source

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey val id: String,
    val type: String,
    val start: Long,
    val end: Long,
    val calories: Int?,
    val source: String
) {
    fun toDomain() = ExerciseLog(
        id = id,
        type = type,
        start = start,
        end = end,
        calories = calories,
        source = Source.valueOf(source)
    )
}

fun ExerciseLog.toEntity() = ExerciseEntity(
    id = id,
    type = type,
    start = start,
    end = end,
    calories = calories,
    source = source.name
)