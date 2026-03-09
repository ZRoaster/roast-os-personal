package com.roastos.app

data class RoastDrivingAdvice(
    val actionLevel: String,
    val heatAdvice: String,
    val airflowAdvice: String,
    val timingAdvice: String,
    val reason: String,
    val confidence: String,
    val summary: String
)

object RoastDrivingAdvisorEngine {

    fun evaluate(
        prediction: RoastCurvePredictionV3,
        stability: RoastStabilityResult
    ): RoastDrivingAdvice {

        var actionLevel = "Monitor"
        var heatAdvice = "Hold heat"
        var airflowAdvice = "Hold airflow"
        var timingAdvice = "Stay on current roast pace"

        val reasons = mutableListOf<String>()
        var confidence = "Medium"

        if (prediction.crashRisk) {
            actionLevel = "Intervene"
            heatAdvice = "Increase heat slightly"
            airflowAdvice = "Reduce excessive airflow"
            timingAdvice = "Recover momentum before anchor drift expands"
            reasons.add("ROR crash risk detected")
        }

        if (prediction.flickRisk) {
            actionLevel = "Intervene"
            heatAdvice = "Reduce heat slightly"
            airflowAdvice = "Increase airflow slightly"
            timingAdvice = "Prevent late-stage overreaction and flick"
            reasons.add("ROR flick risk detected")
        }

        if (!prediction.crashRisk && !prediction.flickRisk) {
            when {
                prediction.rorMomentum < -1.5 -> {
                    actionLevel = "Adjust"
                    heatAdvice = "Increase heat slightly"
                    airflowAdvice = "Hold airflow"
                    timingAdvice = "Protect FC timing from slipping late"
                    reasons.add("ROR momentum is falling")
                }

                prediction.rorMomentum > 1.2 -> {
                    actionLevel = "Adjust"
                    heatAdvice = "Reduce heat slightly"
                    airflowAdvice = "Increase airflow slightly"
                    timingAdvice = "Slow the rate of rise before flick develops"
                    reasons.add("ROR momentum is rising too quickly")
                }
            }
        }

        val fcDelta = prediction.fcDelta
        val dropDelta = prediction.dropDelta

        if (fcDelta != null) {
            when {
                fcDelta > 20.0 -> {
                    if (actionLevel == "Monitor") actionLevel = "Adjust"
                    heatAdvice = "Increase heat slightly"
                    timingAdvice = "FC is trending late versus baseline"
                    reasons.add("FC predicted later than baseline")
                }

                fcDelta < -20.0 -> {
                    if (actionLevel == "Monitor") actionLevel = "Adjust"
                    heatAdvice = "Reduce heat slightly"
                    airflowAdvice = if (airflowAdvice == "Hold airflow") {
                        "Increase airflow slightly"
                    } else {
                        airflowAdvice
                    }
                    timingAdvice = "FC is trending early versus baseline"
                    reasons.add("FC predicted earlier than baseline")
                }
            }
        }

        if (dropDelta != null) {
            when {
                dropDelta > 25.0 -> {
                    if (actionLevel == "Monitor") actionLevel = "Adjust"
                    timingAdvice = "Development is stretching later than baseline"
                    reasons.add("Drop predicted later than baseline")
                }

                dropDelta < -25.0 -> {
                    if (actionLevel == "Monitor") actionLevel = "Adjust"
                    timingAdvice = "Development is compressing earlier than baseline"
                    reasons.add("Drop predicted earlier than baseline")
                }
            }
        }

        when (stability.stability) {
            "Stable" -> {
                if (actionLevel == "Monitor") {
                    heatAdvice = "Hold heat"
                    airflowAdvice = "Hold airflow"
                    timingAdvice = "Continue current rhythm"
                }
                reasons.add("Roast stability is strong")
            }

            "Mostly Stable" -> {
                if (actionLevel == "Monitor") {
                    actionLevel = "Monitor"
                    timingAdvice = "Hold course and monitor anchor drift"
                }
                reasons.add("Roast stability is acceptable")
            }

            "Slightly Unstable" -> {
                if (actionLevel == "Monitor") {
                    actionLevel = "Adjust"
                    timingAdvice = "Apply smaller earlier corrections"
                }
                reasons.add("Roast is slightly unstable")
            }

            "Unstable" -> {
                actionLevel = "Adjust"
                timingAdvice = "Correct rhythm before instability expands"
                reasons.add("Roast is unstable")
            }

            "Crash Risk" -> {
                actionLevel = "Intervene"
                heatAdvice = "Increase heat slightly"
                airflowAdvice = "Reduce airflow if excessively high"
                timingAdvice = "Protect roast energy before crash deepens"
                reasons.add("Stability engine flags crash risk")
            }

            "Flick Risk" -> {
                actionLevel = "Intervene"
                heatAdvice = "Reduce heat slightly"
                airflowAdvice = "Increase airflow slightly"
                timingAdvice = "Calm the curve before flick expands"
                reasons.add("Stability engine flags flick risk")
            }

            "Highly Unstable" -> {
                actionLevel = "Intervene"
                timingAdvice = "Make immediate corrective action"
                reasons.add("Roast is highly unstable")
            }
        }

        confidence = when {
            prediction.confidence >= 80 && stability.score >= 80 -> "High"
            prediction.confidence >= 60 && stability.score >= 60 -> "Medium"
            else -> "Low"
        }

        val reason = if (reasons.isEmpty()) {
            "No major instability detected"
        } else {
            reasons.distinct().joinToString(" | ")
        }

        val summary = """
Roast Driving Advice

Action Level
$actionLevel

Heat
$heatAdvice

Airflow
$airflowAdvice

Timing
$timingAdvice

Reason
$reason

Confidence
$confidence
        """.trimIndent()

        return RoastDrivingAdvice(
            actionLevel = actionLevel,
            heatAdvice = heatAdvice,
            airflowAdvice = airflowAdvice,
            timingAdvice = timingAdvice,
            reason = reason,
            confidence = confidence,
            summary = summary
        )
    }
}
