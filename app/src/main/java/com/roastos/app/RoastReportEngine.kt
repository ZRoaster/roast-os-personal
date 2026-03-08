package com.roastos.app

data class RoastReport(
    val title: String,
    val batchId: String,
    val summary: String,
    val sections: List<RoastReportSection>
)

data class RoastReportSection(
    val heading: String,
    val body: String
)

object RoastReportEngine {

    fun buildFromCurrentState(): RoastReport {
        val planner = AppState.lastPlannerResult
        val plannerInput = AppState.lastPlannerInput
        val timeline = RoastTimelineStore.current
        val session = BatchSessionEngine.current()
        val diagnosis = RoastDeviationEngine.diagnoseFromCurrentState()
        val bridge = RoastCorrectionBridge.buildFromCurrentState()

        val batchId = session?.batchId ?: "NO-BATCH"
        val title = "Roast Report"

        val predTurning = timeline.predicted.turningSec
            ?: planner?.let { (it.h1Sec - 60.0).toInt().coerceAtLeast(50) }

        val predYellow = timeline.predicted.yellowSec
            ?: planner?.h2Sec?.toInt()

        val predFc = timeline.predicted.fcSec
            ?: planner?.fcPredSec?.toInt()

        val predDrop = timeline.predicted.dropSec
            ?: planner?.dropSec?.toInt()

        val actualTurning = timeline.actual.turningSec
        val actualYellow = timeline.actual.yellowSec
        val actualFc = timeline.actual.fcSec
        val actualDrop = timeline.actual.dropSec
        val actualRor = AppState.liveActualPreFcRor

        val elapsedSec = BatchSessionEngine.currentElapsedSec()
        val elapsedText = if (elapsedSec != null) {
            "${elapsedSec / 60}:${(elapsedSec % 60).toString().padStart(2, '0')}"
        } else {
            "-"
        }

        val predictedDevText =
            if (predFc != null && predDrop != null && predDrop > predFc) {
                "${predDrop - predFc}s"
            } else {
                "-"
            }

        val predictedDtrText =
            if (predFc != null && predDrop != null && predDrop > predFc && predDrop > 0) {
                "%.1f".format(((predDrop - predFc).toDouble() / predDrop.toDouble()) * 100.0) + "%"
            } else {
                "-"
            }

        val overviewBody = if (session == null) {
            """
Batch ID
-

Status
Idle

Elapsed
-

Notes
No active session
            """.trimIndent()
        } else {
            """
Batch ID
${session.batchId}

Status
${session.status}

Elapsed
$elapsedText

Started
${session.startTimeMillis}

Ended
${session.endTimeMillis?.toString() ?: "-"}

Notes
${if (session.notes.isBlank()) "-" else session.notes}
            """.trimIndent()
        }

        val beanEnvBody = if (plannerInput == null) {
            """
Bean
No planner input available

Environment
No planner input available
            """.trimIndent()
        } else {
            """
Bean
Process    ${plannerInput.process}
Density    ${"%.1f".format(plannerInput.density)}
Moisture   ${"%.1f".format(plannerInput.moisture)}
aw         ${"%.2f".format(plannerInput.aw)}

Environment
Temp       ${"%.1f".format(plannerInput.envTemp)}℃
RH         ${"%.1f".format(plannerInput.envRH)}%

Intent
Roast      ${plannerInput.roastLevel}
Direction  ${plannerInput.orientation}
Mode       ${plannerInput.mode}
Batch No   ${plannerInput.batchNum}
            """.trimIndent()
        }

        val predictedBody = """
Charge
${planner?.chargeBT?.toString()?.plus("℃") ?: "-"}

Predicted Anchors
Turning   ${predTurning?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}
Yellow    ${predYellow?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}
FC        ${predFc?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}
Drop      ${predDrop?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}

Development
Dev       $predictedDevText
DTR       $predictedDtrText
        """.trimIndent()

        val actualBody = """
Actual Anchors
Turning   ${actualTurning?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}
Yellow    ${actualYellow?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}
FC        ${actualFc?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}
Drop      ${actualDrop?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}

Current
Phase     ${timeline.currentPhase}
ROR       ${timeline.currentRor?.let { "%.1f".format(it) } ?: "-"}
Dev       ${timeline.devSec?.toString() ?: "-"}
DTR       ${timeline.dtrPercent?.let { "%.1f".format(it) + "%" } ?: "-"}
        """.trimIndent()

        fun deviationLine(label: String, predicted: Int?, actual: Int?): String {
            return when {
                predicted == null -> "$label   Pred - / Actual ${actual?.toString() ?: "-"} / Δ -"
                actual == null -> "$label   Pred ${predicted}s / Actual - / Δ -"
                else -> {
                    val delta = actual - predicted
                    "$label   Pred ${predicted}s / Actual ${actual}s / Δ ${formatSigned(delta)}s"
                }
            }
        }

        val deviationBody = """
${deviationLine("Turning", predTurning, actualTurning)}
${deviationLine("Yellow", predYellow, actualYellow)}
${deviationLine("FC", predFc, actualFc)}
${deviationLine("Drop", predDrop, actualDrop)}
Pre-FC ROR ${actualRor?.let { "%.1f".format(it) } ?: "-"}
        """.trimIndent()

        val sections = listOf(
            RoastReportSection(
                heading = "BATCH OVERVIEW",
                body = overviewBody
            ),
            RoastReportSection(
                heading = "BEAN / ENVIRONMENT",
                body = beanEnvBody
            ),
            RoastReportSection(
                heading = "PREDICTED PLAN",
                body = predictedBody
            ),
            RoastReportSection(
                heading = "ACTUAL TIMELINE",
                body = actualBody
            ),
            RoastReportSection(
                heading = "DEVIATION SUMMARY",
                body = deviationBody
            ),
            RoastReportSection(
                heading = "DIAGNOSIS",
                body = diagnosis.summary
            ),
            RoastReportSection(
                heading = "NEXT-BATCH CORRECTION",
                body = bridge.summary
            )
        )

        val summary = buildString {
            appendLine(title)
            appendLine()
            appendLine("Batch ID")
            appendLine(batchId)
            appendLine()

            sections.forEachIndexed { index, section ->
                appendLine(section.heading)
                appendLine(section.body)
                if (index != sections.lastIndex) {
                    appendLine()
                }
            }
        }.trim()

        return RoastReport(
            title = title,
            batchId = batchId,
            summary = summary,
            sections = sections
        )
    }

    private fun formatSigned(value: Int): String {
        return if (value > 0) "+$value" else value.toString()
    }
}
