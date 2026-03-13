package com.roastos.app

import kotlin.math.roundToInt

data class RoastRorPrediction(
    val trendLabel: String,
    val predictedRisk: String,
    val estimatedFirstCrackWindowSec: Int?,
    val reason: String
) {

    fun summaryText(): String {
        val fc = estimatedFirstCrackWindowSec?.let {
            "${it / 60}:${"%02d".format(it % 60)}"
        } ?: "-"

        return """
RoR Trend
$trendLabel

Predicted Risk
$predictedRisk

Estimated First Crack
$fc

Reason
$reason
        """.trimIndent()
    }
}

object RoastRorPredictionEngine {

    fun evaluate(
        snapshot: RoastSessionBusSnapshot
    ): RoastRorPrediction {

        val session = snapshot.session
        val ror = session.lastRor
        val bt = session.lastBeanTemp
        val phase = snapshot.companion.phaseLabel
        val elapsed = session.lastElapsedSec

        val trend = buildTrend(ror, phase)
        val risk = buildRisk(ror, phase)
        val fc = estimateFirstCrack(bt, ror, phase, elapsed)
        val reason = buildReason(ror, bt, phase)

        return RoastRorPrediction(
            trendLabel = trend,
            predictedRisk = risk,
            estimatedFirstCrackWindowSec = fc,
            reason = reason
        )
    }

    private fun buildTrend(
        ror: Double,
        phase: String
    ): String {

        return when {
            ror > 12 -> "RoR very aggressive"
            ror > 9 -> "RoR high"
            ror > 6 -> "RoR healthy"
            ror > 4 -> "RoR moderate"
            ror > 2.5 -> "RoR dropping"
            else -> "RoR weak"
        }
    }

    private fun buildRisk(
        ror: Double,
        phase: String
    ): String {

        return when {
            ror < 2.5 && phase != "Charge" -> "stall risk"
            ror > 12 && phase != "Charge" -> "flick risk"
            ror > 9 && phase == "Maillard" -> "possible overshoot"
            else -> "normal"
        }
    }

    private fun estimateFirstCrack(
        bt: Double,
        ror: Double,
        phase: String,
        elapsed: Int
    ): Int? {

        if (phase == "First Crack" || phase == "Development") {
            return elapsed
        }

        if (ror <= 0.5) {
            return null
        }

        val target = 196.0
        val delta = target - bt

        if (delta <= 0) {
            return elapsed
        }

        val sec = (delta / (ror / 60.0)).roundToInt()

        return elapsed + sec
    }

    private fun buildReason(
        ror: Double,
        bt: Double,
        phase: String
    ): String {

        val parts = mutableListOf<String>()

        parts += "current ror ${"%.1f".format(ror)}"
        parts += "bean temp ${"%.1f".format(bt)}"
        parts += "phase ${phase.lowercase()}"

        if (ror < 3.0) {
            parts += "energy looks weak"
        }

        if (ror > 10.0) {
            parts += "momentum looks strong"
        }

        return parts.joinToString("; ")
    }
}
