package com.roastos.app.ui

import com.roastos.app.AppState
import com.roastos.app.BatchSessionEngine
import com.roastos.app.DecisionEngine
import com.roastos.app.RoastEngine
import com.roastos.app.RoastStateModel
import com.roastos.app.RoastTimelineStore

object LiveAssistPage {

    private const val DEFAULT_POWER_W = 540
    private const val DEFAULT_AIRFLOW_PA = 10
    private const val DEFAULT_DRUM_RPM = 60

    fun buildLiveAssist(): String {
        val planner = AppState.lastPlannerResult ?: return """
LIVE ASSIST

No planner result available
Go to Planner first.
        """.trimIndent()

        val plannerInput = AppState.lastPlannerInput ?: return """
LIVE ASSIST

No planner input available
Go to Planner first.
        """.trimIndent()

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

        RoastTimelineStore.syncPredicted(
            turningSec = predTurning,
            yellowSec = predYellow,
            fcSec = predFc,
            dropSec = predDrop
        )

        RoastTimelineStore.syncActual(
            turningSec = AppState.liveActualTurningSec,
            yellowSec = AppState.liveActualYellowSec,
            fcSec = AppState.liveActualFcSec,
            dropSec = AppState.liveActualDropSec,
            ror = AppState.liveActualPreFcRor
        )

        BatchSessionEngine.syncLiveData(
            turningSec = AppState.liveActualTurningSec,
            yellowSec = AppState.liveActualYellowSec,
            fcSec = AppState.liveActualFcSec,
            dropSec = AppState.liveActualDropSec,
            ror = AppState.liveActualPreFcRor
        )

        return """
LIVE EXECUTION OVERVIEW

${buildPlannerBaselineCard(predTurning, predYellow, predFc, predDrop)}

${buildTimelineCard()}

${buildDecisionCard(predTurning, predYellow, predFc, predDrop)}

${buildControlCard(predTurning, predYellow, predFc, predDrop)}

${buildSessionCard()}
        """.trimIndent()
    }

    private fun buildPlannerBaselineCard(
        predTurning: Int,
        predYellow: Int,
        predFc: Int,
        predDrop: Int
    ): String {
        return """
━━━━━━━━━━━━━━━━━━
PLANNER BASELINE
━━━━━━━━━━━━━━━━━━
Turning   ${RoastEngine.toMMSS(predTurning.toDouble())}
Yellow    ${RoastEngine.toMMSS(predYellow.toDouble())}
FC        ${RoastEngine.toMMSS(predFc.toDouble())}
Drop      ${RoastEngine.toMMSS(predDrop.toDouble())}
        """.trimIndent()
    }

    private fun buildTimelineCard(): String {
        val tl = RoastTimelineStore.current

        return """
━━━━━━━━━━━━━━━━━━
ROAST TIMELINE
━━━━━━━━━━━━━━━━━━
Predicted
Turning   ${tl.predicted.turningSec?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}
Yellow    ${tl.predicted.yellowSec?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}
FC        ${tl.predicted.fcSec?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}
Drop      ${tl.predicted.dropSec?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}

Actual
Turning   ${tl.actual.turningSec?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}
Yellow    ${tl.actual.yellowSec?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}
FC        ${tl.actual.fcSec?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}
Drop      ${tl.actual.dropSec?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}

Current
Phase     ${tl.currentPhase}
ROR       ${tl.currentRor?.let { "%.1f".format(it) } ?: "-"}
Dev       ${tl.devSec?.toString() ?: "-"}
DTR       ${tl.dtrPercent?.let { "%.1f".format(it) + "%" } ?: "-"}
        """.trimIndent()
    }

    private fun buildDecisionCard(
        predTurning: Int,
        predYellow: Int,
        predFc: Int,
        predDrop: Int
    ): String {
        val plannerInput = AppState.lastPlannerInput ?: return """
━━━━━━━━━━━━━━━━━━
DECISION CENTER
━━━━━━━━━━━━━━━━━━
Planner not initialized
        """.trimIndent()

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
━━━━━━━━━━━━━━━━━━
DECISION CENTER
━━━━━━━━━━━━━━━━━━
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

Physics / Energy
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
━━━━━━━━━━━━━━━━━━
CONTROL ANALYSIS
━━━━━━━━━━━━━━━━━━
Current Stage
$currentStage

Predicted Anchors
Turning   ${RoastEngine.toMMSS(predTurning.toDouble())}
Yellow    ${RoastEngine.toMMSS(predYellow.toDouble())}
FC        ${RoastEngine.toMMSS(predFc.toDouble())}
Drop      ${RoastEngine.toMMSS(predDrop.toDouble())}

Actual Anchors
Turning   ${actualTurning?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}
Yellow    ${actualYellow?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}
FC        ${actualFc?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}
Drop      ${actualDrop?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}
Pre-FC ROR ${actualRor?.let { "%.1f".format(it) } ?: "-"}

Next Action
$nextAction

Biggest Risk
$biggestRisk
        """.trimIndent()
    }

    private fun buildSessionCard(): String {
        val session = BatchSessionEngine.current()
        return if (session == null) {
            """
━━━━━━━━━━━━━━━━━━
BATCH SESSION
━━━━━━━━━━━━━━━━━━
No active session
            """.trimIndent()
        } else {
            """
━━━━━━━━━━━━━━━━━━
BATCH SESSION
━━━━━━━━━━━━━━━━━━
Batch ID
${session.batchId}

Status
${session.status}

Bean Snapshot
Process   ${session.beanSnapshot.process}
Density   ${"%.1f".format(session.beanSnapshot.density)}
Moisture  ${"%.1f".format(session.beanSnapshot.moisture)}
aw        ${"%.2f".format(session.beanSnapshot.aw)}

Environment
Temp      ${"%.1f".format(session.envSnapshot.tempC)}℃
RH        ${"%.1f".format(session.envSnapshot.humidityRh)}%

Planner Snapshot
Charge    ${session.plannerSnapshot.chargeTemp}℃
            """.trimIndent()
        }
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
