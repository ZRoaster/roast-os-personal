package com.roastos.app

data class RoastControlAdvisorOutput(
    val stage: String,
    val priority: String,
    val finalHeatAdvice: String,
    val finalAirflowAdvice: String,
    val flavorDirection: String,
    val riskLevel: String,
    val confidence: String,
    val reason: String
) {

    fun summaryText(): String {
        return """
Stage
$stage

Priority
$priority

Heat
$finalHeatAdvice

Airflow
$finalAirflowAdvice

Flavor Direction
$flavorDirection

Risk
$riskLevel

Confidence
$confidence

Reason
$reason
        """.trimIndent()
    }
}

object RoastControlAdvisorEngine {

    fun evaluate(
        snapshot: RoastSessionBusSnapshot
    ): RoastControlAdvisorOutput {

        val decision = RoastDecisionEngine.evaluate(snapshot)
        val control = RoastControlModel.evaluate(snapshot)
        val ai = RoastAiAssistantEngine.generate()

        val finalHeat = buildHeatAdvice(decision, control)
        val finalAirflow = buildAirflowAdvice(decision, control)

        val risk = ai.riskLevel
        val confidence = decision.confidence

        val reason = buildReason(
            decision = decision,
            control = control,
            ai = ai
        )

        return RoastControlAdvisorOutput(
            stage = decision.stage,
            priority = decision.priority,
            finalHeatAdvice = finalHeat,
            finalAirflowAdvice = finalAirflow,
            flavorDirection = decision.flavorDirection,
            riskLevel = risk,
            confidence = confidence,
            reason = reason
        )
    }

    private fun buildHeatAdvice(
        decision: RoastDecision,
        control: RoastControlAdjustment
    ): String {

        val bias = control.recommendedHeatBiasPercent

        val biasText = when {
            bias >= 5 -> "明显补火 (${formatSignedPercent(bias)})"
            bias >= 2 -> "小幅补火 (${formatSignedPercent(bias)})"
            bias <= -5 -> "明显收火 (${formatSignedPercent(bias)})"
            bias <= -2 -> "小幅收火 (${formatSignedPercent(bias)})"
            else -> "保持基准 (${formatSignedPercent(bias)})"
        }

        return "${decision.heatAction} · $biasText"
    }

    private fun buildAirflowAdvice(
        decision: RoastDecision,
        control: RoastControlAdjustment
    ): String {

        val bias = control.recommendedAirflowBiasSteps

        val biasText = when {
            bias >= 2 -> "明显加风 (+$bias)"
            bias == 1 -> "小幅加风 (+1)"
            bias <= -2 -> "明显减风 ($bias)"
            bias == -1 -> "小幅减风 (-1)"
            else -> "保持基准 (0)"
        }

        return "${decision.airflowAction} · $biasText"
    }

    private fun buildReason(
        decision: RoastDecision,
        control: RoastControlAdjustment,
        ai: RoastAiAssistantOutput
    ): String {

        val parts = mutableListOf<String>()

        parts += decision.rationale

        if (control.reason.isNotBlank()) {
            parts += control.reason
        }

        if (ai.summary.isNotBlank()) {
            parts += ai.summary
        }

        return parts.joinToString("\n\n")
    }

    private fun formatSignedPercent(value: Int): String {
        return if (value >= 0) "+${value}%" else "${value}%"
    }
}
