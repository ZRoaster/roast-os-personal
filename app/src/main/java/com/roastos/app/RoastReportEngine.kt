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

        val overviewSection = RoastReportSection(
            heading = "BATCH OVERVIEW",
            body = buildOverviewBody(session)
        )

        val beanEnvSection = RoastReportSection(
            heading = "BEAN / ENVIRONMENT",
            body = buildBeanEnvBody(plannerInput)
        )

        val predictedSection = RoastReportSection(
            heading = "PREDICTED PLAN",
            body = buildPredictedBody(planner, timeline)
        )

        val actualSection = RoastReportSection(
            heading = "ACTUAL TIMELINE",
            body = buildActualBody(timeline)
        )

        val deviationSection = RoastReportSection(
            heading = "DEVIATION SUMMARY",
            body = buildDeviationBody(timeline, planner)
        )

        val diagnosisSection = RoastReportSection(
            heading = "DIAGNOSIS",
            body = diagnosis.summary
        )

        val correctionSection = RoastReportSection(
            heading = "NEXT-BATCH CORRECTION",
            body = bridge.summary
        )

        val sections = listOf(
            overviewSection,
            beanEnvSection,
            predictedSection,
            actualSection,
            deviationSection,
            diagnosisSection,
            correctionSection
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

    private fun buildOverviewBody(session: BatchSession?): String {
        if (session == null) {
            return """
Batch ID
-

Status
Idle

Elapsed
-

Notes
No active session
            """.trimIndent()
        }

        val elapsedSec = BatchSessionEngine.currentElapsedSec()
        val elapsedText = elapsedSec?.let { "${it / 60}:${(it % 60).toString().padStart(2, '0')}" } ?: "-"

        return """
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

    private fun buildBeanEnvBody(plannerInput: PlannerInput?): String {
        if (plannerInput == null) {
            return """
Bean
No planner input available

Environment
No planner input available
            """.trimIndent()
        }

        return """
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

    private fun buildPredictedBody(
        planner: Any?,
        timeline: RoastTimelineStore.State
    ): String {
        val plannerCard = planner as? RoastCard

        val predTurning = timeline.predicted.turningSec
        val predYellow = timeline.predicted.yellowSec
        val predFc = timeline.predicted.fcSec
        val predDrop = timeline.predicted.dropSec

        return """
Charge
${plannerCard?.chargeBT?.toString()?.plus("℃") ?: "-"}

Predicted Anchors
Turning   ${predTurning?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}
Yellow    ${predYellow?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}
FC        ${predFc?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}
Drop      ${predDrop?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}

Development
Dev       ${if (predFc != null && predDrop != null && predDrop > predFc) "${predDrop - predFc}s" else "-"}
DTR       ${
            if (predFc != null && predDrop != null && predDrop > 0 && predDrop > predFc) {
                "%.1f".format(((predDrop - predFc).toDouble() / predDrop.toDouble()) * 100.0) + "%"
            } else "-"
        }
        """.trimIndent()
    }

    private fun buildActualBody(timeline: RoastTimelineStore.State): String {
        return """
Actual Anchors
Turning   ${timeline.actual.turningSec?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}
Yellow    ${timeline.actual.yellowSec?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}
FC        ${timeline.actual.fcSec?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}
Drop      ${timeline.actual.dropSec?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}

Current
Phase     ${timeline.currentPhase}
ROR       ${timeline.currentRor?.let { "%.1f".format(it) } ?: "-"}
Dev       ${timeline.devSec?.toString() ?: "-"}
DTR       ${timeline.dtrPercent?.let { "%.1f".format(it) + "%" } ?: "-"}
        """.trimIndent()
    }

    private fun buildDeviationBody(
        timeline: RoastTimelineStore.State,
        planner: Any?
    ): String {
        val plannerCard = planner as? RoastCard

        val predTurning = timeline.predicted.turningSec
            ?: plannerCard?.let { (it.h1Sec - 60.0).toInt().coerceAtLeast(50) }
        val predYellow = timeline.predicted.yellowSec ?: plannerCard?.h2Sec?.toInt()
        val predFc = timeline.predicted.fcSec ?: plannerCard?.fcPredSec?.toInt()
        val predDrop = timeline.predicted.dropSec ?: plannerCard?.dropSec?.toInt()

        val lines = mutableListOf<String>()

        lines.add(
            buildDeviationLine(
                label = "Turning",
                predicted = predTurning,
                actual = timeline.actual.turningSec
            )
        )
        lines.add(
            buildDeviationLine(
                label = "Yellow",
                predicted = predYellow,
                actual = timeline.actual.yellowSec
            )
        )
        lines.add(
            buildDeviationLine(
                label = "FC",
                predicted = predFc,
                actual = timeline.actual.fcSec
            )
        )
        lines.add(
            buildDeviationLine(
                label = "Drop",
                predicted = predDrop,
                actual = timeline.actual.dropSec
            )
        )

        val rorLine = "Pre-FC ROR ${AppState.liveActualPreFcRor?.let { "%.1f".format(it) } ?: "-"}"
        lines.add(rorLine)

        return lines.joinToString("\n")
    }

    private fun buildDeviationLine(
        label: String,
        predicted: Int?,
        actual: Int?
    ): String {
        if (predicted == null) return "$label   Pred - / Actual ${actual?.toString() ?: "-"} / Δ -"
        if (actual == null) return "$label   Pred ${predicted}s / Actual - / Δ -"

        val delta = actual - predicted
        val deltaText = if (delta > 0) "+${delta}s" else "${delta}s"

        return "$label   Pred ${predicted}s / Actual ${actual}s / Δ $deltaText"
    }
}
