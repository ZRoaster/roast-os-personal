package com.roastos.app.ui

import com.roastos.app.*

object LiveAssistPage {

    private const val DEFAULT_POWER_W = 540
    private const val DEFAULT_AIRFLOW_PA = 10
    private const val DEFAULT_DRUM_RPM = 60


    fun buildLiveAssist(): String {

        val predTurning = AppState.predTurningSec ?: return "No prediction available"
        val predYellow = AppState.predYellowSec ?: return "No prediction available"
        val predFc = AppState.predFcSec ?: return "No prediction available"
        val predDrop = AppState.predDropSec ?: return "No prediction available"

        val decisionCard = buildDecisionCard(
            predTurning,
            predYellow,
            predFc,
            predDrop
        )

        val controlCard = buildControlCard(
            predTurning,
            predYellow,
            predFc,
            predDrop
        )

        return """
LIVE ASSIST

$decisionCard

$controlCard
""".trimIndent()
    }


    private fun buildDecisionCard(
        predTurning: Int,
        predYellow: Int,
        predFc: Int,
        predDrop: Int
    ): String {

        val plannerInput = AppState.lastPlannerInput ?: return "Planner not initialized"

        val decision = DecisionEngine.decide(
            predTurning = predTurning,
            predYellow = predYellow,
            predFc = predFc,
            predDrop = predDrop,
            actualTurning = AppState.liveActualTurningSec,
            actualYellow = AppState.liveActualYellowSec,
            actualFc = AppState.liveActualFcSec,
            actualDrop = AppState.liveActualDropSec,
            currentRor = AppState.liveActualPreFcRor,
            envTemp = plannerInput.envTemp,
            humidity = plannerInput.envRH,
            pressureKpa = 1013.0,
            density = plannerInput.density,
            moisture = plannerInput.moisture,
            aw = plannerInput.aw,
            heatLevelW = DEFAULT_POWER_W,
            airflowPa = DEFAULT_AIRFLOW_PA,
            drumRpm = DEFAULT_DRUM_RPM
        )

        return """
Decision Engine

Current Phase
${decision.currentPhase}

Action Now
${decision.actionNow}

Heat Command
${decision.heatCommand}

Air Command
${decision.airCommand}

Target Window
${decision.targetWindow}

Risk Level
${decision.riskLevel}

Reason
${decision.reason}

Physics
${decision.physicsSummary}
""".trimIndent()
    }



    private fun buildControlCard(
        predTurning: Int,
        predYellow: Int,
        predFc: Int,
        predDrop: Int
    ): String {

        val actualTurning = AppState.liveActualTurningSec
        val actualYellow = AppState.liveActualYellowSec
        val actualFc = AppState.liveActualFcSec
        val actualDrop = AppState.liveActualDropSec
        val actualRor = AppState.liveActualPreFcRor


        val currentStage = when {
            actualDrop != null -> "Finished"
            actualFc != null -> "Development"
            actualYellow != null -> "Maillard"
            actualTurning != null -> "Drying"
            else -> "Pre Turning"
        }


        val nextAction = when {
            actualDrop != null -> "Roast complete"
            actualFc != null && actualRor != null && actualRor > 10 -> "Reduce heat slightly"
            actualFc != null && actualRor != null && actualRor < 7 -> "Maintain energy"
            actualYellow != null -> "Manage Maillard energy"
            actualTurning != null -> "Guide drying phase"
            else -> "Watch turning point"
        }


        val biggestRisk = when {
            actualFc != null && actualRor != null && actualRor > 10 -> "Flick risk"
            actualFc != null && actualRor != null && actualRor < 7 -> "Crash risk"
            actualYellow != null && actualYellow - predYellow > 15 -> "Late development"
            actualYellow != null && actualYellow - predYellow < -15 -> "Early spike"
            actualTurning != null && actualTurning - predTurning > 8 -> "Front energy low"
            actualTurning != null && actualTurning - predTurning < -8 -> "Front energy high"
            else -> "No dominant risk yet"
        }


        return """
Control Analysis

Current Stage
$currentStage

Predicted Anchors
Turning ${RoastEngine.toMMSS(predTurning.toDouble())}
Yellow ${RoastEngine.toMMSS(predYellow.toDouble())}
FC ${RoastEngine.toMMSS(predFc.toDouble())}
Drop ${RoastEngine.toMMSS(predDrop.toDouble())}

Actual Anchors
Turning ${actualTurning?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}
Yellow ${actualYellow?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}
FC ${actualFc?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}
Drop ${actualDrop?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}
Pre-FC ROR ${actualRor?.let { "%.1f".format(it) } ?: "-"}

Next Action
$nextAction

Biggest Risk
$biggestRisk
""".trimIndent()
    }

}
