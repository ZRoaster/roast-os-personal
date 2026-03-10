package com.roastos.app.ui

import android.content.Context
import android.widget.LinearLayout
import com.roastos.app.RoastSessionBus
import com.roastos.app.UiKit

class RoastInsightPanel(context: Context) : LinearLayout(context) {

    private val companionBody = UiKit.bodyText(context, "")
    private val validationBody = UiKit.bodyText(context, "")

    init {
        orientation = VERTICAL
        addView(companionBody)
        addView(UiKit.spacer(context))
        addView(validationBody)
        update()
    }

    fun update() {
        val snapshot = RoastSessionBus.tick()

        companionBody.text =
            """
${snapshot.companion.title}

${snapshot.companion.body}

阶段
${snapshot.companion.phaseLabel}

风险
${formatRisk(snapshot.companion.riskLevel)}
            """.trimIndent()

        validationBody.text = buildValidationText(snapshot.validation)
    }

    private fun buildValidationText(
        validation: com.roastos.app.RoastValidationResult
    ): String {
        if (!validation.hasIssues()) {
            return """
Roast Health
稳定

说明
当前没有检测到明显风险。
            """.trimIndent()
        }

        val issuesText = validation.issues.joinToString("\n\n") { issue ->
            """
${issue.title}
${issue.detail}
等级：${formatRisk(issue.severity)}
            """.trimIndent()
        }

        return """
Roast Health
${formatRisk(validation.highestSeverity())}

$issuesText
        """.trimIndent()
    }

    private fun formatRisk(risk: String): String {
        return when (risk) {
            "none" -> "无"
            "low" -> "低"
            "watch" -> "留意"
            "medium" -> "中"
            "high" -> "高"
            else -> risk
        }
    }
}
