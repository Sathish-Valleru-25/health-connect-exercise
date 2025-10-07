package com.example.healthconnectexercise.domain.repository

import com.example.healthconnectexercise.domain.model.ExerciseLog

interface ExerciseRepository {
    suspend fun addManualLog(log: ExerciseLog)
    suspend fun getMergedLogs(): Pair<List<ExerciseLog>, List<Pair<ExerciseLog, ExerciseLog>>>
    suspend fun resolveConflict(keep: ExerciseLog, drop: ExerciseLog): Boolean
}