package com.roastos.app

import java.util.Locale
import kotlin.math.abs

data class RoastControlAdvisorOutput(
    val stage: String,
    val priority: String,
    val finalHeatAdvice: String,
    val finalAirflowAdvice: String,
    val flavorDirection: String,
    val riskLevel: String,
    val confidence: String,
    val reason: String,
    val referenceContext: String
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

Reference Context
$referenceContext
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
        val machine = RoastStateModel.machine

        val finalHeat = buildHeatAdvice(decision, control, prediction)
        val finalAirflow = buildAirflowAdvice(decision, control, prediction)

        val risk = buildRisk(ai, prediction)
        val confidence = decision.confidence

        val reason = buildReason(
            decision = decision,
            control = control,
            ai = ai,
            prediction = prediction,
            calibrationMatch = calibrationMatch,
            machine = machine
        )

        val referenceContext = buildReferenceContext(snapshot)

        return RoastControlAdvisorOutput(
            stage = decision.stage,
            priority = decision.priority,
            finalHeatAdvice = finalHeat,
            finalAirflowAdvice = finalAirflow,
            flavorDirection = decision.flavorDirection,
            riskLevel = risk,
            confidence = confidence,
            reason = reason,
            referenceContext = referenceContext
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
        calibrationMatch: RoastCalibrationMatchResult,
        machine: RoastStateModel.MachineState
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

        parts += buildMachineStateBlock(machine)

        if (ai.summary.isNotBlank()) {
            parts += "AI Assistant: ${ai.summary}"
        }

        return parts.joinToString("\n\n")
    }

    private fun buildReferenceContext(
        snapshot: RoastSessionBusSnapshot
    ): String {
        val latest = RoastHistoryEngine.latest()
            ?: return "No latest roast reference available."

        val session = snapshot.session
        val currentElapsed = session.lastElapsedSec
        val currentHealth = buildHealthHeadline(snapshot.validation)
        val currentHealthScore = riskScore(currentHealth)
        val lastHealthScore = riskScore(latest.roastHealthHeadline)

        val parts = mutableListOf<String>()

        if (currentHealthScore > lastHealthScore && currentHealthScore > 0) {
            parts += "Current health is weaker than the last saved roast. Prefer stability over aggressive late-phase carry."
        }

        val lastYellow = latest.actualYellowSec ?: latest.predictedYellowSec
        if (lastYellow != null && currentElapsed >= lastYellow + 20) {
            parts += "Current roast is already slower than last yellow reference. Do not assume the same crack timing without adjustment."
        }

        val lastFc = latest.actualFcSec ?: latest.predictedFcSec
        if (lastFc != null && currentElapsed >= lastFc - 15) {
            parts += "Current roast is already close to last first crack reference. Verify whether the current mid-late phase pace is aligned with intent."
        }

        val currentEnv = AppState.lastPlannerInput
        val currentEnvTemp = currentEnv?.envTemp
        val currentEnvRh = currentEnv?.envRH
        val envShiftDetected =
            currentEnvTemp != null &&
                currentEnvRh != null &&
                (abs(currentEnvTemp - latest.envTemp) >= 1.5 || abs(currentEnvRh - latest.envRh) >= 8.0)

        if (envShiftDetected) {
            parts += "Current environment differs clearly from the last saved roast. Re-check phase expectations before copying the previous rhythm."
        }

        return if (parts.isEmpty()) {
            "No strong reference deviation under current rules."
        } else {
            parts.take(2).joinToString("\n\n")
        }
    }

    private fun buildMachineStateBlock(
        machine: RoastStateModel.MachineState
    ): String {
        return """
Current Synced Machine State:
Power Response Delay: ${formatDouble(machine.powerResponseDelay)} s
Airflow Response Delay: ${formatDouble(machine.airflowResponseDelay)} s
RPM Response Delay: ${formatDouble(machine.rpmResponseDelay)} s
Thermal Mass: ${formatDouble(machine.thermalMass)}
Heat Retention: ${formatDouble(machine.heatRetention)}
Drum Mass: ${formatDouble(machine.drumMass)}
Max Power: ${machine.maxPowerW} W
Max Air: ${machine.maxAirPa} Pa
Max RPM: ${machine.maxRpm}
        """.trimIndent()
    }

    private fun buildHealthHeadline(
        validation: RoastValidationResult
    ): String {
        if (!validation.hasIssues()) return "稳定"

        return when (validation.highestSeverity()) {
            "high" -> "高风险"
            "medium" -> "中风险"
            "watch" -> "需留意"
            "low" -> "低风险"
            else -> "稳定"
        }
    }

    private fun riskScore(headline: String): Int {
        val text = headline.lowercase(Locale.getDefault())
        return when {
            "高风险" in headline -> 4
            "中风险" in headline -> 3
            "需留意" in headline -> 2
            "低风险" in headline -> 1
            "high" in text -> 4
            "medium" in text -> 3
            "watch" in text -> 2
            "low" in text -> 1
            else -> 0
        }
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

    private fun formatDouble(value: Double): String {
        return "%.2f".format(value)
    }
}
