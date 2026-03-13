package com.roastos.app

import kotlin.math.abs

data class RoastAiAssistantOutput(
    val title: String,
    val summary: String,
    val heatAdvice: String,
    val airflowAdvice: String,
    val timingAdvice: String,
    val riskLevel: String,
    val confidence: String
) {
    fun summaryText(): String {
        return """
$title

Summary
$summary

Heat
$heatAdvice

Airflow
$airflowAdvice

Timing
$timingAdvice

Risk
$riskLevel

Confidence
$confidence
        """.trimIndent()
    }
}

object RoastAiAssistantEngine {

    fun generate(
        context: RoastAiContext = RoastAiRealtimeContextBuilder.build()
    ): RoastAiAssistantOutput {

        val comp = context.environmentCompensation
        val control = RoastControlModel.evaluate()

        val dataCompleteness = evaluateCompleteness(context)
        val riskLevel = evaluateRisk(comp, dataCompleteness)

        val heatAdvice = buildHeatAdvice(comp, control, dataCompleteness)
        val airflowAdvice = buildAirflowAdvice(comp, control, dataCompleteness)
        val timingAdvice = buildTimingAdvice(comp, dataCompleteness)
        val summary = buildSummary(
            context = context,
            control = control,
            completeness = dataCompleteness,
            heatAdvice = heatAdvice,
            airflowAdvice = airflowAdvice,
            timingAdvice = timingAdvice
        )

        val confidence = when (dataCompleteness) {
            "high" -> "guided-local"
            "medium" -> "partial-local"
            else -> "limited-local"
        }

        return RoastAiAssistantOutput(
            title = "Roast AI Assistant",
            summary = summary,
            heatAdvice = heatAdvice,
            airflowAdvice = airflowAdvice,
            timingAdvice = timingAdvice,
            riskLevel = riskLevel,
            confidence = confidence
        )
    }

    private fun evaluateCompleteness(
        context: RoastAiContext
    ): String {
        var score = 0

        if (context.environmentProfile != null) score += 1
        if (context.environmentCompensation != null) score += 1
        if (context.userPrompt.isNotBlank()) score += 1
        if (context.machineProfile != null) score += 1
        if (context.machineState != null) score += 1
        if (context.telemetryFrame != null) score += 1

        return when {
            score >= 5 -> "high"
            score >= 3 -> "medium"
            else -> "low"
        }
    }

    private fun evaluateRisk(
        comp: EnvironmentCompensationResult?,
        completeness: String
    ): String {
        if (comp == null) {
            return if (completeness == "low") "watch" else "low"
        }

        val maxOffset = maxOf(
            abs(comp.heatRetentionOffset),
            abs(comp.dryingOffset),
            abs(comp.airflowEfficiencyOffset),
            abs(comp.pressureOffset)
        )

        return when {
            maxOffset >= 1.20 -> "medium"
            maxOffset >= 0.60 -> "watch"
            completeness == "low" -> "watch"
            else -> "low"
        }
    }

    private fun buildHeatAdvice(
        comp: EnvironmentCompensationResult?,
        control: RoastControlAdjustment,
        completeness: String
    ): String {
        val baseAdvice = if (comp == null) {
            "No compensation data available; keep heat close to baseline."
        } else {
            when {
                comp.heatRetentionOffset <= -1.0 ->
                    "Heat retention looks weak. Support heat slightly more than baseline, but avoid abrupt increases."

                comp.heatRetentionOffset <= -0.5 ->
                    "Heat retention is a bit weak. Consider mild heat support if roast momentum fades."

                comp.heatRetentionOffset >= 1.0 ->
                    "Heat retention looks strong. Reduce the chance of overshooting by using slightly less heat than baseline."

                comp.heatRetentionOffset >= 0.5 ->
                    "Heat retention is slightly favorable. Avoid stacking too much heat into the next phase."

                completeness == "low" ->
                    "Use conservative baseline heat until more live roast data is available."

                else ->
                    "Keep heat near baseline."
            }
        }

        val controlLine = when {
            control.recommendedHeatBiasPercent > 0 ->
                "Control model adds heat bias ${formatSignedPercent(control.recommendedHeatBiasPercent)}."

            control.recommendedHeatBiasPercent < 0 ->
                "Control model reduces heat by ${formatSignedPercent(control.recommendedHeatBiasPercent)}."

            else ->
                "Control model keeps heat at baseline."
        }

        return "$baseAdvice $controlLine"
    }

