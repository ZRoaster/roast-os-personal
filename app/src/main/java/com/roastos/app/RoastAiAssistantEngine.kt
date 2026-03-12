package com.roastos.app

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

        val envComp = context.environmentCompensation

        val heatAdvice = when {
            envComp == null -> "Hold current heat unless roast data suggests otherwise."
            envComp.heatRetentionOffset <= -0.50 -> "Consider slightly more heat support than baseline."
            envComp.heatRetentionOffset >= 0.50 -> "Consider slightly less heat than baseline."
            else -> "Keep current heat near baseline."
        }

        val airflowAdvice = when {
            envComp == null -> "Hold current airflow unless exhaust looks weak."
            envComp.airflowEfficiencyOffset <= -0.50 -> "Airflow efficiency looks reduced; consider slightly stronger airflow."
            envComp.airflowEfficiencyOffset >= 0.50 -> "Airflow efficiency looks favorable; avoid over-venting."
            else -> "Keep airflow close to baseline."
        }

        val timingAdvice = when {
            envComp == null -> "Use normal phase timing references."
            envComp.dryingOffset >= 0.50 -> "Expect drying to run a little slower than baseline."
            envComp.dryingOffset <= -0.50 -> "Expect drying to run a little faster than baseline."
            else -> "Phase timing should stay near baseline."
        }

        val riskLevel = when {
            context.environmentCompensation == null -> "low"
            kotlin.math.abs(envComp!!.heatRetentionOffset) >= 1.0 ||
                kotlin.math.abs(envComp.dryingOffset) >= 1.0 ||
                kotlin.math.abs(envComp.airflowEfficiencyOffset) >= 1.0 -> "watch"
            else -> "low"
        }

        val summary = buildSummary(
            context = context,
            heatAdvice = heatAdvice,
            airflowAdvice = airflowAdvice,
            timingAdvice = timingAdvice
        )

        return RoastAiAssistantOutput(
            title = "Roast AI Assistant",
            summary = summary,
            heatAdvice = heatAdvice,
            airflowAdvice = airflowAdvice,
            timingAdvice = timingAdvice,
            riskLevel = riskLevel,
            confidence = "mock-stable"
        )
    }

    private fun buildSummary(
        context: RoastAiContext,
        heatAdvice: String,
        airflowAdvice: String,
        timingAdvice: String
    ): String {
        val env = context.environmentProfile

        return """
Environment baseline loaded.
Ambient temp: ${env?.ambientTempC ?: "-"} °C
Humidity: ${env?.ambientHumidityRh ?: "-"} %RH
Altitude: ${env?.altitudeMeters ?: "-"} m

$heatAdvice
$airflowAdvice
$timingAdvice
        """.trimIndent()
    }
}
