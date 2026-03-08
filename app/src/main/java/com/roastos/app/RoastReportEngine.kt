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

        val sections = listOf(
            RoastReportSection(
                "BATCH OVERVIEW",
                buildOverviewBody(session)
            ),

            RoastReportSection(
                "BEAN / ENVIRONMENT",
                buildBeanEnvBody(plannerInput)
            ),

            RoastReportSection(
                "PREDICTED PLAN",
                buildPredictedBody(planner, timeline)
            ),

            RoastReportSection(
                "ACTUAL TIMELINE",
                buildActualBody(timeline)
            ),

            RoastReportSection(
                "DEVIATION SUMMARY",
                buildDeviationBody(timeline, planner)
            ),

            RoastReportSection(
                "DIAGNOSIS",
                diagnosis.summary
            ),

            RoastReportSection(
                "NEXT-BATCH CORRECTION",
                bridge.summary
            )
        )

        val reportText = buildString {

            appendLine(title)
            appendLine()

            appendLine("Batch ID")
            appendLine(batchId)
            appendLine()

            for (section in sections) {

                appendLine(section.heading)
                appendLine(section.body)
                appendLine()

            }

        }.trim()

        return RoastReport(
            title = title,
            batchId = batchId,
            summary = reportText,
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

        val elapsed = BatchSessionEngine.currentElapsedSec()

        val elapsedText =
            if (elapsed != null)
                "${elapsed / 60}:${(elapsed % 60).toString().padStart(2, '0')}"
            else
                "-"

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
${session.endTimeMillis ?: "-"}

Notes
${if (session.notes.isBlank()) "-" else session.notes}
""".trimIndent()
    }

    private fun buildBeanEnvBody(input: PlannerInput?): String {

        if (input == null) {

            return """
Bean
No planner input available

Environment
No planner input available
""".trimIndent()

        }

        return """
Bean
Process    ${input.process}
Density    ${"%.1f".format(input.density)}
Moisture   ${"%.1f".format(input.moisture)}
aw         ${"%.2f".format(input.aw)}

Environment
Temp       ${"%.1f".format(input.envTemp)}℃
RH         ${"%.1f".format(input.envRH)}%

Intent
Roast      ${input.roastLevel}
Direction  ${input.orientation}
Mode       ${input.mode}
Batch No   ${input.batchNum}
""".trimIndent()
    }

    private fun buildPredictedBody(
        planner: Any?,
        timeline: RoastTimelineStore.State
    ): String {

        val predTurning = timeline.predicted.turningSec
        val predYellow = timeline.predicted.yellowSec
        val predFc = timeline.predicted.fcSec
        val predDrop = timeline.predicted.dropSec

        val turningText =
            if (predTurning != null) RoastEngine.toMMSS(predTurning.toDouble()) else "-"

        val yellowText =
            if (predYellow != null) RoastEngine.toMMSS(predYellow.toDouble()) else "-"

        val fcText =
            if (predFc != null) RoastEngine.toMMSS(predFc.toDouble()) else "-"

        val dropText =
            if (predDrop != null) RoastEngine.toMMSS(predDrop.toDouble()) else "-"

        val devText =
            if (predFc != null && predDrop != null && predDrop > predFc)
                "${predDrop - predFc}s"
            else
                "-"

        val dtrText =
            if (predFc != null && predDrop != null && predDrop > predFc)
                "%.1f".format((predDrop - predFc).toDouble() / predDrop * 100.0) + "%"
            else
                "-"

        return """
Predicted Anchors
Turning   $turningText
Yellow    $yellowText
FC        $fcText
Drop      $dropText

Development
Dev       $devText
DTR       $dtrText
""".trimIndent()
    }

    private fun buildActualBody(timeline: RoastTimelineStore.State): String {

        val turning =
            timeline.actual.turningSec?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"

        val yellow =
            timeline.actual.yellowSec?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"

        val fc =
            timeline.actual.fcSec?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"

        val drop =
            timeline.actual.dropSec?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"

        val ror =
            timeline.currentRor?.let { "%.1f".format(it) } ?: "-"

        val dev =
            timeline.devSec?.toString() ?: "-"

        val dtr =
            timeline.dtrPercent?.let { "%.1f".format(it) + "%" } ?: "-"

        return """
Actual Anchors
Turning   $turning
Yellow    $yellow
FC        $fc
Drop      $drop

Current
Phase     ${timeline.currentPhase}
ROR       $ror
Dev       $dev
DTR       $dtr
""".trimIndent()
    }

    private fun buildDeviationBody(
        timeline: RoastTimelineStore.State,
        planner: Any?
    ): String {

        val lines = mutableListOf<String>()

        lines.add(buildDeviationLine("Turning", timeline.predicted.turningSec, timeline.actual.turningSec))
        lines.add(buildDeviationLine("Yellow", timeline.predicted.yellowSec, timeline.actual.yellowSec))
        lines.add(buildDeviationLine("FC", timeline.predicted.fcSec, timeline.actual.fcSec))
        lines.add(buildDeviationLine("Drop", timeline.predicted.dropSec, timeline.actual.dropSec))

        val rorText =
            AppState.liveActualPreFcRor?.let { "%.1f".format(it) } ?: "-"

        lines.add("Pre-FC ROR $rorText")

        return lines.joinToString("\n")
    }

    private fun buildDeviationLine(
        label: String,
        predicted: Int?,
        actual: Int?
    ): String {

        if (predicted == null)
            return "$label   Pred - / Actual ${actual ?: "-"} / Δ -"

        if (actual == null)
            return "$label   Pred ${predicted}s / Actual - / Δ -"

        val delta = actual - predicted

        val deltaText =
            if (delta > 0)
                "+${delta}s"
            else
                "${delta}s"

        return "$label   Pred ${predicted}s / Actual ${actual}s / Δ $deltaText"
    }
}
