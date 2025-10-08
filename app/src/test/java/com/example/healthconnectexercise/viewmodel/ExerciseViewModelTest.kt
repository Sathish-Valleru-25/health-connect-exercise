package com.example.healthconnectexercise.presentation.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.compose.foundation.layout.size
import com.example.healthconnectexercise.data.healthimport.HealthConnectManager
import com.example.healthconnectexercise.domain.model.ExerciseLog
import com.example.healthconnectexercise.domain.model.Source
import com.example.healthconnectexercise.domain.repository.ExerciseRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.UUID

@ExperimentalCoroutinesApi
class ExerciseViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockRepository: ExerciseRepository
    private lateinit var mockHealthConnectManager: HealthConnectManager
    private lateinit var viewModel: ExerciseViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = mockk(relaxed = true)
        mockHealthConnectManager = mockk(relaxed = true)
        viewModel = ExerciseViewModel(mockRepository, mockHealthConnectManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `load() success - updates state with logs and conflicts`() = runTest {
        val logs = listOf(createFakeLog(source = Source.MANUAL))
        val conflicts = listOf(createFakeLog(source = Source.MANUAL) to createFakeLog(source = Source.HEALTH_CONNECT))
        coEvery { mockRepository.getMergedLogs() } returns (logs to conflicts)
        viewModel.load()
        testDispatcher.scheduler.advanceUntilIdle()
        val uiState = viewModel.state.first()
        assertEquals(logs.size, uiState.logs.size)
        assertEquals(conflicts.size, uiState.conflicts.size) // Changed "ui" to "uiState"
        assertFalse(uiState.loading)
    }

    @Test
    fun `load() failure - updates state with error`() = runTest {
        val errorMessage = "Database error"
        coEvery { mockRepository.getMergedLogs() } throws Exception(errorMessage)
        viewModel.load()
        testDispatcher.scheduler.advanceUntilIdle()
        val uiState = viewModel.state.first()
        assertEquals(errorMessage, uiState.error)
        assertFalse(uiState.loading)
        assertTrue(uiState.logs.isEmpty())
    }

    @Test
    fun `addManual() - calls repository and reloads data`() = runTest {
        viewModel.addManual("Running", 0L, 1L, 100)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify { mockRepository.addManualLog(any()) }
        coVerify { mockRepository.getMergedLogs() }
    }

    @Test
    fun `resolveConflict() - calls repository and reloads data`() = runTest {
        val keepLog = createFakeLog(source = Source.MANUAL)
        val dropLog = createFakeLog(source = Source.HEALTH_CONNECT)
        coEvery { mockRepository.resolveConflict(keepLog, dropLog) } returns true
        viewModel.resolveConflict(keepLog, dropLog)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify { mockRepository.resolveConflict(keep = keepLog, drop = dropLog) }
        coVerify { mockRepository.getMergedLogs() }
    }

    @Test
    fun `onSyncClick with permissions - triggers load()`() = runTest {
        coEvery { mockHealthConnectManager.hasAllPermissions() } returns true
        viewModel.onSyncClick() // Pass a dummy launcher
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify { mockRepository.getMergedLogs() }
    }

    @Test
    fun `onSyncClick without permissions - shows rationale`() = runTest {
        coEvery { mockHealthConnectManager.hasAllPermissions() } returns false
        viewModel.onSyncClick()
        testDispatcher.scheduler.advanceUntilIdle()
        val showRationale = viewModel.showPermissionRationale.first()
        assertTrue(showRationale)
    }
    private fun createFakeLog(source: Source): ExerciseLog {
        return ExerciseLog(
            id = UUID.randomUUID().toString(),
            type = "Running",
            start = System.currentTimeMillis(),
            end = System.currentTimeMillis() + 1000,
            calories = 100,
            source = source,
            conflicted = false
        )
    }
}
