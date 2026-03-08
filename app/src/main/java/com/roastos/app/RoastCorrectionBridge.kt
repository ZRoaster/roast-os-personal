package com.roastos.app

data class RoastCorrectionBridgeResult(
    val headline: String,
    val direction: String,
    val adjustments: List<String>,
    val priority: List<String>,
    val summary: String
)

object RoastCorrectionBridge {

    fun buildFromCurrentState(): RoastCorrectionBridgeResult {
        val planner = AppState.lastPlannerResult
        val timeline = RoastTimelineStore.current
        val actualRor = AppState.liveActualPreFcRor
        val diagnosis = RoastDeviationEngine.diagnoseFromCurrentState()

        if (planner == null) {
            return RoastCorrectionBridgeResult(
                headline = "Planner not ready",
                direction = "Generate planner baseline first",
                adjustments = listOf("Run Planner before building correction bridge"),
                priority = listOf("Create roast baseline"),
                summary = """
Correction Bridge

Headline
Planner not ready

Direction
Generate planner baseline first

Priority
• Create roast baseline

Adjustments
• Run Planner before building correction bridge
                """.trimIndent()
            )
        }

        val predTurning = timeline.predicted.turningSec
            ?: (planner.h1Sec - 60.0).toInt().coerceAtLeast(50)
        val predYellow = timeline.predicted.yellowSec
            ?: planner.h2Sec.toInt()
        val predFc = timeline.predicted.fcSec
            ?: planner.fcPredSec.toInt()
        val predDrop = timeline.predicted.dropSec
            ?: planner.dropSec.toInt()

        val actualTurning = timeline.actual.turningSec
        val actualYellow = timeline.actual.yellowSec
        val actualFc = timeline.actual.fcSec
        val actualDrop = timeline.actual.dropSec

        val turningDelta = actualTurning?.minus(predTurning)
        val yellowDelta = actualYellow?.minus(predYellow)
        val fcDelta = actualFc?.minus(predFc)
        val dropDelta = actualDrop?.minus(predDrop)

        val priority = mutableListOf<String>()
        val adjustments = mutableListOf<String>()

        if (turningDelta != null) {
            when {
                turningDelta >= 10 -> {
                    priority.add("Front-end energy recovery")
                    adjustments.add("Next batch: charge temperature +1℃ to +2℃")
                    adjustments.add("Next batch: protect early heat momentum before Turning")
                }
                turningDelta <= -10 -> {
                    priority.add("Front-end aggression reduction")
                    adjustments.add("Next batch: charge temperature -1℃ to -2℃")
                    adjustments.add("Next batch: soften initial push before Turning")
                }
            }
        }

        if (yellowDelta != null) {
            when {
                yellowDelta >= 15 -> {
                    priority.add("Middle-phase energy support")
                    adjustments.add("Next batch: carry more energy from drying into Maillard")
                    adjustments.add("Next batch: avoid excessive airflow before Yellow")
                }
                yellowDelta <= -15 -> {
                    priority.add("Middle-phase speed control")
                    adjustments.add("Next batch: reduce mid-phase aggression slightly")
                    adjustments.add("Next batch: use airflow to slow early acceleration")
                }
            }
        }

        if (fcDelta != null) {
            when {
                fcDelta >= 15 -> {
                    priority.add("Pre-FC energy preservation")
                    adjustments.add("Next batch: delay heat reduction before FC")
                    adjustments.add("Next batch: preserve more momentum entering crack")
                }
                fcDelta <= -15 -> {
                    priority.add("Pre-FC aggressiveness control")
                    adjustments.add("Next batch: reduce pre-FC push")
                    adjustments.add("Next batch: watch for flick and harsh finish")
                }
            }
        }

        if (dropDelta != null) {
            when {
                dropDelta >= 15 -> {
                    priority.add("Development endpoint discipline")
                    adjustments.add("Next batch: tighten drop timing")
                }
                dropDelta <= -15 -> {
                    priority.add("Development completion protection")
                    adjustments.add("Next batch: allow more finish time before drop")
                }
            }
        }

        if (actualRor != null) {
            when {
                actualRor >= 10.8 -> {
                    priority.add("Late-stage spike control")
                    adjustments.add("Next batch: reduce heat earlier before FC")
                    adjustments.add("Next batch: consider +1Pa airflow near pre-FC")
                }
                actualRor <= 7.0 -> {
                    priority.add("Late-stage momentum protection")
                    adjustments.add("Next batch: preserve heat deeper into pre-FC")
                    adjustments.add("Next batch: avoid over-venting before crack")
                }
            }
        }

        if (priority.isEmpty()) {
            priority.add("Fine tuning only")
            adjustments.add("Batch is near target; use small corrections only")
            adjustments.add("Keep current structure and adjust one variable at a time")
        }

        val direction = when {
            actualRor != null && actualRor >= 10.8 ->
                "Reduce late-stage aggressiveness and protect finish quality"
            actualRor != null && actualRor <= 7.0 ->
                "Preserve more energy into crack and prevent flattening"
            fcDelta != null && fcDelta >= 15 ->
                "Carry more usable energy into first crack"
            fcDelta != null && fcDelta <= -15 ->
                "Slow down pre-FC acceleration"
            turningDelta != null && turningDelta >= 10 ->
                "Recover front-end energy earlier"
            turningDelta != null && turningDelta <= -10 ->
                "Soften front-end push"
            else ->
                "Apply moderate correction and protect replayability"
        }

        val headline = when {
            actualRor != null && actualRor >= 10.8 ->
                "Bridge focus: control late spike"
            actualRor != null && actualRor <= 7.0 ->
                "Bridge focus: protect momentum into FC"
            fcDelta != null && fcDelta >= 15 ->
                "Bridge focus: FC arrived too late"
            fcDelta != null && fcDelta <= -15 ->
                "Bridge focus: FC arrived too early"
            turningDelta != null && turningDelta >= 10 ->
                "Bridge focus: front-end energy too weak"
            turningDelta != null && turningDelta <= -10 ->
                "Bridge focus: front-end push too strong"
            else ->
                "Bridge focus: mild correction path"
        }

        val summary = """
Correction Bridge

Headline
$headline

Direction
$direction

Diagnosis Link
${diagnosis.headline}

Priority
${priority.joinToString("\n") { "• $it" }}

Next-Batch Adjustments
${adjustments.joinToString("\n") { "• $it" }}
        """.trimIndent()

        return RoastCorrectionBridgeResult(
            headline = headline,
            direction = direction,
            adjustments = adjustments,
            priority = priority,
            summary = summary
        )
    }
}
