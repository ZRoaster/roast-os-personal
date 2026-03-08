package com.roastos.app.ui

import android.content.Context
import android.graphics.Typeface
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
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
        val root = LinearLayout(context)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(24, 24, 24, 24)

        root.addView(buildHeaderCard(context))
        root.addView(buildCard(context, "PLANNER BASELINE", buildPlannerCard()))
        root.addView(buildCard(context, "BATCH SESSION", buildSessionCard()))
        root.addView(buildCard(context, "ROAST TIMELINE", buildTimelineCard()))
        root.addView(buildCard(context, "DECISION CENTER", buildDecisionCard()))
        root.addView(buildCard(context, "ADAPTIVE CALIBRATION", buildCalibrationCard()))

        scroll.addView(root)
        container.addView(scroll)
    }

    private fun buildHeaderCard(context: Context): LinearLayout {
        val card = LinearLayout(context)
        card.orientation = LinearLayout.VERTICAL
        card.setPadding(24, 24, 24, 24)

        val title = TextView(context)
        title.text = "ROAST OS"
        title.textSize = 24f
        title.setTypeface(null, Typeface.BOLD)

        val subtitle = TextView(context)
        subtitle.text = "Main Control Dashboard"
        subtitle.textSize = 14f

        val phase = RoastTimelineStore.current.currentPhase
        val ror = RoastTimelineStore.current.currentRor?.let { "%.1f".format(it) } ?: "-"
        val learningCount = RoastStateModel.calibration.learningCount

        val status = TextView(context)
        status.text = """
Current Phase  $phase
Current ROR    $ror
Learning Count $learningCount
        """.trimIndent()
        status.textSize = 16f
        status.setPadding(0, 20, 0, 0)

        card.addView(title)
        card.addView(subtitle)
        card.addView(status)
        return card
    }

    private fun buildCard(
        context: Context,
        heading: String,
        content: String
    ): LinearLayout {
        val card = LinearLayout(context)
        card.orientation = LinearLayout.VERTICAL
        card.setPadding(24, 24, 24, 24)

        val title = TextView(context)
        title.text = heading
        title.textSize = 18f
        title.setTypeface(null, Typeface.BOLD)

        val body = TextView(context)
        body.text = content
        body.textSize = 15f
        body.setPadding(0, 16, 0, 0)

        card.addView(title)
        card.addView(body)
        return card
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

Development
Dev       ${planner.devTime}s
DTR       ${"%.1f".format(planner.dtrPercent)}%
        """.trimIndent()
    }

    private fun buildSessionCard(): String {
        val session = BatchSessionEngine.current() ?: return "No active session"

        return """
Batch ID
${session.batchId}

Status
${session.status}

Bean Snapshot
Process   ${session.beanSnapshot.process}
Density   ${"%.1f".format(session.beanSnapshot.density)}
Moisture  ${"%.1f".format(session.beanSnapshot.moisture)}
aw        ${"%.2f".format(session.beanSnapshot.aw)}

Environment Snapshot
Temp      ${"%.1f".format(session.envSnapshot.tempC)}℃
RH        ${"%.1f".format(session.envSnapshot.humidityRh)}%

Planner Snapshot
Charge    ${session.plannerSnapshot.chargeTemp}℃
Turning   ${session.plannerSnapshot.predictedTurningSec?.toString() ?: "-"}
Yellow    ${session.plannerSnapshot.predictedYellowSec?.toString() ?: "-"}
FC        ${session.plannerSnapshot.predictedFcSec?.toString() ?: "-"}
Drop      ${session.plannerSnapshot.predictedDropSec?.toString() ?: "-"}

Notes
${if (session.notes.isBlank()) "-" else session.notes}
        """.trimIndent()
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

Derived
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
