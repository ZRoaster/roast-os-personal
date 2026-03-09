package com.roastos.app

import kotlin.math.abs

data class RoastStabilityResult(
    val stability: String,
    val score: Int,
    val confidence: String,
    val reason: String,
    val suggestion: String,
    val summary: String
)

object RoastStabilityEngine {

    fun evaluate(prediction: RoastCurvePredictionV3): RoastStabilityResult {

        var score = 100
        val reasons = mutableListOf<String>()
        val suggestions = mutableListOf<String>()

        if (prediction.crashRisk) {
            score -= 28
            reasons.add("ROR crash risk detected")
            suggestions.add("increase energy or reduce excessive airflow")
        }

        if (prediction.flickRisk) {
            score -= 24
            reasons.add("ROR flick risk detected")
            suggestions.add("smooth heat application and avoid late aggressive pushes")
        }

        val slopeAbs = abs(prediction.rorSlope)
        when {
            slopeAbs > 2.5 -> {
                score -= 16
                reasons.add("ROR slope is highly unstable")
                suggestions.add("reduce abrupt control changes")
            }
            slopeAbs > 1.5 -> {
                score -= 8
                reasons.add("ROR slope is moderately unstable")
            }
        }

        val momentumAbs = abs(prediction.rorMomentum)
        when {
            momentumAbs > 2.0 -> {
                score -= 14
                reasons.add("ROR momentum is swinging strongly")
                suggestions.add("stabilize heat and airflow rhythm")
            }
            momentumAbs > 1.0 -> {
                score -= 7
                reasons.add("ROR momentum is somewhat unstable")
            }
        }

        when {
            prediction.chainScore < 40 -> {
                score -= 20
                reasons.add("anchor chain is far from baseline")
                suggestions.add("re-center roast pace toward baseline anchors")
            }
            prediction.chainScore < 60 -> {
                score -= 10
                reasons.add("anchor chain is moderately offset from baseline")
            }
            prediction.chainScore < 75 -> {
                score -= 5
                reasons.add("anchor chain is slightly offset from baseline")
            }
        }

        val turningDeltaAbs = abs(prediction.turningDelta ?: 0.0)
        val yellowDeltaAbs = abs(prediction.yellowDelta ?: 0.0)
        val fcDeltaAbs = abs(prediction.fcDelta ?: 0.0)
        val dropDeltaAbs = abs(prediction.dropDelta ?: 0.0)

        if (turningDeltaAbs > 18.0) {
            score -= 6
            reasons.add("turning anchor deviates from baseline")
        }

        if (yellowDeltaAbs > 22.0) {
            score -= 8
            reasons.add("yellow anchor deviates from baseline")
        }

        if (fcDeltaAbs > 25.0) {
            score -= 12
            reasons.add("FC anchor deviates from baseline")
            suggestions.add("adjust roast momentum before first crack")
        }

        if (dropDeltaAbs > 30.0) {
            score -= 12
            reasons.add("drop anchor deviates from baseline")
            suggestions.add("review development pacing")
        }

        when {
            prediction.confidence < 45 -> {
                score -= 10
                reasons.add("prediction confidence is low")
            }
            prediction.confidence < 60 -> {
                score -= 5
                reasons.add("prediction confidence is moderate")
            }
        }

        score = score.coerceIn(0, 100)

        val stability = when {
            prediction.crashRisk && prediction.flickRisk -> "Highly Unstable"
            prediction.crashRisk -> "Crash Risk"
            prediction.flickRisk -> "Flick Risk"
            score >= 85 -> "Stable"
            score >= 70 -> "Mostly Stable"
            score >= 55 -> "Slightly Unstable"
            score >= 40 -> "Unstable"
            else -> "Highly Unstable"
        }

        val confidence = when {
            prediction.confidence >= 80 -> "High"
            prediction.confidence >= 60 -> "Medium"
            else -> "Low"
        }

        val reason = if (reasons.isEmpty()) {
            "ROR behavior and anchor chain are stable"
        } else {
            reasons.joinToString(separator = " | ")
        }

        val suggestion = if (suggestions.isEmpty()) {
            when (stability) {
                "Stable" -> "hold current roast rhythm"
                "Mostly Stable" -> "maintain course and monitor ROR closely"
                else -> "make smaller and earlier corrections"
            }
        } else {
            suggestions.distinct().joinToString(separator = " | ")
        }

        val summary = """
Roast Stability

Stability
$stability

Score
$score

Confidence
$confidence

Reason
$reason

Suggestion
$suggestion
        """.trimIndent()

        return RoastStabilityResult(
            stability = stability,
            score = score,
            confidence = confidence,
            reason = reason,
            suggestion = suggestion,
            summary = summary
        )
    }
}
