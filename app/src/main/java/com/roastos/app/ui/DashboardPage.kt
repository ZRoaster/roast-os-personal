package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import android.widget.ScrollView
import com.roastos.app.AppState
import com.roastos.app.BatchSessionEngine
import com.roastos.app.DecisionEngine
import com.roastos.app.RoastEngine
import com.roastos.app.RoastStateModel
import com.roastos.app.RoastTimelineStore

object DashboardPage {

    private const val DEFAULT_POWER_W = 540
    private const val DEFAULT_AIRFLOW_PA = 10
    private const val DEFAULT_DRUM_RPM = 60

    fun show(context: Context, container: LinearLayout) {
        container.removeAllViews()

        val scroll = ScrollView(context)
        val root = UiKit.pageRoot(context)

        root.addView(UiKit.pageTitle(context, "ROAST OS"))
        root.addView(UiKit.pageSubtitle(context, "Main Control Dashboard"))
        root.addView(UiKit.spacer(context))

        root.addView(UiKit.buildCard(context, "PLANNER BASELINE", buildPlannerCard()))
        root.addView(UiKit.spacer(context))

        root.addView(UiKit.buildCard(context, "BATCH SESSION", buildSessionCard()))
        root.addView(UiKit.spacer(context))

        root.addView(UiKit.buildCard(context, "ROAST TIMELINE", buildTimelineCard()))
        root.addView(UiKit.spacer(context))

        root.addView(UiKit.buildCard(context, "DECISION CENTER", buildDecisionCard()))
        root.addView(UiKit.spacer(context))

        root.addView(UiKit.buildCard(context, "ADAPTIVE CALIBRATION", buildCalibrationCard()))

        scroll.addView(root)
        container.addView(scroll)
    }

    private fun buildPlannerCard(): String {
        val planner = AppState.lastPlannerResult ?: return "No planner result available"
        val plannerInput = AppState.lastPlannerInput ?: return "No planner input available"

        val predTurning = (planner.h1Sec - 60.0).toInt().coerceAtLeast(50)
        val predYellow = planner.h2Sec.toInt()
        val predFc = planner.fcPredSec.toInt()
        val predDrop = planner.dropSec.toInt()

        return """
Process
${plannerInput.process}

Bean
Density   ${"%.1f".format(plannerInput.density)}
Moisture  ${"%.1f".format(plannerInput.moisture)}
aw        ${"%.2f".format(plannerInput.aw)}

Environment
Temp      ${"%.1f".format(plannerInput.envTemp)}℃
RH        ${"%.1f".format(plannerInput.envRH)}%

Predicted Anchors
Turning   ${RoastEngine.toMMSS(predTurning.toDouble())}
Yellow    ${RoastEngine.toMMSS(predYellow.toDouble())}
FC        ${RoastEngine.toMMSS(predFc.toDouble())}
Drop      ${RoastEngine.toMMSS(predDrop.toDouble())}
        """.trimIndent()
    }

    private fun buildSessionCard(): String {
        return BatchSessionEngine.summary()
    }

    private fun buildTimelineCard(): String {
        val tl = RoastTimelineStore.current

        return """
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

    private fun buildDecisionCard(): String {
        val planner = AppState.lastPlannerResult ?: return "No planner result available"
        val plannerInput = AppState.lastPlannerInput ?: return "No planner input available"

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
AppState
FC Bias      ${"%.1f".format(appCalibration.fcBiasSec)}
Drop Bias    ${"%.1f".format(appCalibration.dropBiasSec)}
Heat Bias    ${"%.2f".format(appCalibration.heatResponseBias)}
Air Bias     ${"%.2f".format(appCalibration.airResponseBias)}
Bean Bias    ${"%.2f".format(appCalibration.beanLoadBias)}
Learn Count  ${appCalibration.learningCount}

RoastStateModel
FC Bias      ${"%.1f".format(modelCalibration.fcBias)}
Drop Bias    ${"%.1f".format(modelCalibration.dropBias)}
ROR Bias     ${"%.1f".format(modelCalibration.rorBias)}
Heat Bias    ${"%.2f".format(modelCalibration.heatBias)}
Air Bias     ${"%.2f".format(modelCalibration.airBias)}
Bean Bias    ${"%.2f".format(modelCalibration.beanBias)}
Machine Resp ${"%.2f".format(modelCalibration.machineResponseFactor)}
Learn Count  ${modelCalibration.learningCount}
        """.trimIndent()
    }
}
