package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import com.roastos.app.EnergyEngine
import com.roastos.app.MachineProfiles
import com.roastos.app.MachineStateEngine
import com.roastos.app.RoastCurveEngineV3
import com.roastos.app.RoastInsightEngine
import com.roastos.app.RoastSessionBus
import com.roastos.app.RoastStabilityEngine
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
            powerW = session.lastPower,
            airflowPa = session.lastAirflow,
            drumRpm = session.lastDrumRpm,
            beanTemp = session.lastBeanTemp,
            ror = session.lastRor,
            elapsedSec = session.lastElapsedSec,
            environmentTemp = session.envTemp,
            environmentHumidity = session.envRH
        )

        val profile = MachineProfiles.HB_M2SE
        val energy = EnergyEngine.evaluate(profile, machineState)
        val curvePrediction = RoastCurveEngineV3.predict(profile, machineState, energy)
        val stability = RoastStabilityEngine.evaluate(curvePrediction)

        val report = RoastInsightEngine.analyze(
            profile = profile,
            machineState = machineState,
            energy = energy,
            stability = stability,
            styleGoal = null
        )

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
        report: com.roastos.app.RoastInsightReport
    ): String {
        if (report.observations.isEmpty()) return "-"

        return report.observations.take(4).joinToString("\n\n") { insight ->
            """
${insight.title}
${insight.message}
等级：${formatSeverity(insight.severity.name)}
类型：${formatType(insight.type.name)}
            """.trimIndent()
        }
    }

    private fun buildPossibilityText(
        report: com.roastos.app.RoastInsightReport
    ): String {
        if (report.possibilities.isEmpty()) return "-"

        return report.possibilities.take(3).joinToString("\n\n") { possibility ->
            """
${possibility.title}
方向：${formatDirection(possibility.direction.name)}
${possibility.description}
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
            "CONSEQUENCE" -> "后果"
            "POSSIBILITY" -> "可能性"
            else -> value
        }
    }

    private fun formatDirection(value: String): String {
        return when (value) {
            "CLARITY" -> "清晰"
            "SWEETNESS" -> "甜感"
            "BODY" -> "醇厚"
            "BALANCE" -> "平衡"
            "EXPERIMENTAL" -> "实验"
            else -> value
        }
    }
}