    private fun buildAirflowAdvice(
        comp: EnvironmentCompensationResult?,
        control: RoastControlAdjustment,
        completeness: String
    ): String {
        val baseAdvice = if (comp == null) {
            "No airflow compensation data available; keep airflow near normal reference."
        } else {
            when {
                comp.airflowEfficiencyOffset <= -1.0 ->
                    "Airflow efficiency looks clearly reduced. Consider slightly stronger airflow to maintain exhaust response."

                comp.airflowEfficiencyOffset <= -0.5 ->
                    "Airflow efficiency looks a bit reduced. Watch exhaust behavior and be ready to open airflow slightly."

                comp.airflowEfficiencyOffset >= 1.0 ->
                    "Airflow efficiency looks strong. Avoid over-venting and unnecessary drying acceleration."

                comp.airflowEfficiencyOffset >= 0.5 ->
                    "Airflow response is slightly favorable. Keep airflow measured and avoid over-correction."

                completeness == "low" ->
                    "Hold airflow near baseline until more roast feedback is available."

                else ->
                    "Keep airflow close to baseline."
            }
        }

        val controlLine = when {
            control.recommendedAirflowBiasSteps > 0 ->
                "Control model increases airflow by ${formatSignedStep(control.recommendedAirflowBiasSteps)} step."

            control.recommendedAirflowBiasSteps < 0 ->
                "Control model decreases airflow by ${formatSignedStep(control.recommendedAirflowBiasSteps)} step."

            else ->
                "Control model keeps airflow at baseline."
        }

        return "$baseAdvice $controlLine"
    }

    private fun buildTimingAdvice(
        comp: EnvironmentCompensationResult?,
        completeness: String
    ): String {
        if (comp == null) {
            return "Use standard timing references."
        }

        return when {
            comp.dryingOffset >= 1.0 ->
                "Drying may run clearly slower than baseline. Expect more patience before phase transitions."

            comp.dryingOffset >= 0.5 ->
                "Drying may run a little slower than baseline. Do not rush early transitions."

            comp.dryingOffset <= -1.0 ->
                "Drying may run clearly faster than baseline. Watch for early momentum and avoid overdevelopment later."

            comp.dryingOffset <= -0.5 ->
                "Drying may run slightly faster than baseline. Be careful not to advance phases too aggressively."

            completeness == "low" ->
                "Use normal timing checkpoints, but wait for more live roast evidence."

            else ->
                "Phase timing should stay near baseline."
        }
    }

    private fun buildSummary(
        context: RoastAiContext,
        control: RoastControlAdjustment,
        completeness: String,
        heatAdvice: String,
        airflowAdvice: String,
        timingAdvice: String
    ): String {
        val env = context.environmentProfile
        val promptLine = if (context.userPrompt.isBlank()) {
            "No operator prompt provided."
        } else {
            "Operator prompt: ${context.userPrompt}"
        }

        return """
Environment baseline loaded.
Ambient temp: ${env?.ambientTempC ?: "-"} °C
Humidity: ${env?.ambientHumidityRh ?: "-"} %RH
Altitude: ${env?.altitudeMeters ?: "-"} m

Context completeness: $completeness
$promptLine

Control Model
Heat Bias: ${formatSignedPercent(control.recommendedHeatBiasPercent)}
Airflow Bias: ${formatSignedStep(control.recommendedAirflowBiasSteps)}
Reason: ${control.reason}

$heatAdvice
$airflowAdvice
$timingAdvice
        """.trimIndent()
    }

    private fun formatSignedPercent(value: Int): String {
        return if (value >= 0) "+${value}%" else "${value}%"
    }

    private fun formatSignedStep(value: Int): String {
        return if (value >= 0) "+$value" else "$value"
    }
}
