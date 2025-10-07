import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.healthconnectexercise.domain.model.ExerciseLog
import com.example.healthconnectexercise.domain.model.Source
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class ExerciseRepositoryImplTest {

    private fun createLog(
        id: String,
        type: String,
        startOffset: Long,
        endOffset: Long,
        source: Source
    ): ExerciseLog {
        val now = Instant.now().toEpochMilli()
        return ExerciseLog(
            id = id,
            type = type,
            start = now + startOffset,
            end = now + endOffset,
            calories = null,
            source = source
        )
    }

    private fun detectConflicts(logs: List<ExerciseLog>): List<Pair<ExerciseLog, ExerciseLog>> {
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

    @Test
    fun `manualvshealthconnectoverlappinglogsshouldconflict`() = runTest {
        val manual = createLog(
            id = "manual1",
            type = "Running",
            startOffset = -60_000L,  // started 1 min ago
            endOffset = +60_000L,    // ends 1 min from now
            source = Source.MANUAL
        )
        val health = createLog(
            id = "healthconnect1",
            type = "Running",
            startOffset = -90_000L,  // started earlier
            endOffset = -30_000L,    // ends within manual session
            source = Source.HEALTH_CONNECT
        )

        val conflicts = detectConflicts(listOf(manual, health))

        assertTrue("Conflict should be detected", conflicts.isNotEmpty())
        assertEquals("Expected 1 conflict pair", 1, conflicts.size)
        assertEquals("Running", conflicts.first().first.type)
    }

    @Test
    fun `differenttypesshouldnotconflict`() = runTest {
        val manual = createLog("manual2", "Running", -1000, 1000, Source.MANUAL)
        val health = createLog("healthconnect2", "Cycling", -1000, 1000, Source.HEALTH_CONNECT)

        val conflicts = detectConflicts(listOf(manual, health))

        assertTrue("No conflicts expected for different types", conflicts.isEmpty())
    }

    @Test
    fun `nonoverlappingtimesshouldnotconflict`() = runTest {
        val manual = createLog("manual3", "Running", -10_000L, -5_000L, Source.MANUAL)
        val health = createLog("healthconnect3", "Running", 0L, 5_000L, Source.HEALTH_CONNECT)

        val conflicts = detectConflicts(listOf(manual, health))

        assertTrue("No conflicts expected for non-overlapping sessions", conflicts.isEmpty())
    }

    @Test
    fun `multipleoverlapsshouldallbedetected`() = runTest {
        val keep = createLog("manual4", "Running", -60_000L, 60_000L, Source.MANUAL)
        val overlap1 = createLog("healthconnect4a", "Running", -90_000L, -30_000L, Source.HEALTH_CONNECT)
        val overlap2 = createLog("healthconnect4b", "Running", -10_000L, 10_000L, Source.HEALTH_CONNECT)

        val conflicts = detectConflicts(listOf(keep, overlap1, overlap2))

        assertEquals("Two conflicts expected", 2, conflicts.size)
        assertTrue(conflicts.all { it.first.type == "Running" })
    }
}