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
    val baselineSourceBatchId: String? = null,

    val roastHealthHeadline: String = "稳定",
    val roastHealthDetail: String = "当前未检测到明显风险。"
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

    fun latestLogText(): String {
        return latest()?.reportText ?: "No roast history yet."
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
            baselineSourceBatchId = baseline?.sourceBatchId,

            roastHealthHeadline = existing?.roastHealthHeadline ?: "稳定",
            roastHealthDetail = existing?.roastHealthDetail ?: "当前未检测到明显风险。"
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

    fun saveRoastLog(
        log: RoastLog,
        machineName: String = "HB M2SE"
    ): RoastHistorySaveResult {

        val existing = findByBatchId(log.batchId)
        val validation = buildValidationFromLog(log)

        val entry = RoastHistoryEntry(
            batchId = log.batchId,
            createdAtMillis = System.currentTimeMillis(),
            title = "Roast ${log.batchId}",

            process = machineName,
            density = 0.0,
            moisture = 0.0,
            aw = 0.0,

            envTemp = 0.0,
            envRh = 0.0,

            predictedTurningSec = null,
            predictedYellowSec = null,
            predictedFcSec = null,
            predictedDropSec = null,

            actualTurningSec = log.turningPointSec,
            actualYellowSec = log.dryEndSec,
            actualFcSec = log.firstCrackSec,
            actualDropSec = log.dropSec,
            actualPreFcRor = log.finalRor,

            batchStatus = log.status,
            reportText = RoastLogEngine.buildLogText(
                session = RoastSessionEngine.currentState(),
                machineName = machineName
            ),
            diagnosisText = buildDiagnosisText(log),
            correctionText = buildCorrectionText(log),
            evaluation = existing?.evaluation,

            baselineSource = existing?.baselineSource,
            baselineLabel = existing?.baselineLabel,
            baselineMatchGrade = existing?.baselineMatchGrade,
            baselineSourceProfileId = existing?.baselineSourceProfileId,
            baselineSourceBatchId = existing?.baselineSourceBatchId,

            roastHealthHeadline = buildValidationHeadline(validation),
            roastHealthDetail = buildValidationDetail(validation)
        )

        val existingIndex = entries.indexOfFirst { it.batchId == log.batchId }
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
                "History updated for ${log.batchId}"
            } else {
                "History saved for ${log.batchId}"
            }
        )
    }

    fun saveCurrentRoastLog(
        session: RoastSessionState,
        machineName: String = "HB M2SE"
    ): RoastHistorySaveResult {
        val log = RoastLogEngine.buildLog(session, machineName)
        return saveRoastLog(log, machineName)
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

Latest Status
${latest?.batchStatus ?: "-"}

Latest Health
${latest?.roastHealthHeadline ?: "-"}

Latest Evaluation
${if (latest?.evaluation != null) "Saved" else "Not saved"}
        """.trimIndent()
    }

    private fun trimToMaxSize() {

        if (entries.size <= MAX_HISTORY_COUNT) return

        val sorted = entries.sortedByDescending { it.createdAtMillis }
        val trimmed = sorted.take(MAX_HISTORY_COUNT)

        entries.clear()
        entries.addAll(trimmed)
    }

    private fun buildDiagnosisText(log: RoastLog): String {
        return buildString {
            appendLine("Roast Diagnosis")
            appendLine()
            appendLine("Status")
            appendLine(log.status)
            appendLine()
            appendLine("Total Time")
            appendLine(formatSec(log.totalTimeSec))
            appendLine()
            appendLine("Development Ratio")
            appendLine(
                if (log.developmentRatio == null) {
                    "-"
                } else {
                    "${oneDecimal(log.developmentRatio * 100.0)}%"
                }
            )
            appendLine()
            appendLine("Final RoR")
            append(
                if (log.finalRor == null) {
                    "-"
                } else {
                    "${oneDecimal(log.finalRor)} ℃/min"
                }
            )
        }
    }

    private fun buildCorrectionText(log: RoastLog): String {
        return buildString {
            appendLine("Correction")
            appendLine()
            appendLine("Suggested Direction")
            appendLine(
                when {
                    log.developmentRatio == null ->
                        "Continue observing. Development ratio not available yet."

                    log.developmentRatio < 0.12 ->
                        "Development may be short. Consider extending post-crack slightly."

                    log.developmentRatio > 0.22 ->
                        "Development may be long. Consider shortening finish slightly."

                    else ->
                        "Development ratio looks balanced. Maintain current roast rhythm."
                }
            )
        }
    }

    private fun buildValidationFromLog(
        log: RoastLog
    ): RoastValidationResult {
        val issues = mutableListOf<RoastValidationIssue>()

        if (log.finalRor != null && log.totalTimeSec >= 30 && log.finalRor < 3.0) {
            issues.add(
                RoastValidationIssue(
                    code = "stall",
                    title = "Possible Stall",
                    detail = "Final RoR is very low. The roast may have lost internal momentum.",
                    severity = "medium"
                )
            )
        }

        if (log.dropTemp != null && log.dropTemp >= 175.0 && log.finalRor != null && log.finalRor < 1.5) {
            issues.add(
                RoastValidationIssue(
                    code = "crash",
                    title = "Crash Risk",
                    detail = "Late-stage RoR appears collapsed. The finish may become flat.",
                    severity = "high"
                )
            )
        }

        if (log.dropTemp != null && log.dropTemp >= 185.0 && log.finalRor != null && log.finalRor > 10.0) {
            issues.add(
                RoastValidationIssue(
                    code = "flick",
                    title = "Flick Risk",
                    detail = "Late-stage RoR appears too aggressive. The finish may become sharp.",
                    severity = "medium"
                )
            )
        }

        return RoastValidationResult(
            issues = issues.distinctBy { it.code }
        )
    }

    private fun buildValidationHeadline(
        validation: RoastValidationResult
    ): String {
        return if (!validation.hasIssues()) {
            "稳定"
        } else {
            when (validation.highestSeverity()) {
                "high" -> "高风险"
                "medium" -> "中风险"
                "watch" -> "需留意"
                "low" -> "低风险"
                else -> "稳定"
            }
        }
    }

    private fun buildValidationDetail(
        validation: RoastValidationResult
    ): String {
        if (!validation.hasIssues()) {
            return "当前未检测到明显风险。"
        }

        return validation.issues.joinToString("\n\n") { issue ->
            "${issue.title}\n${issue.detail}\n等级：${formatSeverity(issue.severity)}"
        }
    }

    private fun formatSeverity(severity: String): String {
        return when (severity) {
            "high" -> "高"
            "medium" -> "中"
            "watch" -> "留意"
            "low" -> "低"
            else -> severity
        }
    }

    private fun formatSec(sec: Int): String {
        val minutes = sec / 60
        val seconds = sec % 60
        return "%d:%02d".format(minutes, seconds)
    }

    private fun oneDecimal(value: Double): String {
        return "%.1f".format(value)
    }
}
