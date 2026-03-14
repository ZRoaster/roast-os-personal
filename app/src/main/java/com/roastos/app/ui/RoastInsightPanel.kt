package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import com.roastos.app.RoastInsightBridge
import com.roastos.app.RoastInsightReport
import com.roastos.app.RoastSessionBus
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

        val report = RoastInsightBridge.analyzeSnapshot(snapshot)
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
