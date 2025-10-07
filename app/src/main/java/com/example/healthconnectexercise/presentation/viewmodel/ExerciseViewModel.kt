package com.example.healthconnectexercise.presentation.viewmodel


import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthconnectexercise.data.healthimport.HealthConnectManager
import com.example.healthconnectexercise.domain.model.ExerciseLog
import com.example.healthconnectexercise.domain.model.Source
import com.example.healthconnectexercise.domain.repository.ExerciseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ExerciseViewModel @Inject constructor(
    private val repository: ExerciseRepository,
    val healthConnectManager: HealthConnectManager
) : ViewModel() {


    data class UiState(
        val logs: List<ExerciseLog> = emptyList(),
        val conflicts: List<Pair<ExerciseLog, ExerciseLog>> = emptyList(),
        val loading: Boolean = false,
        val error: String? = null
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state
    private val _showPermissionRationale = MutableStateFlow(false)
    val showPermissionRationale = _showPermissionRationale.asStateFlow()



    fun onSyncClick(
        permissionLauncher: ManagedActivityResultLauncher<Set<String>, Set<String>>
    ) {
        viewModelScope.launch {
            if (healthConnectManager.hasAllPermissions()) {
                // If permissions are already granted, just sync.
                load()
            } else {
                // If permissions are NOT granted, show the rationale dialog first.
                _showPermissionRationale.value = true
            }
        }
    }
    fun launchPermissionsRequest(
        permissionLauncher: ManagedActivityResultLauncher<Set<String>, Set<String>>
    ) {
        _showPermissionRationale.value = false // Hide the dialog
        permissionLauncher.launch(healthConnectManager.requiredPermissions) // Launch the actual system prompt
    }

    fun onPermissionRationaleDismissed() {
        _showPermissionRationale.value = false
    }

    fun load() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(loading = true) }

                val (merged, conflicts) = repository.getMergedLogs()

                val conflictedIds = conflicts.flatMap { listOf(it.first.id, it.second.id) }.toSet()

                _state.update {
                    it.copy(
                        logs = merged
                            .map { log ->
                                log.copy(conflicted = conflictedIds.contains(log.id))
                            }
                            .sortedByDescending { log -> log.start }, // 👈 Sort by latest added
                        conflicts = conflicts,
                        loading = false
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.message) }
            }
        }
    }

    fun addManual(type: String, start: Long, end: Long, calories: Int?) {
        viewModelScope.launch {
            try {
                val newLog = ExerciseLog(
                    id = UUID.randomUUID().toString(),
                    type = type,
                    start = start,
                    end = end,
                    calories = calories,
                    source = Source.MANUAL
                )

                repository.addManualLog(newLog)
                load()
            } catch (e: Exception) {
                _state.update { it.copy(error = "Failed to add log: ${e.message}") }
            }
        }
    }

    fun resolveConflict(keep: ExerciseLog, drop: ExerciseLog) {
        viewModelScope.launch {
            try {
                repository.resolveConflict(keep, drop)
                load()
            } catch (e: Exception) {
                _state.update { it.copy(error = "Conflict resolution failed: ${e.message}") }
            }
        }
    }

    fun keepAndRemoveOther(log: ExerciseLog) {
        viewModelScope.launch {
            val conflictPair = _state.value.conflicts.find {
                it.first.id == log.id || it.second.id == log.id
            }
            if (conflictPair != null) {
                val keep = log
                val drop = if (conflictPair.first.id == log.id)
                    conflictPair.second else conflictPair.first
                repository.resolveConflict(keep, drop)
                load()
            }
        }
    }
}
