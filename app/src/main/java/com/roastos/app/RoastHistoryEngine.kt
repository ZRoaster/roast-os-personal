package com.roastos.app

data class RoastEvaluation(
    val beanColor: Double?,
    val groundColor: Double?,
    val roastedAw: Double?,
    val sweetness: Int?,
    val acidity: Int?,
    val body: Int?,
    val flavorClarity: Int?,
    val balance: Int?,
    val notes: String
)

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
    val correctionText: String,
    val evaluation: RoastEvaluation? = null,

    val baselineSource: String? = null,
    val baselineLabel: String? = null,
    val baselineMatchGrade: String? = null,
    val baselineSourceProfileId: String? = null,
    val baselineSourceBatchId: String? = null
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

        val baseline = PlannerBaselineStore.current()
        val baselineMatch = PlannerBaselineStore.evaluateMatchAgainstCurrentInput()

        val batchId = session?.batchId ?: "NO-BATCH-${System.currentTimeMillis()}"
        val createdAtMillis = System.currentTimeMillis()

        val existing = findByBatchId(batchId)

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
            correctionText = correction.summary,
            evaluation = existing?.evaluation,

            baselineSource = baseline?.source,
            baselineLabel = baseline?.label,
            baselineMatchGrade = baselineMatch?.grade?.name,
            baselineSourceProfileId = baseline?.sourceProfileId,
            baselineSourceBatchId = baseline?.sourceBatchId
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

    fun saveEvaluation(
        batchId: String,
        evaluation: RoastEvaluation
    ): RoastHistorySaveResult {
        val index = entries.indexOfFirst { it.batchId == batchId }

        if (index < 0) {
            return RoastHistorySaveResult(
                saved = false,
                replacedExisting = false,
                totalCount = entries.size,
                message = "No history found for $batchId"
            )
        }

        val updated = entries[index].copy(evaluation = evaluation)
        entries[index] = updated

        return RoastHistorySaveResult(
            saved = true,
            replacedExisting = true,
            totalCount = entries.size,
            message = "Evaluation saved for $batchId"
        )
    }

    fun clearEvaluation(batchId: String): RoastHistorySaveResult {
        val index = entries.indexOfFirst { it.batchId == batchId }

        if (index < 0) {
            return RoastHistorySaveResult(
                saved = false,
                replacedExisting = false,
                totalCount = entries.size,
                message = "No history found for $batchId"
            )
        }

        val updated = entries[index].copy(evaluation = null)
        entries[index] = updated

        return RoastHistorySaveResult(
            saved = true,
            replacedExisting = true,
            totalCount = entries.size,
            message = "Evaluation cleared for $batchId"
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

Latest Evaluation
${if (latest?.evaluation != null) "Saved" else "Not saved"}

Latest Baseline
${latest?.baselineLabel ?: "Not recorded"}
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
