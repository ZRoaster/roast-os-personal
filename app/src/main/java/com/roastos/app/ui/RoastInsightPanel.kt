package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import com.roastos.app.EnergyEngine
import com.roastos.app.MachineProfiles
import com.roastos.app.MachineStateEngine
import com.roastos.app.RoastInsightEngine
import com.roastos.app.RoastInsightReport
import com.roastos.app.RoastSessionBus
import com.roastos.app.RoastStabilityEngine
import com.roastos.app.RoastCurveEngineV3
import com.roastos.app.UiKit

class RoastInsightPanel(context: Context) : LinearLayout(context) {

    private val headlineBody = UiKit.bodyText(context, "")
    private val observationBody = UiKit.bodyText(context, "")
    private val possibilityBody = UiKit.bodyText(context, "")

    init {
        orientation = VERTICAL
        addView(headlineBody)
        addView(UiKit.spacer(context))
        addView(observationBody)
        addView(UiKit.spacer(context))
        addView(possibilityBody)
        update()
    }

    fun update() {
        val snapshot = RoastSessionBus.peek()

        if (snapshot == null) {
            headlineBody.text = """
Insight
No active roast session.
            """.trimIndent()

            observationBody.text = """
Observations
-
            """.trimIndent()

            possibilityBody.text = """
Possibilities
-
            """.trimIndent()
            return
        }

        val session = snapshot.session

        val machineState = MachineStateEngine.buildState(
            powerW = 0,
            airflowPa = 0,
            drumRpm = 0,
            beanTemp = session.lastBeanTemp,
            ror = session.lastRor,
            elapsedSec = session.lastElapsedSec,
            environmentTemp = 25.0,
            environmentHumidity = 50.0
        )

        val profile = MachineProfiles.HB_M2SE
        val energy = EnergyEngine.evaluate(profile, machineState)

        RoastCurveEngineV3.reset()
        RoastCurveEngineV3.ingest(
            elapsedSec = machineState.elapsedSec,
            bt = machineState.beanTemp
        )
        val curvePrediction = RoastCurveEngineV3.predict()

        val stability = RoastStabilityEngine.evaluate(curvePrediction)

        val report = RoastInsightEngine.analyze(
            profile = profile,
            machineState = machineState,
            energy = energy,
            stability = stability,
            styleGoal = null
        )

        renderReport(report)
    }

    private fun renderReport(report: RoastInsightReport) {
        headlineBody.text = """
Insight

Phase
${report.phaseLabel}

Energy
${report.energyLabel}

Stability
${report.stabilityLabel}

Momentum
${report.momentumLabel}

Quiet Summary
${report.quietSummary}
        """.trimIndent()

        observationBody.text = """
Observations
${buildObservationText(report)}
        """.trimIndent()

        possibilityBody.text = """
Possibilities
${buildPossibilityText(report)}
        """.trimIndent()
    }

    private fun buildObservationText(
        report: RoastInsightReport
    ): String {
        if (report.observations.isEmpty()) return "-"

        return report.observations.take(4).joinToString("\n\n") { item ->
            """
${item.title}
${item.message}

Severity
${formatSeverity(item.severity.name)}

Type
${formatType(item.type.name)}
            """.trimIndent()
        }
    }

    private fun buildPossibilityText(
        report: RoastInsightReport
    ): String {
        if (report.possibilities.isEmpty()) return "-"

        return report.possibilities.take(3).joinToString("\n\n") { item ->
            """
${item.title}

Direction
${formatDirection(item.direction.name)}

Description
${item.description}
            """.trimIndent()
        }
    }

    private fun formatSeverity(value: String): String {
        return when (value) {
            "QUIET" -> "安静"
            "NOTICE" -> "提示"
            "WATCH" -> "留意"
            "ALERT" -> "警报"
            else -> value
        }
    }

    private fun formatType(value: String): String {
        return when (value) {
            "OBSERVATION" -> "观察"
            "CAUSE" -> "原因"
            "CONSEQUENCE" -> "结果"
            "POSSIBILITY" -> "可能性"
            else -> value
        }
    }

    private fun formatDirection(value: String): String {
        return when (value) {
            "CLARITY" -> "清晰"
            "SWEETNESS" -> "甜感"
            "BODY" -> "厚度"
            "BALANCE" -> "平衡"
            "EXPERIMENTAL" -> "实验"
            else -> value
        }
    }
}
