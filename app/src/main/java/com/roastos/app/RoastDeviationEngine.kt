package com.roastos.app

import kotlin.math.abs

data class DeviationDiagnosis(
    val overallRisk: String,
    val headline: String,
    val findings: List<String>,
    val causes: List<String>,
    val actions: List<String>,
    val summary: String
)

object RoastDeviationEngine {

    fun diagnoseFromCurrentState(): DeviationDiagnosis {
        val planner = AppState.lastPlannerResult
        val timeline = RoastTimelineStore.current
        val actualRor = AppState.liveActualPreFcRor

        if (planner == null) {
            return DeviationDiagnosis(
                overallRisk = "Unknown",
                headline = "Planner not ready",
                findings = listOf("No planner result available"),
                causes = listOf("Run Planner first"),
                actions = listOf("Generate a roast plan before deviation diagnosis"),
                summary = """
Deviation Diagnosis

Risk
Unknown

Headline
Planner not ready

Findings
• No planner result available

Possible Causes
• Run Planner first

Suggested Actions
• Generate a roast plan before deviation diagnosis
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

        val findings = mutableListOf<String>()
        val causes = linkedSetOf<String>()
        val actions = linkedSetOf<String>()

        var riskScore = 0

        if (actualTurning != null) {
            val delta = actualTurning - predTurning
            findings.add("Turning ${formatSigned(delta)}s vs predicted")
            when {
                delta >= 12 -> {
                    riskScore += 3
                    causes.add("Front-end energy likely too weak")
                    causes.add("Charge temperature may be too low")
                    actions.add("Raise charge temperature slightly next batch")
                    actions.add("Protect early heat momentum")
                }
                delta >= 6 -> {
                    riskScore += 2
                    causes.add("Initial energy slightly weak")
                    actions.add("Tighten front-end energy")
                }
                delta <= -12 -> {
                    riskScore += 3
                    causes.add("Front-end push likely too strong")
                    causes.add("Initial heat may be too aggressive")
                    actions.add("Soften initial charge or early heat")
                    actions.add("Watch overshoot risk")
                }
                delta <= -6 -> {
                    riskScore += 2
                    causes.add("Initial acceleration slightly aggressive")
                    actions.add("Reduce early push slightly")
                }
            }
        } else {
            findings.add("Turning actual not recorded")
        }

        if (actualYellow != null) {
            val delta = actualYellow - predYellow
            findings.add("Yellow ${formatSigned(delta)}s vs predicted")
            when {
                delta >= 18 -> {
                    riskScore += 3
                    causes.add("Drying to Maillard transition likely delayed")
                    causes.add("Middle energy may be insufficient")
                    actions.add("Carry more momentum into Yellow")
                    actions.add("Avoid over-venting before Maillard")
                }
                delta >= 8 -> {
                    riskScore += 2
                    causes.add("Middle phase slightly late")
                    actions.add("Support Maillard energy earlier")
                }
                delta <= -18 -> {
                    riskScore += 3
                    causes.add("Middle phase may be advancing too fast")
                    causes.add("Heat or restricted airflow may be pushing too hard")
                    actions.add("Reduce mid-phase aggression")
                    actions.add("Use airflow to control spike risk")
                }
                delta <= -8 -> {
                    riskScore += 2
                    causes.add("Yellow arrives slightly early")
                    actions.add("Watch pre-FC rise rate carefully")
                }
            }
        } else {
            findings.add("Yellow actual not recorded")
        }

        if (actualFc != null) {
            val delta = actualFc - predFc
            findings.add("FC ${formatSigned(delta)}s vs predicted")
            when {
                delta >= 20 -> {
                    riskScore += 3
                    causes.add("Crack arrival significantly delayed")
                    causes.add("Maillard energy likely faded too much")
                    actions.add("Protect energy before FC")
                    actions.add("Avoid excessive air or premature heat reduction")
                }
                delta >= 10 -> {
                    riskScore += 2
                    causes.add("FC slightly late")
                    actions.add("Carry more energy into crack")
                }
                delta <= -20 -> {
                    riskScore += 3
                    causes.add("Crack arrival significantly early")
                    causes.add("Pre-FC push likely too strong")
                    actions.add("Reduce pre-FC aggressiveness")
                    actions.add("Prevent flick and harsh finish")
                }
                delta <= -10 -> {
                    riskScore += 2
                    causes.add("FC slightly early")
                    actions.add("Moderate energy before crack")
                }
            }
        } else {
            findings.add("FC actual not recorded")
        }

        if (actualDrop != null) {
            val delta = actualDrop - predDrop
            findings.add("Drop ${formatSigned(delta)}s vs predicted")
            when {
                delta >= 20 -> {
                    riskScore += 2
                    causes.add("Finish likely dragged too long")
                    actions.add("Tighten development endpoint")
                }
                delta >= 10 -> {
                    riskScore += 1
                    causes.add("Drop slightly late")
                    actions.add("Watch end-of-roast discipline")
                }
                delta <= -20 -> {
                    riskScore += 2
                    causes.add("Drop likely cut too early")
                    actions.add("Protect enough development time")
                }
                delta <= -10 -> {
                    riskScore += 1
                    causes.add("Drop slightly early")
                    actions.add("Recheck development target")
                }
            }
        } else {
            findings.add("Drop actual not recorded")
        }

        if (actualRor != null) {
            findings.add("Pre-FC ROR ${"%.1f".format(actualRor)}")
            when {
                actualRor >= 10.8 -> {
                    riskScore += 3
                    causes.add("Pre-FC ROR too high")
                    causes.add("Flick risk elevated")
                    actions.add("Reduce heat earlier before FC")
                    actions.add("Use airflow to control late acceleration")
                }
                actualRor >= 9.5 -> {
                    riskScore += 2
                    causes.add("Pre-FC ROR slightly high")
                    actions.add("Watch late spike risk")
                }
                actualRor <= 7.0 -> {
                    riskScore += 3
                    causes.add("Pre-FC ROR too low")
                    causes.add("Crash / flat cup risk elevated")
                    actions.add("Preserve more energy into FC")
                    actions.add("Avoid excessive air or early heat cuts")
                }
                actualRor <= 8.0 -> {
                    riskScore += 2
                    causes.add("Pre-FC ROR slightly low")
                    actions.add("Protect momentum before crack")
                }
            }
        } else {
            findings.add("Pre-FC ROR not recorded")
        }

        if (actions.isEmpty()) {
            actions.add("Batch appears close to plan")
            actions.add("Apply only moderate corrections")
        }

        if (causes.isEmpty()) {
            causes.add("No strong deviation pattern detected")
        }

        val overallRisk = when {
            riskScore >= 9 -> "High"
            riskScore >= 5 -> "Medium"
            riskScore >= 2 -> "Low"
            else -> "Minor"
        }

        val headline = when {
            actualRor != null && actualRor >= 10.8 ->
                "Late-stage acceleration too strong"
            actualRor != null && actualRor <= 7.0 ->
                "Energy may be collapsing before crack"
            actualFc != null && actualFc - predFc >= 20 ->
                "FC is landing too late"
            actualFc != null && actualFc - predFc <= -20 ->
                "FC is landing too early"
            actualTurning != null && actualTurning - predTurning >= 12 ->
                "Front-end energy looks insufficient"
            actualTurning != null && actualTurning - predTurning <= -12 ->
                "Front-end push looks excessive"
            else ->
                "Deviation pattern is moderate"
        }

        val summary = """
Deviation Diagnosis

Risk
$overallRisk

Headline
$headline

Findings
${findings.joinToString("\n") { "• $it" }}

Possible Causes
${causes.joinToString("\n") { "• $it" }}

Suggested Actions
${actions.joinToString("\n") { "• $it" }}
        """.trimIndent()

        return DeviationDiagnosis(
            overallRisk = overallRisk,
            headline = headline,
            findings = findings,
            causes = causes.toList(),
            actions = actions.toList(),
            summary = summary
        )
    }

    private fun formatSigned(value: Int): String {
        return if (value > 0) "+$value" else value.toString()
    }
}
