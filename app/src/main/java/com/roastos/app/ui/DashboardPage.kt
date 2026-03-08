package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.roastos.app.AppState
import com.roastos.app.DecisionEngine
import com.roastos.app.RoastEngine
import com.roastos.app.RoastStateModel

object DashboardPage {

    private const val DEFAULT_POWER_W = 540
    private const val DEFAULT_AIRFLOW_PA = 10
    private const val DEFAULT_DRUM_RPM = 60

    fun show(context: Context, container: LinearLayout) {
        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = LinearLayout(context)
        root.orientation = LinearLayout.VERTICAL

        val title = TextView(context)
        title.text = "ROAST OS"
        title.textSize = 24f

        val subtitle = TextView(context)
        subtitle.text = "Main Control Dashboard"

        val plannerCard = TextView(context)
        plannerCard.text = buildPlannerCard()

        val roastCard = TextView(context)
        roastCard.text = buildRoastStateCard()

        val decisionCard = TextView(context)
        decisionCard.text = buildDecisionCard()

        val calibrationCard = TextView(context)
        calibrationCard.text = buildCalibrationCard()

        root.addView(title)
        root.addView(subtitle)
        root.addView(plannerCard)
        root.addView(roastCard)
        root.addView(decisionCard)
        root.addView(calibrationCard)

        scroll.addView(root)
        container.addView(scroll)
    }

    private fun buildPlannerCard(): String {
        val planner = AppState.lastPlannerResult ?: return """
Planner
No planner result available
        """.trimIndent()

        val plannerInput = AppState.lastPlannerInput ?: return """
Planner
No planner input available
        """.trimIndent()

        val predTurning = (planner.h1Sec - 60.0).toInt().coerceAtLeast(50)
        val predYellow = planner.h2Sec.toInt()
        val predFc = planner.fcPredSec.toInt()
        val predDrop = planner.dropSec.toInt()

        return """
Planner Baseline

Process
${plannerInput.process}

Bean
Density ${"%.1f".format(plannerInput.density)}
Moisture ${"%.1f".format(plannerInput.moisture)}
aw ${"%.2f".format(plannerInput.aw)}

Environment
Temp ${"%.1f".format(plannerInput.envTemp)}℃
RH ${"%.1f".format(plannerInput.envRH)}%

Predicted Anchors
Turning ${RoastEngine.toMMSS(predTurning.toDouble())}
Yellow ${RoastEngine.toMMSS(predYellow.toDouble())}
FC ${RoastEngine.toMMSS(predFc.toDouble())}
Drop ${RoastEngine.toMMSS(predDrop.toDouble())}

Development
Dev ${planner.devTime}s
DTR ${"%.1f".format(planner.dtrPercent)}%
        """.trimIndent()
    }

    private fun buildRoastStateCard(): String {
        val planner = AppState.lastPlannerResult
        val predTurning = planner?.let { (it.h1Sec - 60.0).toInt().coerceAtLeast(50) }
        val predYellow = planner?.h2Sec?.toInt()
        val predFc = planner?.fcPredSec?.toInt()
        val predDrop = planner?.dropSec?.toInt()

        val actualTurning = AppState.liveActualTurningSec
        val actualYellow = AppState.liveActualYellowSec
        val actualFc = AppState.liveActualFcSec
        val actualDrop = AppState.liveActualDropSec
        val actualRor = AppState.liveActualPreFcRor

        val phase = when {
            actualDrop != null -> "Finished"
            actualFc != null -> "Development"
            actualYellow != null -> "Maillard / Pre-FC"
            actualTurning != null -> "Drying"
            else -> "Idle"
        }

        return """
Roast State

Current Phase
$phase

Current ROR
${actualRor?.let { "%.1f".format(it) } ?: "-"}

Actual Anchors
Turning ${actualTurning?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}
Yellow ${actualYellow?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}
FC ${actualFc?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}
Drop ${actualDrop?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}

Predicted Anchors
Turning ${predTurning?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}
Yellow ${predYellow?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}
FC ${predFc?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}
Drop ${predDrop?.let { RoastEngine.toMMSS(it.toDouble()) } ?: "-"}

State Model
Bean Density ${"%.1f".format(RoastStateModel.bean.density)}
Bean Moisture ${"%.1f".format(RoastStateModel.bean.moisture)}
Ambient Temp ${"%.1f".format(RoastStateModel.environment.ambientTemp)}℃
Power ${RoastStateModel.control.powerW}W
Air ${RoastStateModel.control.airflowPa}Pa
Drum ${RoastStateModel.control.drumRpm}rpm
        """.trimIndent()
    }

    private fun buildDecisionCard(): String {
        val planner = AppState.lastPlannerResult ?: return """
Decision
No planner result available
        """.trimIndent()

        val plannerInput = AppState.lastPlannerInput ?: return """
Decision
No planner input available
        """.trimIndent()

        val predTurning = (planner.h1Sec - 60.0).toInt().coerceAtLeast(50)
        val predYellow = planner.h2Sec.toInt()
        val predFc = planner.fcPredSec.toInt()
        val predDrop = planner.dropSec.toInt()

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
            heatLevelW = if (RoastStateModel.control.powerW > 0) RoastStateModel.control.powerW else DEFAULT_POWER_W,
            airflowPa = if (RoastStateModel.control.airflowPa > 0) RoastStateModel.control.airflowPa else DEFAULT_AIRFLOW_PA,
            drumRpm = if (RoastStateModel.control.drumRpm > 0) RoastStateModel.control.drumRpm else DEFAULT_DRUM_RPM
        )

        return """
Decision Center

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

    private fun buildCalibrationCard(): String {
        val appCalibration = AppState.calibrationState
        val modelCalibration = RoastStateModel.calibration

        return """
Adaptive Calibration

AppState
FC Bias ${"%.1f".format(appCalibration.fcBiasSec)}
Drop Bias ${"%.1f".format(appCalibration.dropBiasSec)}
Heat Bias ${"%.2f".format(appCalibration.heatResponseBias)}
Air Bias ${"%.2f".format(appCalibration.airResponseBias)}
Bean Bias ${"%.2f".format(appCalibration.beanLoadBias)}
Learning Count ${appCalibration.learningCount}

RoastStateModel
FC Bias ${"%.1f".format(modelCalibration.fcBias)}
Drop Bias ${"%.1f".format(modelCalibration.dropBias)}
ROR Bias ${"%.1f".format(modelCalibration.rorBias)}
Heat Bias ${"%.2f".format(modelCalibration.heatBias)}
Air Bias ${"%.2f".format(modelCalibration.airBias)}
Bean Bias ${"%.2f".format(modelCalibration.beanBias)}
Machine Response ${"%.2f".format(modelCalibration.machineResponseFactor)}
Learning Count ${modelCalibration.learningCount}
        """.trimIndent()
    }
}
