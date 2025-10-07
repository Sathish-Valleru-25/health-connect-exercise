package com.example.healthconnectexercise.data.repository

import android.os.RemoteException
import com.example.healthconnectexercise.domain.model.ExerciseLog
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.example.healthconnectexercise.data.healthimport.HealthConnectManager
import com.example.healthconnectexercise.data.local.ExerciseDao
import com.example.healthconnectexercise.data.local.toEntity
import com.example.healthconnectexercise.domain.model.Source
import com.example.healthconnectexercise.domain.repository.ExerciseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZonedDateTime
import javax.inject.Inject

class ExerciseRepositoryImpl @Inject constructor(
    private val dao: ExerciseDao,
    private val healthConnectClient: HealthConnectClient,
    private val healthConnectManager: HealthConnectManager
) : ExerciseRepository {

    /**
     ** Add Manual Logs
     */
    override suspend fun addManualLog(log: ExerciseLog) = withContext(Dispatchers.IO) {
        dao.insert(log.toEntity())
    }

    private val ignoredHealthConnectIds = mutableSetOf<String>()


    /**
     ** Get Merged Logs both HealthConnect and Manual
     */
    override suspend fun getMergedLogs(): Pair<List<ExerciseLog>, List<Pair<ExerciseLog, ExerciseLog>>> =
        withContext(Dispatchers.IO) {
            val manualLogs = dao.getAll().map { it.toDomain() }

            val healthLogs: List<ExerciseLog> = try {
                if (healthConnectManager.hasAllPermissions()) {
                    readHealthConnectLogs()
                        .filterNot { ignoredHealthConnectIds.contains(it.id) }
                } else {
                    emptyList()
                }
            } catch (e: RemoteException) {
                emptyList()
            } catch (e: SecurityException) {
                emptyList()
            }
            val mergedList = manualLogs + healthLogs
            val conflicts = detectConflicts(mergedList)
            mergedList to conflicts.filterNot { isConflictResolved(it.first, it.second) }

        }

    private val resolvedConflicts = mutableSetOf<Pair<String, String>>()


    /**
     ** Resolve Conflicts
     */

    override suspend fun resolveConflict(keep: ExerciseLog, drop: ExerciseLog): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val normalizedType = keep.type.trim().lowercase()
                var manualLogs = dao.getAll().map { it.toDomain() }
                var healthLogs =
                    readHealthConnectLogs().filterNot { ignoredHealthConnectIds.contains(it.id) }
                var allLogs = manualLogs + healthLogs

                var overlapsFound: Boolean
                do {
                    val overlappingLogs = allLogs.filter { log ->
                        log.id != keep.id &&
                                log.type.trim().lowercase() == normalizedType &&
                                log.start < keep.end && log.end > keep.start &&
                                (log.source == Source.MANUAL || log.source == Source.HEALTH_CONNECT)
                    }

                    overlapsFound = overlappingLogs.isNotEmpty()

                    overlappingLogs.forEach { logToDrop ->
                        when (logToDrop.source) {
                            Source.MANUAL -> dao.delete(logToDrop.toEntity())
                            Source.HEALTH_CONNECT -> ignoredHealthConnectIds.add(logToDrop.id)
                        }
                        resolvedConflicts.add(keep.id to logToDrop.id)
                    }

                    manualLogs = dao.getAll().map { it.toDomain() }
                    healthLogs =
                        readHealthConnectLogs().filterNot { ignoredHealthConnectIds.contains(it.id) }
                    allLogs = manualLogs + healthLogs

                } while (overlapsFound)

                true
            } catch (e: Exception) {
                false
            }
        }
    private fun isConflictResolved(a: ExerciseLog, b: ExerciseLog): Boolean {
        return resolvedConflicts.contains(a.id to b.id) || resolvedConflicts.contains(b.id to a.id)
    }
    /**
     ** Get HealthConnect Logs
     */
    private suspend fun readHealthConnectLogs(): List<ExerciseLog> = withContext(Dispatchers.IO) {
        val thirtyDaysAgo = ZonedDateTime.now().minusDays(30)
        val timeRangeFilter = TimeRangeFilter.between(
            startTime = thirtyDaysAgo.toInstant(),
            endTime = Instant.now()
        )

        val request = ReadRecordsRequest(
            recordType = ExerciseSessionRecord::class,
            timeRangeFilter = timeRangeFilter
        )

        val response = healthConnectClient.readRecords(request)
        response.records.map { record: ExerciseSessionRecord ->
            ExerciseLog(
                id = record.metadata.id,
                type = ExerciseTypeMapper.toName(record.exerciseType),
                start = record.startTime.toEpochMilli(),
                end = record.endTime.toEpochMilli(),
                calories = null,
                source = Source.HEALTH_CONNECT
            )
        }
    }
    fun detectConflicts(logs: List<ExerciseLog>): List<Pair<ExerciseLog, ExerciseLog>> {
        val conflicts = mutableListOf<Pair<ExerciseLog, ExerciseLog>>()

        for (i in logs.indices) {
            for (j in i + 1 until logs.size) {
                val a = logs[i]
                val b = logs[j]

                val sameType = a.type.trim().equals(b.type.trim(), ignoreCase = true)
                val overlaps = a.start < b.end && a.end > b.start
                val validSources =
                    (a.source == Source.MANUAL || b.source == Source.MANUAL)
                if (sameType && overlaps && validSources) {
                    conflicts += a to b
                }
            }
        }

        return conflicts
    }
}