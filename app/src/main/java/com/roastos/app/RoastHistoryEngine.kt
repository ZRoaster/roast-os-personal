package com.roastos.app

data class RoastHistoryEntry(
    val batchId: String,
    val createdAtMillis: Long,
    val title: String,

    val process: String,
    val density: Double,
    val moisture: Double,
    val aw: Double,

    val envTemp: Double,
    val envRh: Double,

    val predictedTurningSec: Int?,
    val predictedYellowSec: Int?,
    val predictedFcSec: Int?,
    val predictedDropSec: Int?,

    val actualTurningSec: Int?,
    val actualYellowSec: Int?,
    val actualFcSec: Int?,
    val actualDropSec: Int?,
    val actualPreFcRor: Double?,

    val batchStatus: String,
    val reportText: String,
    val diagnosisText: String,
    val correctionText: String
)

data class RoastHistorySaveResult(
    val saved: Boolean,
    val replacedExisting: Boolean,
    val totalCount: Int,
    val message: String
)

data class RoastHistoryDeleteResult(
    val deleted: Boolean,
    val totalCount: Int,
    val message: String
)

object RoastHistoryEngine {

    private const val MAX_HISTORY_COUNT = 200

    private val entries = mutableListOf<RoastHistoryEntry>()

    fun all(): List<RoastHistoryEntry> {
        return entries.sortedByDescending { it.createdAtMillis }
    }

    fun count(): Int {
        return entries.size
    }

    fun latest(): RoastHistoryEntry? {
        return entries.maxByOrNull { it.createdAtMillis }
    }

    fun findByBatchId(batchId: String): RoastHistoryEntry? {
        return entries.firstOrNull { it.batchId == batchId }
    }

    fun exists(batchId: String): Boolean {
        return entries.any { it.batchId == batchId }
    }

    fun saveCurrentState(): RoastHistorySaveResult {
        val report = RoastReportEngine.buildFromCurrentState()
        val diagnosis = RoastDeviationEngine.diagnoseFromCurrentState()
        val correction = RoastCorrectionBridge.buildFromCurrentState()

        val plannerInput = AppState.lastPlannerInput
        val timeline = RoastTimelineStore.current
        val session = BatchSessionEngine.current()

        val batchId = session?.batchId ?: "NO-BATCH-${System.currentTimeMillis()}"
        val createdAtMillis = System.currentTimeMillis()

        val entry = RoastHistoryEntry(
            batchId = batchId,
            createdAtMillis = createdAtMillis,
            title = report.title,

            process = plannerInput?.process ?: "",
            density = plannerInput?.density ?: 0.0,
            moisture = plannerInput?.moisture ?: 0.0,
            aw = plannerInput?.aw ?: 0.0,

            envTemp = plannerInput?.envTemp ?: 0.0,
            envRh = plannerInput?.envRH ?: 0.0,

            predictedTurningSec = timeline.predicted.turningSec,
            predictedYellowSec = timeline.predicted.yellowSec,
            predictedFcSec = timeline.predicted.fcSec,
            predictedDropSec = timeline.predicted.dropSec,

            actualTurningSec = timeline.actual.turningSec,
            actualYellowSec = timeline.actual.yellowSec,
            actualFcSec = timeline.actual.fcSec,
            actualDropSec = timeline.actual.dropSec,
            actualPreFcRor = AppState.liveActualPreFcRor,

            batchStatus = session?.status ?: "Idle",
            reportText = report.summary,
            diagnosisText = diagnosis.summary,
            correctionText = correction.summary
        )

        val existingIndex = entries.indexOfFirst { it.batchId == batchId }
        val replacedExisting = existingIndex >= 0

        if (existingIndex >= 0) {
            entries[existingIndex] = entry
        } else {
            entries.add(entry)
        }

        trimToMaxSize()

        return RoastHistorySaveResult(
            saved = true,
            replacedExisting = replacedExisting,
            totalCount = entries.size,
            message = if (replacedExisting) {
                "History updated for $batchId"
            } else {
                "History saved for $batchId"
            }
        )
    }

    fun delete(batchId: String): RoastHistoryDeleteResult {
        val removed = entries.removeAll { it.batchId == batchId }

        return RoastHistoryDeleteResult(
            deleted = removed,
            totalCount = entries.size,
            message = if (removed) {
                "Deleted history for $batchId"
            } else {
                "No history found for $batchId"
            }
        )
    }

    fun clear(): RoastHistoryDeleteResult {
        entries.clear()
        return RoastHistoryDeleteResult(
            deleted = true,
            totalCount = 0,
            message = "All roast history cleared"
        )
    }

    fun summary(): String {
        if (entries.isEmpty()) {
            return """
Roast History

Count
0

Latest
-

Status
Empty
            """.trimIndent()
        }

        val latest = latest()

        return """
Roast History

Count
${entries.size}

Latest Batch
${latest?.batchId ?: "-"}

Latest Process
${latest?.process ?: "-"}

Latest Status
${latest?.batchStatus ?: "-"}
        """.trimIndent()
    }

    private fun trimToMaxSize() {
        if (entries.size <= MAX_HISTORY_COUNT) return

        val sorted = entries.sortedByDescending { it.createdAtMillis }
        val trimmed = sorted.take(MAX_HISTORY_COUNT)

        entries.clear()
        entries.addAll(trimmed)
    }
}
