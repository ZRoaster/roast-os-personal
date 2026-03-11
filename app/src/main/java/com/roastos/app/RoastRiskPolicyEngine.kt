package com.roastos.app

data class RoastRiskPolicy(
    val issueCode: String,
    val level: String,
    val allowOverride: Boolean,
    val requireLog: Boolean,
    val suggestedHeatAction: String,
    val suggestedAirflowAction: String,
    val operatorMessage: String
)

data class RoastRiskDecisionBundle(
    val hasRisk: Boolean,
    val highestLevel: String,
    val policies: List<RoastRiskPolicy>
) {
    fun summary(): String {
        if (policies.isEmpty()) {
            return """
Risk Policy

Level
none

Action
Continue
            """.trimIndent()
        }

        return buildString {
            appendLine("Risk Policy")
            appendLine()
            appendLine("Level")
            appendLine(highestLevel)
            appendLine()

            policies.forEachIndexed { index, policy ->
                appendLine("Issue ${index + 1}")
                appendLine(policy.issueCode)
                appendLine()
                appendLine("Override")
                appendLine(if (policy.allowOverride) "Allowed" else "Blocked")
                appendLine()
                appendLine("Heat")
                appendLine(policy.suggestedHeatAction)
                appendLine()
                appendLine("Airflow")
                appendLine(policy.suggestedAirflowAction)
                appendLine()
                appendLine("Message")
                appendLine(policy.operatorMessage)

                if (index != policies.lastIndex) {
                    appendLine()
                }
            }
        }
    }
}

object RoastRiskPolicyEngine {

    fun evaluate(
        validation: RoastValidationResult
    ): RoastRiskDecisionBundle {

        if (!validation.hasIssues()) {
            return RoastRiskDecisionBundle(
                hasRisk = false,
                highestLevel = "none",
                policies = emptyList()
            )
        }

        val policies = validation.issues.map { issue ->
            when (issue.code) {

                "stall" -> RoastRiskPolicy(
                    issueCode = issue.code,
                    level = "soft",
                    allowOverride = true,
                    requireLog = true,
                    suggestedHeatAction = "小幅加火",
                    suggestedAirflowAction = "暂不继续增风",
                    operatorMessage = "检测到失速风险。建议先恢复动能。若继续实验，请记录风险。"
                )

                "low_energy" -> RoastRiskPolicy(
                    issueCode = issue.code,
                    level = "soft",
                    allowOverride = true,
                    requireLog = true,
                    suggestedHeatAction = "轻微补火",
                    suggestedAirflowAction = "避免风门过大",
                    operatorMessage = "检测到中段能量不足。可继续实验，但建议记录该次操作。"
                )

                "high_energy" -> RoastRiskPolicy(
                    issueCode = issue.code,
                    level = "soft",
                    allowOverride = true,
                    requireLog = true,
                    suggestedHeatAction = "收一点火",
                    suggestedAirflowAction = "视情况轻微增风",
                    operatorMessage = "检测到中段推进偏强。可继续实验，但需留意后段粗糙风险。"
                )

                "flick" -> RoastRiskPolicy(
                    issueCode = issue.code,
                    level = "soft",
                    allowOverride = true,
                    requireLog = true,
                    suggestedHeatAction = "停止继续推火",
                    suggestedAirflowAction = "保持或轻微增风",
                    operatorMessage = "检测到后段过冲风险。允许继续实验，但建议记录并在杯测后回看。"
                )

                "crash" -> RoastRiskPolicy(
                    issueCode = issue.code,
                    level = "hard",
                    allowOverride = true,
                    requireLog = true,
                    suggestedHeatAction = "不要继续减火",
                    suggestedAirflowAction = "保持稳定排气",
                    operatorMessage = "检测到尾段塌陷风险。强烈建议修正；若坚持继续，必须记录本次 override。"
                )

                else -> RoastRiskPolicy(
                    issueCode = issue.code,
                    level = "soft",
                    allowOverride = true,
                    requireLog = true,
                    suggestedHeatAction = "观察",
                    suggestedAirflowAction = "观察",
                    operatorMessage = "检测到未分类风险。建议记录后复盘。"
                )
            }
        }

        return RoastRiskDecisionBundle(
            hasRisk = true,
            highestLevel = highestLevel(policies),
            policies = policies
        )
    }

    private fun highestLevel(
        policies: List<RoastRiskPolicy>
    ): String {
        return when {
            policies.any { it.level == "hard" } -> "hard"
            policies.any { it.level == "soft" } -> "soft"
            else -> "none"
        }
    }
}
