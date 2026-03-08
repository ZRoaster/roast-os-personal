package com.roastos.app.ui

import com.roastos.app.AppState
import com.roastos.app.DecisionEngine
import com.roastos.app.RoastEngine
import com.roastos.app.RoastStateModel

object LiveAssistPage {

    private const val DEFAULT_POWER_W = 540
    private const val DEFAULT_AIRFLOW_PA = 10
    private const val DEFAULT_DRUM_RPM = 60

    fun buildLiveAssist(): String {
        val planner = AppState.lastPlannerResult ?: return "No planner result available"
        val plannerInput = AppState.lastPlannerInput ?: return "No planner input available"

        val predTurning = (planner.h1Sec - 60.0).toInt().coerceAtLeast(50)
        val predYellow = planner.h2Sec.toInt()
        val predFc = planner.fcPredSec.toInt()
        val predDrop = planner.dropSec.toInt()

        RoastStateModel.syncPlannerInput(plannerInput)
        RoastStateModel.syncLiveState(
            phase = currentPhase(),
            ror = AppState.liveActualPreFcRor ?: defaultRorForCurrentState(),
            turningSec = AppState.liveActualTurningSec,
            yellowSec = AppState.liveActualYellowSec,
            fcSec = AppState.liveActualFcSec,
            dropSec = AppState.liveActualDropSec,
            powerW = DEFAULT_POWER_W,
            airflowPa = DEFAULT_AIRFLOW_PA,
            drumRpm = DEFAULT_DRUM_RPM
        )

        val decisionCard = buildDecisionCard(
            predTurning = predTurning,
            predYellow = predYellow,
            predFc = predFc,
            predDrop = predDrop
        )

        val controlCard = buildControlCard(
            predTurning = predTurning,
            predYellow = predYellow,
            predFc = predFc,
            predDrop = predDrop
        )

        return """
LIVE ASSIST

Planner Baseline
Turning ${RoastEngine.toMMSS(predTurning.toDouble())}
Yellow ${RoastEngine.toMMSS(predYellow.toDouble())}
FC ${RoastEngine.toMMSS(predFc.toDouble())}
Drop ${RoastEngine.toMMSS(predDrop.toDouble())}

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
            actualFc != null && actualRor != null && actualRor > 10.0 ->
                "Reduce heat slightly"
            actualFc != null && actualRor != null && actualRor < 7.0 ->
                "Maintain energy"
            actualYellow != null ->
                "Manage Maillard energy"
            actualTurning != null ->
                "Guide drying phase"
            else ->
                "Watch turning point"
        }

        val biggestRisk = when {
            actualFc != null && actualRor != null && actualRor > 10.0 ->
                "Flick risk"
            actualFc != null && actualRor != null && actualRor < 7.0 ->
                "Crash risk"
            actualYellow != null && actualYellow - predYellow > 15 ->
                "Late development"
            actualYellow != null && actualYellow - predYellow < -15 ->
                "Early spike"
            actualTurning != null && actualTurning - predTurning > 8 ->
                "Front energy low"
            actualTurning != null && actualTurning - predTurning < -8 ->
                "Front energy high"
            else ->
                "No dominant risk yet"
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

    private fun currentPhase(): String {
        return when {
            AppState.liveActualDropSec != null -> "Finished"
            AppState.liveActualFcSec != null -> "Development"
            AppState.liveActualYellowSec != null -> "Maillard / Pre-FC"
            AppState.liveActualTurningSec != null -> "Drying"
            else -> "Idle"
        }
    }

    private fun defaultRorForCurrentState(): Double {
        return when (currentPhase()) {
            "Drying" -> 16.0
            "Maillard / Pre-FC" -> 12.0
            "Development" -> 7.0
            else -> 12.0
        }
    }
}
