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
        val prediction = RoastRorPredictionEngine.evaluate(snapshot)
        val calibrationMatch = RoastCalibrationMatcherEngine.matchBest()

        val finalHeat = buildHeatAdvice(decision, control, prediction)
        val finalAirflow = buildAirflowAdvice(decision, control, prediction)

        val risk = buildRisk(ai, prediction)
        val confidence = decision.confidence

        val reason = buildReason(
            decision = decision,
            control = control,
            ai = ai,
            prediction = prediction,
            calibrationMatch = calibrationMatch
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
        control: RoastControlAdjustment,
        prediction: RoastRorPrediction
    ): String {

        val bias = control.recommendedHeatBiasPercent

        val biasText = when {
            bias >= 5 -> "明显补火 (${formatSignedPercent(bias)})"
            bias >= 2 -> "小幅补火 (${formatSignedPercent(bias)})"
            bias <= -5 -> "明显收火 (${formatSignedPercent(bias)})"
            bias <= -2 -> "小幅收火 (${formatSignedPercent(bias)})"
            else -> "保持基准 (${formatSignedPercent(bias)})"
        }

        val predictionText = when (prediction.predictedRisk) {
            "stall risk" -> "；预测显示存在失速风险"
            "flick risk" -> "；预测显示存在后段反弹风险"
            "possible overshoot" -> "；预测显示可能推进过猛"
            else -> ""
        }

        return "${decision.heatAction} · $biasText$predictionText"
    }

    private fun buildAirflowAdvice(
        decision: RoastDecision,
        control: RoastControlAdjustment,
        prediction: RoastRorPrediction
    ): String {

        val bias = control.recommendedAirflowBiasSteps

        val biasText = when {
            bias >= 2 -> "明显加风 (+$bias)"
            bias == 1 -> "小幅加风 (+1)"
            bias <= -2 -> "明显减风 ($bias)"
            bias == -1 -> "小幅减风 (-1)"
            else -> "保持基准 (0)"
        }

        val predictionText = when (prediction.predictedRisk) {
            "flick risk" -> "；配合排气抑制后段上冲"
            "possible overshoot" -> "；配合控制推进速度"
            "stall risk" -> "；避免过度加风削弱热量积累"
            else -> ""
        }

        return "${decision.airflowAction} · $biasText$predictionText"
    }

    private fun buildRisk(
        ai: RoastAiAssistantOutput,
        prediction: RoastRorPrediction
    ): String {
        return when {
            prediction.predictedRisk == "stall risk" -> "watch"
            prediction.predictedRisk == "flick risk" -> "watch"
            prediction.predictedRisk == "possible overshoot" -> "watch"
            else -> ai.riskLevel
        }
    }

    private fun buildReason(
        decision: RoastDecision,
        control: RoastControlAdjustment,
        ai: RoastAiAssistantOutput,
        prediction: RoastRorPrediction,
        calibrationMatch: RoastCalibrationMatchResult
    ): String {

        val parts = mutableListOf<String>()

        parts += decision.rationale

        if (control.reason.isNotBlank()) {
            parts += "Control Model: ${control.reason}"
        }

        if (prediction.reason.isNotBlank()) {
            parts += "RoR Prediction: ${prediction.reason}"
        }

        prediction.estimatedFirstCrackWindowSec?.let {
            parts += "Estimated First Crack Window: ${formatSec(it)}"
        }

        calibrationMatch.matchedProfile?.let { matched ->
            parts += "Matched Calibration: ${matched.machineName} / ${matched.calibrationId}"
            parts += "Calibration Match Score: ${formatScore(calibrationMatch.score)}"
        }

        if (calibrationMatch.reason.isNotBlank()) {
            parts += "Calibration Match Reason: ${calibrationMatch.reason}"
        }

        if (ai.summary.isNotBlank()) {
            parts += "AI Assistant: ${ai.summary}"
        }

        return parts.joinToString("\n\n")
    }

    private fun formatSignedPercent(value: Int): String {
        return if (value >= 0) "+${value}%" else "${value}%"
    }

    private fun formatSec(value: Int): String {
        val m = value / 60
        val s = value % 60
        return "%d:%02d".format(m, s)
    }

    private fun formatScore(value: Double): String {
        return "%.2f".format(value)
    }
}
